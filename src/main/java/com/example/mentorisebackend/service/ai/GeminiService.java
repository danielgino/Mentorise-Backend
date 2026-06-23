package com.example.mentorisebackend.service.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.url}")
    private String geminiUrl;

    public String generate(String prompt) {
        Map<String, Object> request = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("responseMimeType", "application/json")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        String url = geminiUrl + "?key=" + apiKey;
        log.debug("Gemini URL: {}", geminiUrl);
        GeminiResponse response = restTemplate.postForObject(url, entity, GeminiResponse.class);

        if (response == null || response.candidates == null || response.candidates.isEmpty()) {
            throw new RuntimeException("Empty response from Gemini API");
        }

        return response.candidates.get(0).content.parts.get(0).text;
    }

    public <T> T generateStructured(String prompt, Class<T> responseType) {
        try {
            String rawText = generate(prompt);
            return objectMapper.readValue(rawText, responseType);
        } catch (HttpServerErrorException e) {
            if (e.getStatusCode().value() == 503) {
                log.warn("Gemini unavailable, using fallback tutor match reasons");
            } else {
                log.warn("Gemini request failed (HTTP {}), using fallback tutor match reasons", e.getStatusCode().value());
            }
            log.debug("Gemini error details: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("Gemini response parsing failed ({}), using fallback tutor match reasons", e.getClass().getSimpleName());
            log.debug("Gemini error details: {}", e.getMessage());
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GeminiResponse {
        public List<Candidate> candidates;

        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Candidate {
            public Content content;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Content {
            public List<Part> parts;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Part {
            public String text;
        }
    }
}
