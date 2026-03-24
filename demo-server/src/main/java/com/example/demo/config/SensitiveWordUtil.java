package com.example.demo.config;

import com.example.demo.common.BusinessException;
import com.example.demo.common.ErrorCode;
import com.example.demo.common.constants.SensitiveWordConstants;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SensitiveWordUtil {

    private static final String END_FLAG = "isEnd";

    private final Map<Object, Object> dfaMap = new HashMap<>();

    @PostConstruct
    public void initDfaTree() {
        dfaMap.clear();
        for (String word : loadSensitiveWords()) {
            Map<Object, Object> currentMap = dfaMap;

            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                if (!currentMap.containsKey(c)) {
                    currentMap.put(c, new HashMap<>());
                }
                currentMap = (Map<Object, Object>) currentMap.get(c);
                if (i == word.length() - 1) {
                    currentMap.put(END_FLAG, Boolean.TRUE);
                }
            }
        }
    }

    private Set<String> loadSensitiveWords() {
        ClassPathResource resource = new ClassPathResource(SensitiveWordConstants.SENSITIVE_WORD_FILE_PATH);
        if (!resource.exists()) {
            throw new IllegalStateException("Missing sensitive word file: " + SensitiveWordConstants.SENSITIVE_WORD_FILE_PATH);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(String::trim)
                    .filter(word -> !word.isEmpty())
                    .collect(Collectors.toCollection(HashSet::new));
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load sensitive word file: " + SensitiveWordConstants.SENSITIVE_WORD_FILE_PATH,
                    e
            );
        }
    }

    public String filter(String content) {
        if (content == null || content.length() == 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        StringBuffer result = new StringBuffer(content);
        Map<Object, Object> currentMap = dfaMap;
        int matchStart = 0;
        for (int i = 0; i < result.length(); i++) {
            char c = result.charAt(i);
            if (currentMap.containsKey(c)) {
                if (matchStart == 0) {
                    matchStart = i;
                }
                currentMap = (Map<Object, Object>) currentMap.get(c);
                if (Boolean.TRUE.equals(currentMap.get(END_FLAG))) {
                    int matchEnd = i;
                    for (int j = matchStart; j <= matchEnd; j++) {
                        result.setCharAt(j, SensitiveWordConstants.REPLACE_CHAR);
                    }
                    matchStart = 0;
                    currentMap = dfaMap;
                }
            } else {
                matchStart = 0;
                currentMap = dfaMap;
            }
        }
        return result.toString();
    }
}
