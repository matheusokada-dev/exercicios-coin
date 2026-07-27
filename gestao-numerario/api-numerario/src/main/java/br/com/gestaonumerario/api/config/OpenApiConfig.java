package br.com.gestaonumerario.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String JWT = "bearerAuth";

    @Bean
    OpenAPI apiNumerarioOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Gestão de Numerário")
                        .version("v1")
                        .description("""
                                API de domínio para agências, movimentações e operações de numerário.
                                Todos os contratos, legados e evoluídos, usam o prefixo /api/v1.
                                """)
                        .contact(new Contact().name("Equipe Gestão de Numerário")))
                .components(new Components().addSecuritySchemes(
                        JWT,
                        new SecurityScheme()
                                .name(JWT)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(JWT));
    }
}
