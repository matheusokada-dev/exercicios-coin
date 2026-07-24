package br.com.gestaonumerario.bff.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "api-numerario")
public record ApiNumerarioProperties(
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout
) {
}
