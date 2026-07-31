package br.com.gestaonumerario.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String JWT = "bearerAuth";

    @Bean
    OpenAPI apiNumerarioOpenApi() {
        return new OpenAPI().info(
                new Info().title("API Gestão de Numerário")
                        .version("v1")
                        .description("""
                                API de domínio para agências, movimentações e operações de numerário.
                                Todos os contratos, usam o prefixo /api/v1.
                                """)
        )
                .components(
                        new Components().addSecuritySchemes(
                                JWT,
                                new SecurityScheme().name(JWT)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList(JWT));
    }

    @Bean
    OpenApiCustomizer respostasPadraoOpenApi() {
        return openApi -> openApi.getPaths()
                .values()
                .forEach(path -> path.readOperations()
                        .forEach(operation -> {
                            descreverSucessos(operation.getResponses());
                            adicionarSeAusente(
                                    operation.getResponses(),
                                    "400",
                                    "Requisição inválida ou parâmetros inconsistentes."
                            );
                            if (operation.getSecurity() == null || !operation.getSecurity().isEmpty()) {
                                adicionarSeAusente(
                                        operation.getResponses(),
                                        "401",
                                        "Token JWT ausente, inválido ou expirado."
                                );
                                adicionarSeAusente(
                                        operation.getResponses(),
                                        "403",
                                        "Usuário autenticado sem permissão para a operação."
                                );
                            }
                            adicionarSeAusente(
                                    operation.getResponses(),
                                    "500",
                                    "Falha interna inesperada."
                            );
                        }));
    }

    private static void descreverSucessos(Map<String, ApiResponse> responses) {
        responses.forEach((codigo, response) -> {
            if ("200".equals(codigo) && "OK".equals(response.getDescription())) {
                response.setDescription("Operação realizada com sucesso.");
            } else if ("201".equals(codigo) && "Created".equals(response.getDescription())) {
                response.setDescription("Recurso criado com sucesso.");
            } else if ("204".equals(codigo) && "No Content".equals(response.getDescription())) {
                response.setDescription("Operação concluída sem conteúdo de resposta.");
            }
        });
    }

    private static void adicionarSeAusente(
            Map<String, ApiResponse> responses,
            String codigo,
            String descricao) {
        responses.putIfAbsent(codigo, new ApiResponse().description(descricao));
    }
}
