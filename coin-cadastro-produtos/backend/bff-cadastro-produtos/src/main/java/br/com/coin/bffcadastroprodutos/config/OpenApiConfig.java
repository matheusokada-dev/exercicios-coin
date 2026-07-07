package br.com.coin.bffcadastroprodutos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bffCadastroProdutosOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Coin Cadastro de Produtos - BFF")
                        .description("Documentação da camada BFF consumida pelo frontend de cadastro de produtos.")
                        .version("v1"));
    }
}
