package com.reviewline.backend.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnthropicService {

    private final RestClient anthropicRestClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String buildPrompt(String language, String code) {
        String languageInstruction = language.equals("auto")
                ? "First, detect the programming language of the code below."
                : "The code is written in " + language + ".";

        return """
            You are a senior software engineer performing a code review. %s
            Analyze the following code and respond with ONLY valid JSON (no markdown, no explanation outside the JSON) matching exactly this structure:

            {
              "detectedLanguage": "the programming language, lowercase (e.g. javascript, python, java)",
              "summary": "one or two sentence overview of the code quality and main concerns",
              "score": <integer 0-100, where 100 is flawless>,
              "issues": [
                {
                  "severity": "bug" | "security" | "style",
                  "lineStart": <integer>,
                  "lineEnd": <integer>,
                  "title": "short title of the issue",
                  "explanation": "clear explanation of the problem and why it matters",
                  "suggestion": "brief suggestion for how to fix it, or null",
                  "beforeCode": "the problematic line(s), or null",
                  "afterCode": "the corrected version, or null"
                }
              ]
            }

            If the code has no issues, return an empty issues array and a high score.
            Line numbers refer to the code as given below, starting at line 1.

            Code to review:
            %s
            """.formatted(languageInstruction, code);
    }

    public JsonNode getReviewFromClaude(String language, String code) {
        String prompt = buildPrompt(language, code);

        Map<String, Object> requestBody = Map.of(
                "model", "claude-sonnet-5",
                "max_tokens", 2000,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        String rawResponse = anthropicRestClient.post()
                .body(requestBody)
                .retrieve()
                .body(String.class);

        System.out.println("=== RAW ANTHROPIC RESPONSE ===");
        System.out.println(rawResponse);

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode contentArray = root.path("content");

            System.out.println("=== CLAUDE'S TEXT CONTENT ===");
           // System.out.println(claudeText);

            String claudeText = null;
            for(JsonNode block : contentArray){
                if("text".equals(block.path("type").asText())){
                    claudeText = block.path("text").asText();
                    break;
                }
            }

            if(claudeText == null){
                throw new IllegalStateException("No text block found in claude's response");
            }

            String cleanedJson = stripMarkdownFences(claudeText);

            System.out.println("=== CLEANED JSON ===");
            System.out.println(cleanedJson);

            return objectMapper.readTree(cleanedJson);
        } catch (Exception e) {
            System.out.println("=== PARSE EXCEPTION ===");
            e.printStackTrace();
            throw new RuntimeException("Failed to parse Claude's response: " + e.getMessage(), e);
        }
    }

    private String stripMarkdownFences(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(json)?\\s*", "");
            trimmed = trimmed.replaceFirst("```\\s*$", "");
        }
        return trimmed.trim();
    }
}