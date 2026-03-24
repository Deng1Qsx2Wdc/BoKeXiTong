package com.example.demo.controller;

import com.example.demo.service.AIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * AI 创作控制器
 * 提供流式输出接口（Server-Sent Events）
 */
@RestController
@RequestMapping("/api/ai")
@Slf4j
public class AIController {

    @Autowired
    private AIService aiService;

    // 线程池用于异步处理 AI 请求
    // 使用有界线程池防止内存泄漏：核心10线程，最大50线程，队列容量100
    private final ExecutorService executorService = new ThreadPoolExecutor(
        10,                         // 核心线程数
        50,                                    // 最大线程数
        60L,                                    // 空闲线程存活时间
        SECONDS,                                //时间单位
        new LinkedBlockingQueue<>(100),        // 有界队列，防止无限积压
        new ThreadFactory() {
            private int counter = 0;
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "ai-worker-" + counter++);
                t.setDaemon(false);
                return t;
            }
        },
        new ThreadPoolExecutor.CallerRunsPolicy()  // 队列满时由调用线程执行
    );

    /**
     * AI 生成接口（流式输出）
     * @param type 操作类型：polish（润色）、continue（续写）、summarize（摘要）
     * @param text 输入文本
     * @return SseEmitter 流式响应
     */
    @PostMapping(value = "/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generate(
            @RequestParam String type,
            @RequestParam String text) {

        log.info("收到 AI 生成请求 - 类型: {}, 文本长度: {}", type, text.length());

        // 创建 SseEmitter，设置超时时间为 5 分钟
        SseEmitter emitter = new SseEmitter(300_000L);

        // 异步处理 AI 请求
        executorService.execute(() -> {
            try {
                // 根据类型调用不同的服务方法
                switch (type.toLowerCase()) {
                    case "polish":
                        aiService.polish(text, chunk -> sendChunk(emitter, chunk));
                        break;
                    case "continue":
                        aiService.continueWriting(text, chunk -> sendChunk(emitter, chunk));
                        break;
                    case "summarize":
                        aiService.summarize(text, chunk -> sendChunk(emitter, chunk));
                        break;
                    default:
                        emitter.completeWithError(new IllegalArgumentException("不支持的操作类型: " + type));
                        return;
                }

                // 发送完成标记
                emitter.send(SseEmitter.event()
                        .name("done")
                        .data("[DONE]"));
                emitter.complete();
                log.info("AI 生成完成 - 类型: {}", type);

            } catch (Exception e) {
                log.error("AI 生成失败 - 类型: {}", type, e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("生成失败: " + e.getMessage()));
                } catch (IOException ioException) {
                    log.error("发送错误消息失败", ioException);
                }
                emitter.completeWithError(e);
            }
        });

        // 设置超时和错误回调
        emitter.onTimeout(() -> {
            log.warn("AI 生成超时 - 类型: {}", type);
            emitter.complete();
        });

        emitter.onError(e -> {
            log.error("SSE 连接错误 - 类型: {}", type, e);
        });

        return emitter;
    }

    /**
     * 发送文本片段到客户端
     */
    private void sendChunk(SseEmitter emitter, String chunk) {
        try {
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(chunk));
        } catch (IOException e) {
            log.error("发送 SSE 数据失败", e);
            emitter.completeWithError(e);
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public String health() {
        return "AI Service is running";
    }

    /**
     * 清理资源
     */
    @PreDestroy
    public void destroy() {
        log.info("正在关闭 AI Controller 线程池...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.error("线程池未能正常关闭");
                }
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("AI Controller 线程池已关闭");
    }
}
