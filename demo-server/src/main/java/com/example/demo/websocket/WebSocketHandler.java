package com.example.demo.websocket;

import com.alibaba.fastjson.JSON;
import com.example.demo.pojo.entity.Author;
import com.example.demo.pojo.entity.WebSocketMessage;

import com.example.demo.service.impl.FollowsService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PreDestroy;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
@ServerEndpoint("/ws/{authorId}")
public class WebSocketHandler {
    private static ConcurrentHashMap<String, Session> concurrentHashMap = new ConcurrentHashMap<>();
    private static AtomicInteger counter = new AtomicInteger(0);

    // 定期清理任务
    private static final ScheduledExecutorService cleanupScheduler =
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "websocket-cleanup");
            t.setDaemon(true);
            return t;
        });

    // 会话最后活跃时间
    private static final ConcurrentHashMap<String, Long> lastActiveTime = new ConcurrentHashMap<>();
    private static final long SESSION_TIMEOUT_MS = 5 * 60 * 1000; // 5分钟超时

    static {
        // 每分钟清理一次无效会话
        cleanupScheduler.scheduleAtFixedRate(() -> {
            try {
                cleanupInactiveSessions();
            } catch (Exception e) {
                log.error("定期清理会话失败", e);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("authorId") String authorId) {
        concurrentHashMap.put(authorId, session);
        lastActiveTime.put(authorId, System.currentTimeMillis());
        int counts = counter.incrementAndGet();
        WebSocketMessage message = new WebSocketMessage();
        message.setType("WELCOME");
        message.setMessage("欢迎!当前在线人数:"+counts);
        message.setFromId(authorId);
        message.setToId(authorId);
        message.setTimestamp(new Date());
        sendMessageToUser(message);
    }
    @OnMessage
    public void onMessage(String message, Session session) {
//        WebSocketMessage webSocketMessage = JSON.parseObject(message, WebSocketMessage.class);
//        if(webSocketMessage.getType().equals("PRIVATE_MSG")){
//            sendMessageToUser(session.getId(),webSocketMessage);
//        }
    }
    @OnClose
    public void onClose(@PathParam("authorId") String authorId, Session session) {
        // 移除当前会话
        Session removed = concurrentHashMap.remove(authorId);
        lastActiveTime.remove(authorId);
        if (removed != null) {
            counter.decrementAndGet();
        }

        // 关闭会话
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (Exception e) {
            log.error("关闭会话失败，用户ID: {}", authorId, e);
        }

        // 广播在线人数更新
        WebSocketMessage webSocketMessage = new WebSocketMessage();
        webSocketMessage.setType("ONLINE_COUNT");
        webSocketMessage.setMessage("当前在线人数:" + counter.get());
        broadcastMessage(webSocketMessage);
    }
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 错误", error);

        // 发生错误时清理会话
        String authorId = findAuthorIdBySession(session);
        if (authorId != null) {
            concurrentHashMap.remove(authorId);
            lastActiveTime.remove(authorId);
            counter.decrementAndGet();
        }

        // 尝试关闭会话
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (Exception e) {
            log.error("关闭错误会话失败", e);
        }
    }

    // 清理无效会话（包括超时和断开的会话）
    private static void cleanupInactiveSessions() {
        long now = System.currentTimeMillis();
        int cleanedCount = 0;

        for (String authorId : concurrentHashMap.keySet()) {
            Session session = concurrentHashMap.get(authorId);
            Long lastActive = lastActiveTime.get(authorId);

            boolean shouldRemove = false;
            String reason = "";

            // 检查会话是否无效
            if (session == null || !session.isOpen()) {
                shouldRemove = true;
                reason = "会话已断开";
            }
            // 检查是否超时
            else if (lastActive != null && (now - lastActive) > SESSION_TIMEOUT_MS) {
                shouldRemove = true;
                reason = "会话超时";
                try {
                    session.close();
                } catch (Exception e) {
                    log.error("关闭超时会话失败，用户ID: {}", authorId, e);
                }
            }

            if (shouldRemove) {
                concurrentHashMap.remove(authorId);
                lastActiveTime.remove(authorId);
                counter.decrementAndGet();
                cleanedCount++;
                log.info("清理无效会话，用户ID: {}, 原因: {}", authorId, reason);
            }
        }

        if (cleanedCount > 0) {
            log.info("本次清理了 {} 个无效会话，当前在线: {}", cleanedCount, counter.get());
        }
    }

    // 根据 Session 查找 authorId
    private String findAuthorIdBySession(Session session) {
        for (ConcurrentHashMap.Entry<String, Session> entry : concurrentHashMap.entrySet()) {
            if (entry.getValue().equals(session)) {
                return entry.getKey();
            }
        }
        return null;
    }

    //给单个用户发送消息
    public static void sendMessageToUser(WebSocketMessage message) {
        Session session = concurrentHashMap.get(message.getToId());
        if (session != null) {
            try{
                String json = JSON.toJSONString(message);
                session.getBasicRemote().sendText(json);
                // 更新活跃时间
                lastActiveTime.put(message.getToId(), System.currentTimeMillis());
            }catch (Exception e){
                log.error("发送消息失败，目标用户ID: {}", message.getToId(), e);
            }
        }
    }
    //广播消息
    public  static void broadcastMessage(WebSocketMessage message) {
        String json = JSON.toJSONString(message);
        long now = System.currentTimeMillis();

        for (String authorId : concurrentHashMap.keySet()) {
            Session session = concurrentHashMap.get(authorId);
            if (session != null && session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(json);
                    lastActiveTime.put(authorId, now);
                } catch (Exception e) {
                    log.error("广播消息失败，用户ID: {}", authorId, e);
                }
            }
        }
    }
    //广播在线人数
    public static void broadcastOnlineCount(WebSocketMessage message) {
        WebSocketMessage webSocketMessage = new WebSocketMessage();
        webSocketMessage.setType("BROADCAST");
        webSocketMessage.setMessage(String.valueOf(counter));
        broadcastMessage(webSocketMessage);
    }

    // 应用关闭时清理资源
    @PreDestroy
    public void shutdown() {
        log.info("WebSocket 服务关闭，清理资源...");
        cleanupScheduler.shutdown();
        try {
            if (!cleanupScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
