package br.com.coin.cadastroprodutos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cadastroProdutosOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Coin Cadastro de Produtos - Backend")
                        .description("Documentação da API de domínio de cadastro de produtos.")
                        .version("v1"));
    }
}
