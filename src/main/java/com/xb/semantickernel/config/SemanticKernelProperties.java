package com.xb.semantickernel.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "semantickernel")
public record SemanticKernelProperties(
        String apiKey,
        String model,
        String endpoint,
        Double temperature) {
}