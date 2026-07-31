package br.com.gestaonumerario.bff.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "relatorios")
public record RelatoriosProperties(String baseUrl, Duration connectTimeout, Duration readTimeout) {
}
