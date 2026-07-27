package br.com.gestaonumerario.api.config;

import br.com.gestaonumerario.api.adapter.input.auth.JwtAuthenticationFilter;
import br.com.gestaonumerario.api.adapter.input.error.SecurityErrorWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private static final int AUTENTICACAO_NECESSARIA = 1003;
    private static final int ACESSO_NEGADO = 1004;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityErrorWriter securityErrorWriter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                securityErrorWriter.escrever(
                                        request, response, 401, AUTENTICACAO_NECESSARIA,
                                        "Autenticação necessária."
                                ))
                        .accessDeniedHandler((request, response, exception) ->
                                securityErrorWriter.escrever(
                                        request, response, 403, ACESSO_NEGADO,
                                        "Acesso negado."
                                )))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**",
                                "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/refresh",
                                "/api/v1/auth/logout").permitAll()
                        .requestMatchers(
                                "/api/v1/solicitacoes-numerario/**",
                                "/api/v1/unidades-operacionais/**",
                                "/api/v1/operacoes-numerario/**",
                                "/api/v1/tesouraria/**"
                        ).hasRole("GESTOR")
                        .requestMatchers("/api/v1/agencias/**", "/api/v1/usuarios/**").hasRole("GESTOR")
                        .requestMatchers("/api/v1/solicitacoes/*/aprovar", "/api/v1/solicitacoes/*/rejeitar").hasRole("GESTOR")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
