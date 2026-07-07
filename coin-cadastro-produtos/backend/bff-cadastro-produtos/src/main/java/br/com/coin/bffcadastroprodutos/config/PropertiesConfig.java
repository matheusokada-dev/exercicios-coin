package br.com.coin.bffcadastroprodutos.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ProdutoBackendProperties.class)
public class PropertiesConfig {
}