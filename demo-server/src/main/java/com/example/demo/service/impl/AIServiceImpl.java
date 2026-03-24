package com.example.demo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.config.AIConfig;
import com.example.demo.service.AIService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * AI 服务实现类
 * 使用 OpenAI 兼容格式调用上游 API。
 */
@Service
@Slf4j
public class AIServiceImpl implements AIService {

    @Autowired
    private AIConfig aiConfig;

    @Autowired
    private OkHttpClient okHttpClient;

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    @Override
    public void polish(String text, Consumer<String> onChunk) throws Exception {
        String systemPrompt = "你是一个专业的文本润色助手。请对用户提供的文本进行润色，使其更加流畅、准确、优雅。保持原意不变，只优化表达方式。";
        callAIStream(systemPrompt, text, onChunk);
    }

    @Override
    public void continueWriting(String text, Consumer<String> onChunk) throws Exception {
        String systemPrompt = "你是一个专业的写作助手。请根据用户提供的文本内容，自然地继续写下去。保持风格一致，内容连贯。";
        callAIStream(systemPrompt, text, onChunk);
    }

    @Override
    public void summarize(String text, Consumer<String> onChunk) throws Exception {
        String systemPrompt = "你是一个专业的文本摘要助手。请对用户提供的文本进行摘要，提取核心要点，简洁明了。";
        callAIStream(systemPrompt, text, onChunk);
    }

    /**
     * 调用 AI API 并处理流式响应。
     */
    private void callAIStream(String systemPrompt, String userText, Consumer<String> onChunk) throws Exception {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", aiConfig.getModel());
        requestBody.put("stream", true);

        JSONArray messages = new JSONArray();

        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", userText);
        messages.add(userMessage);

        requestBody.put("messages", messages);

        String url = buildChatCompletionsUrl();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + aiConfig.getKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toJSONString(), JSON_MEDIA_TYPE))
                .build();

        log.info("调用 AI API: {}", url);

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                log.error("AI API 调用失败: {} - {}", response.code(), errorBody);
                throw new IOException("AI API 调用失败: " + response.code() + " - " + errorBody);
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IOException("响应体为空");
            }

            boolean receivedContent = false;
            StringBuilder rawResponse = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(responseBody.byteStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    rawResponse.append(line).append('\n');

                    if (!line.startsWith("data: ")) {
                        continue;
                    }

                    String data = line.substring(6).trim();
                    if ("[DONE]".equals(data)) {
                        break;
                    }

                    try {
                        String content = extractContent(JSON.parseObject(data));
                        if (content != null && !content.isEmpty()) {
                            onChunk.accept(content);
                            receivedContent = true;
                        }
                    } catch (Exception e) {
                        log.warn("解析 SSE 数据失败: {}", data, e);
                    }
                }
            }

            if (!receivedContent) {
                String fallbackContent = extractContentFromRawResponse(rawResponse.toString());
                if (fallbackContent != null && !fallbackContent.isEmpty()) {
                    onChunk.accept(fallbackContent);
                    return;
                }

                String snippet = rawResponse.length() > 300
                        ? rawResponse.substring(0, 300) + "..."
                        : rawResponse.toString();
                throw new IOException("AI API 未返回可解析内容，原始响应片段: " + snippet);
            }
        }
    }

    private String buildChatCompletionsUrl() {
        String baseUrl = aiConfig.getBaseUrl();
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalStateException("AI API base-url 未配置");
        }

        return baseUrl.replaceAll("/+$", "") + "/chat/completions";
    }

    private String extractContentFromRawResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            return null;
        }

        try {
            return extractContent(JSON.parseObject(rawResponse));
        } catch (Exception ignored) {
            // ignore and continue
        }

        StringBuilder collected = new StringBuilder();
        String[] lines = rawResponse.split("\\R");
        for (String line : lines) {
            if (!line.startsWith("data: ")) {
                continue;
            }

            String data = line.substring(6).trim();
            if ("[DONE]".equals(data)) {
                continue;
            }

            try {
                String content = extractContent(JSON.parseObject(data));
                if (content != null && !content.isEmpty()) {
                    collected.append(content);
                }
            } catch (Exception ignored) {
                // ignore invalid fragments and continue
            }
        }

        return collected.length() == 0 ? null : collected.toString();
    }

    private String extractContent(JSONObject jsonData) {
        if (jsonData == null) {
            return null;
        }

        JSONArray choices = jsonData.getJSONArray("choices");
        if (choices != null && !choices.isEmpty()) {
            JSONObject choice = choices.getJSONObject(0);

            String content = extractContentFromMessage(choice.getJSONObject("delta"));
            if (content != null && !content.isEmpty()) {
                return content;
            }

            content = extractContentFromMessage(choice.getJSONObject("message"));
            if (content != null && !content.isEmpty()) {
                return content;
            }

            String text = choice.getString("text");
            if (text != null && !text.isEmpty()) {
                return text;
            }
        }

        JSONObject message = jsonData.getJSONObject("message");
        if (message != null) {
            String content = extractContentFromMessage(message);
            if (content != null && !content.isEmpty()) {
                return content;
            }
        }

        String content = jsonData.getString("content");
        return (content == null || content.isEmpty()) ? null : content;
    }

    private String extractContentFromMessage(JSONObject message) {
        if (message == null) {
            return null;
        }

        String content = message.getString("content");
        if (content != null && !content.isEmpty()) {
            return content;
        }

        JSONArray contentArray = message.getJSONArray("content");
        if (contentArray == null || contentArray.isEmpty()) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < contentArray.size(); i++) {
            Object item = contentArray.get(i);
            if (item instanceof String) {
                builder.append(item);
                continue;
            }

            if (item instanceof JSONObject) {
                JSONObject part = (JSONObject) item;
                String type = part.getString("type");
                if ("text".equals(type) || type == null) {
                    String text = part.getString("text");
                    if (text != null) {
                        builder.append(text);
                    }
                }
            }
        }

        return builder.length() == 0 ? null : builder.toString();
    }
}
