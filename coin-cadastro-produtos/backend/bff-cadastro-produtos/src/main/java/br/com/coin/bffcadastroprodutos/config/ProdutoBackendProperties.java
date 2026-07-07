package br.com.coin.bffcadastroprodutos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "backend.produtos")
public record ProdutoBackendProperties(
        String baseUrl,
        Integer timeoutSegundos
) {
}
