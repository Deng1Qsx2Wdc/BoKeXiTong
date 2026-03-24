package com.example.demo.service;

import java.util.function.Consumer;

/**
 * AI 服务接口
 */
public interface AIService {

    /**
     * 润色文本
     * @param text 原始文本
     * @param onChunk 流式回调，每次接收到一个文本片段时调用
     */
    void polish(String text, Consumer<String> onChunk) throws Exception;

    /**
     * 续写文本
     * @param text 原始文本
     * @param onChunk 流式回调，每次接收到一个文本片段时调用
     */
    void continueWriting(String text, Consumer<String> onChunk) throws Exception;

    /**
     * 摘要生成
     * @param text 原始文本
     * @param onChunk 流式回调，每次接收到一个文本片段时调用
     */
    void summarize(String text, Consumer<String> onChunk) throws Exception;
}
