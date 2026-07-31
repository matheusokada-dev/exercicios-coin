package br.com.gestaonumerario.relatorios.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI relatorioNumerarioOpenApi() {
        return new OpenAPI().info(
                new Info()
                        .title("Serviço de Relatórios de Numerário")
                        .version("v1")
                        .description("""
                                Serviço interno responsável pela geração de arquivos
                                consumidos pelo BFF de Gestão de Numerário.
                                """)
                        .contact(new Contact().name("Equipe Gestão de Numerário"))
        );
    }
}
