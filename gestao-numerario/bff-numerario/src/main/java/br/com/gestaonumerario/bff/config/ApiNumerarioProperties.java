package br.com.gestaonumerario.bff.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "api-numerario")
public record ApiNumerarioProperties(String baseUrl, Duration connectTimeout, Duration readTimeout) {
}
