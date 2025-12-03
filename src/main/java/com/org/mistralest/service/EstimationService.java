package com.org.mistralest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.mistralest.dto.response.EstimationResponse;
import com.org.mistralest.util.DocumentTextExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EstimationService {

    @Value("classpath:prompts/estimation_prompt.txt")
    private Resource promptResource;

    private final ChatClient chatClient;
    private final ObjectMapper mapper;

    private final Logger log = LoggerFactory.getLogger(getClass());

    public EstimationService(ChatClient chatClient, ObjectMapper mapper) {
        this.chatClient = chatClient;
        this.mapper = mapper;
    }

    public EstimationResponse generateEstimation(MultipartFile file, String feedback) throws Exception {
        String brd = DocumentTextExtractor.extractText(file);
        return generate(brd, feedback);
    }

    private String buildPrompt(String brd, String feedback) {
        try {
            String template = new String(
                    promptResource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            return template
                    .replace("{{BRD}}", brd)
                    .replace("{{FEEDBACK}}", feedback == null ? "None" : feedback);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load prompt template", e);
        }
    }

    public EstimationResponse generate(String brd, String feedback) throws Exception {
        try {

            String prompt = buildPrompt(brd, feedback);
            String response = chatClient.prompt().user(prompt).call().content();
            log.info("AI Raw Output:\n{}", response);

            if (response == null || response.isBlank()) {
                throw new IllegalStateException("Empty response received from AI");
            }
            // extract json
            String jsonBlock = extractJsonBlock(response);
            Map<String, Object> json = mapper.readValue(jsonBlock, Map.class);

            int idx = response.indexOf(jsonBlock);
            String markdown = idx >= 0 ? response.substring(0, idx).trim() : "";

            return new EstimationResponse(markdown, json);

        } catch (Exception ex) {
            log.error("Failed to parse JSON. JSON content:\n{}\nError: {}",  ex.getMessage());
            throw new IllegalStateException("Invalid JSON from model", ex);
        }
    }

    private String extractFencedJson(String text) {
        Pattern fence = Pattern.compile("(?is)```json\\s*(\\{.*?\\})\\s*```");
        Matcher m = fence.matcher(text);
        if (m.find()) {
            return m.group(1).trim();
        }
        Pattern fence2 = Pattern.compile("(?is)```\\s*(\\{.*?\\})\\s*```");
        Matcher m2 = fence2.matcher(text);
        if (m2.find()) {
            return m2.group(1).trim();
        }
        return null;
    }

    /**
     * Find the first balanced JSON object by scanning for '{' and matching braces.
     */
    private String findFirstBalancedJson(String s) {
        int start = s.indexOf('{');
        if (start < 0) return null;
        int depth = 0;
        boolean inString = false;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' && s.charAt(i - 1 >= 0 ? i - 1 : 0) != '\\') {
                inString = !inString;
            }
            if (inString) continue;
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return s.substring(start, i + 1).trim();
                }
            }
        }
        return null;
    }



    private String extractJsonBlock(String text) {
        if (text == null) return null;

        // 1) Look for a line that contains JSON with dashes (case-insensitive)
        Pattern sepPattern = Pattern.compile("(?im)^\\s*-{3,}\\s*JSON\\s*-{0,}\\s*$");
        Matcher mSep = sepPattern.matcher(text);
        if (mSep.find()) {
            int after = mSep.end();
            String afterText = text.substring(after);
            String fenced = extractFencedJson(afterText);
            if (fenced != null) return fenced;
            String firstJson = findFirstBalancedJson(afterText);
            if (firstJson != null) return firstJson;
            String trimmed = afterText.trim();
            if (trimmed.startsWith("{")) {
                return findFirstBalancedJson(trimmed);
            }
        }

        String fenced = extractFencedJson(text);
        if (fenced != null) return fenced;

        int idx = text.indexOf("---JSON---");
        if (idx >= 0) {
            String after = text.substring(idx + "---JSON---".length());
            String firstJson = findFirstBalancedJson(after);
            if (firstJson != null) return firstJson;
        }

        String fb = findFirstBalancedJson(text);
        if (fb != null) return fb;
        return null;
    }
}
