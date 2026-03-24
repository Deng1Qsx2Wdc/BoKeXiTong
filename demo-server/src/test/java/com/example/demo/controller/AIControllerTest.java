package com.example.demo.controller;

import com.example.demo.interceptor.AdminInterceptor;
import com.example.demo.interceptor.ArticleInterceptor;
import com.example.demo.interceptor.AuthorInterceptor;
import com.example.demo.interceptor.CategoryInterceptor;
import com.example.demo.interceptor.Interceptor;
import com.example.demo.service.AIService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AIController 单元测试
 * Mock 外部 API 调用，验证 SSE 流式输出
 */
@WebMvcTest(AIController.class)
@AutoConfigureMockMvc(addFilters = false)
class AIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AIService aiService;

    @MockBean
    private AdminInterceptor adminInterceptor;

    @MockBean
    private AuthorInterceptor authorInterceptor;

    @MockBean
    private ArticleInterceptor articleInterceptor;

    @MockBean
    private CategoryInterceptor categoryInterceptor;

    @MockBean(name = "interceptor")
    private Interceptor interceptor;

    @Test
    void testPolish_ShouldReturnSseStream() throws Exception {
        doAnswer(invocation -> {
            Consumer<String> onChunk = invocation.getArgument(1);
            onChunk.accept("chunk-1");
            onChunk.accept("chunk-2");
            onChunk.accept("chunk-3");
            return null;
        }).when(aiService).polish(eq("test-text"), any());

        MvcResult result = mockMvc.perform(post("/api/ai/generate")
                        .param("type", "polish")
                        .param("text", "test-text")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .characterEncoding("UTF-8"))
                .andExpect(request().asyncStarted())
                .andReturn();

        MvcResult asyncResult = mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andReturn();

        String content = asyncResult.getResponse().getContentAsString();
        assertTrue(content.contains("event:message"));
        assertTrue(content.contains("chunk-1"));
        assertTrue(content.contains("chunk-2"));
        assertTrue(content.contains("chunk-3"));
        assertTrue(content.contains("event:done"));
        assertTrue(content.contains("[DONE]"));
    }

    @Test
    void testContinueWriting_ShouldReturnSseStream() throws Exception {
        doAnswer(invocation -> {
            Consumer<String> onChunk = invocation.getArgument(1);
            onChunk.accept("continue-1");
            onChunk.accept("continue-2");
            return null;
        }).when(aiService).continueWriting(eq("source-text"), any());

        MvcResult result = mockMvc.perform(post("/api/ai/generate")
                        .param("type", "continue")
                        .param("text", "source-text")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .characterEncoding("UTF-8"))
                .andExpect(request().asyncStarted())
                .andReturn();

        MvcResult asyncResult = mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andReturn();

        String content = asyncResult.getResponse().getContentAsString();
        assertTrue(content.contains("continue-1"));
        assertTrue(content.contains("continue-2"));
        assertTrue(content.contains("[DONE]"));
    }

    @Test
    void testSummarize_ShouldReturnSseStream() throws Exception {
        doAnswer(invocation -> {
            Consumer<String> onChunk = invocation.getArgument(1);
            onChunk.accept("summary-1");
            onChunk.accept("summary-2");
            return null;
        }).when(aiService).summarize(eq("long-text"), any());

        MvcResult result = mockMvc.perform(post("/api/ai/generate")
                        .param("type", "summarize")
                        .param("text", "long-text")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .characterEncoding("UTF-8"))
                .andExpect(request().asyncStarted())
                .andReturn();

        MvcResult asyncResult = mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andReturn();

        String content = asyncResult.getResponse().getContentAsString();
        assertTrue(content.contains("summary-1"));
        assertTrue(content.contains("summary-2"));
        assertTrue(content.contains("[DONE]"));
    }

    @Test
    void testInvalidType_ShouldReturnError() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/ai/generate")
                        .param("type", "invalid")
                        .param("text", "test-text")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .characterEncoding("UTF-8"))
                .andExpect(request().asyncStarted())
                .andReturn();

        MvcResult asyncResult = mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        Exception resolvedException = asyncResult.getResolvedException();
        assertNotNull(resolvedException);
        assertTrue(resolvedException instanceof IllegalArgumentException);
        assertTrue(resolvedException.getMessage().contains("invalid"));

        String content = asyncResult.getResponse().getContentAsString();
        assertTrue(content.contains("\"code\":500"));
        assertTrue(content.contains("\"data\":null"));
    }
}
