package com.english.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AIConfig {
    private String provider;
    private OpenAiConfig openai;
    private GeminiConfig gemini;

    @Data
    public static class OpenAiConfig {
        private String apiKey;
        private String model;
        private String baseUrl;
    }

    @Data
    public static class GeminiConfig {
        private String apiKey;
        private String model;
        private String baseUrl;
    }
}
