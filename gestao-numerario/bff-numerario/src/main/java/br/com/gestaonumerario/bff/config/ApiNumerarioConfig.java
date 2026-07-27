package br.com.gestaonumerario.bff.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.slf4j.MDC;

import java.net.http.HttpClient;

@Configuration
@EnableConfigurationProperties(ApiNumerarioProperties.class)
public class ApiNumerarioConfig {

    @Bean
    RestClient apiNumerarioRestClient(ApiNumerarioProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.readTimeout());

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .requestInterceptor((request, body, execution) -> {
                    String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
                    if (correlationId != null) {
                        request.getHeaders().set(CorrelationIdFilter.HEADER, correlationId);
                    }
                    return execution.execute(request, body);
                })
                .build();
    }
}
