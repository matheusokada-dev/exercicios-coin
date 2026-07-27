package br.com.gestaonumerario.bff.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI bffNumerarioOpenApi() {
        return new OpenAPI().info(new Info()
                .title("BFF Gestão de Numerário")
                .version("v1")
                .description("""
                        Contrato HTTP consumido pelo frontend Angular.
                        A autenticação utiliza JWT no header Authorization: Bearer.
                        """)
                .contact(new Contact().name("Equipe Gestão de Numerário")));
    }
}
