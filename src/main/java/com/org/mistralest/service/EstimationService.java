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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EstimationService {

    @Value("classpath:prompts/estimation_prompt.txt")
    private Resource promptResource;

    private final ChatClient chatClient;
    private final ObjectMapper mapper;
    private final FileHistoryService fileHistoryService;

    private final Logger log = LoggerFactory.getLogger(getClass());

    public EstimationService(ChatClient chatClient, ObjectMapper mapper, FileHistoryService fileHistoryService) {
        this.chatClient = chatClient;
        this.mapper = mapper;
        this.fileHistoryService = fileHistoryService;
    }

    public EstimationResponse generateHistory(String filePath) throws Exception {
        String brd = DocumentTextExtractor.extractText(filePath);
        // 1. Extract filename from absolute path
        String fileName = fileHistoryService.getFileNameFromPath(filePath);
        // 2. Extract methodology from filename
        String methodology = fileHistoryService.extractMethodologyFromFileName(fileName);
        String prompt = buildPrompt(brd,methodology);
        return generate(prompt);
    }

 /*   public EstimationResponse generateEstimation(MultipartFile file) throws Exception {
        String brd = DocumentTextExtractor.extractText(file);
        return generate(brd);
    }*/

    // primary entrypoint used by controller
    public EstimationResponse generateEstimation(MultipartFile file, String methodology) throws Exception {
        String brd = DocumentTextExtractor.extractText(file);
        String prompt = buildPrompt(brd, methodology);
        return generate(prompt);
    }

    // new: replace methodology placeholder in template (if present)
    private String buildPrompt(String brd, String methodology) {
        String base = buildPrompt(brd);
        if (base.contains("{{METHODOLOGY}}")) {
            base = base.replace("{{METHODOLOGY}}", methodology);
            return base;
        } else {
            StringBuilder sb = new StringBuilder(base);
            sb.append("\n\nMethodology: ").append(methodology).append("\n");
            return sb.toString();
        }
    }


    private String buildPrompt(String brd) {
        try {
            String template = new String(
                    promptResource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            return template
                    .replace("{{BRD}}", brd);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load prompt template", e);
        }
    }

    public EstimationResponse generate(String prompt) throws Exception {
        try {
            String response = chatClient.prompt().user(prompt).call().content();
            log.info("AI Raw Output:\n{}", response);

            if (response == null || response.isBlank()) {
                throw new IllegalStateException("Empty response received from AI");
            }
            // extract json
            String jsonBlock = extractJsonBlock(response);
            Map<String, Object> json = mapper.readValue(jsonBlock, Map.class);
            Object wbsObj = json.get("wbs");
            List<Map<String, Object>> effortList = (List<Map<String, Object>>) json.get("effort");
            reconcileWbsAndEffort(json);

            int idx = response.indexOf(jsonBlock);
            String markdown = idx >= 0 ? response.substring(0, idx).trim() : "";

            return new EstimationResponse(markdown, json);

        } catch (Exception ex) {
            log.error("Failed to parse JSON. JSON content:\n{}\nError: {}",  ex.getMessage());
            throw new IllegalStateException("Invalid JSON from model", ex);
        }
    }


    /**
     * Ensures each module present in "effort" has a corresponding WBS and that:
     *   sum(tasks.estimate_hours) == effort.total
     * If mismatch, this method adjusts effort.total to match the WBS sum (server-side canonicalization).
     */
    @SuppressWarnings("unchecked")
    private void reconcileWbsAndEffort(Map<String, Object> json) {
        List<Map<String, Object>> wbsList = (List<Map<String, Object>>) json.get("wbs");
        Map<String, Integer> wbsSumByModule = new HashMap<>();
        if (wbsList != null) {
            for (Map<String, Object> module : wbsList) {
                String moduleName = String.valueOf(module.get("module"));
                int sum = 0;
                Object tasksObj = module.get("tasks");
                if (tasksObj instanceof List) {
                    for (Object t : (List) tasksObj) {
                        if (t instanceof Map) {
                            Number hrs = (Number) ((Map) t).getOrDefault("estimate_hours", 0);
                            sum += hrs.intValue();
                        }
                    }
                }
                wbsSumByModule.put(moduleName, sum);
            }
        }

        List<Map<String, Object>> effortList = (List<Map<String, Object>>) json.get("effort");
        if (effortList != null) {
            for (Map<String, Object> effort : effortList) {
                String moduleName = String.valueOf(effort.get("module"));
                int fe = ((Number) effort.getOrDefault("fe", 0)).intValue();
                int be = ((Number) effort.getOrDefault("be", 0)).intValue();
                int qa = ((Number) effort.getOrDefault("qa", 0)).intValue();
                int uiux = ((Number) effort.getOrDefault("uiux", 0)).intValue();
                int devops = ((Number) effort.getOrDefault("devops", 0)).intValue();
                int statedTotal = ((Number) effort.getOrDefault("total", 0)).intValue();

                int computedFromComponents = fe + be + qa + uiux + devops;
                Integer wbsSum = wbsSumByModule.get(moduleName);

                if (wbsSum != null) {
                    if (wbsSum != statedTotal) {
                        effort.put("total", wbsSum);
                    }

                } else {
                    if (computedFromComponents != statedTotal) {
                        effort.put("total", computedFromComponents);
                    }
                }
            }
        }

        // Ensure every module in effort appears in WBS; if not, add an empty WBS entry (model should normally provide)
        Set<String> effortModules = new HashSet<>();
        if (effortList != null) {
            for (Map<String, Object> e : effortList) {
                effortModules.add(String.valueOf(e.get("module")));
            }
        }

        if (wbsList == null) {
            wbsList = new ArrayList<>();
            json.put("wbs", wbsList);
        }

        Set<String> wbsModules = new HashSet<>();
        for (Map<String, Object> m : wbsList) wbsModules.add(String.valueOf(m.get("module")));

        for (String mod : effortModules) {
            if (!wbsModules.contains(mod)) {
                // add fallback WBS entry with single task that aggregates the effort total
                for (Map<String, Object> e : effortList) {
                    if (mod.equals(String.valueOf(e.get("module")))) {
                        int total = ((Number) e.getOrDefault("total", 0)).intValue();
                        Map<String, Object> fallback = new HashMap<>();
                        fallback.put("module", mod);
                        List<Map<String, Object>> tasks = new ArrayList<>();
                        Map<String, Object> t = new HashMap<>();
                        t.put("task", "Implementation (auto-generated WBS)");
                        t.put("estimate_hours", total);
                        t.put("owner_role", "TBD");
                        tasks.add(t);
                        fallback.put("tasks", tasks);
                        wbsList.add(fallback);
                        break;
                    }
                }
            }
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

        // prefer explicit separator
        int idx = text.indexOf("---JSON---");
        if (idx >= 0) {
            String after = text.substring(idx + "---JSON---".length());
            String fenced = extractFencedJson(after);
            if (fenced != null) return fenced;
            String firstJson = findFirstBalancedJson(after);
            if (firstJson != null) return firstJson;
        }

        // fenced anywhere
        String fenced = extractFencedJson(text);
        if (fenced != null) return fenced;

        // dashed "JSON" separator
        Pattern sepPattern = Pattern.compile("(?im)^\\s*-{3,}\\s*JSON\\s*-{0,}\\s*$");
        Matcher mSep = sepPattern.matcher(text);
        if (mSep.find()) {
            int after = mSep.end();
            String afterText = text.substring(after);
            String fenced2 = extractFencedJson(afterText);
            if (fenced2 != null) return fenced2;
            String firstJson2 = findFirstBalancedJson(afterText);
            if (firstJson2 != null) return firstJson2;
            String trimmed = afterText.trim();
            if (trimmed.startsWith("{")) return findFirstBalancedJson(trimmed);
        }

        // fallback
        return findFirstBalancedJson(text);
    }

}
