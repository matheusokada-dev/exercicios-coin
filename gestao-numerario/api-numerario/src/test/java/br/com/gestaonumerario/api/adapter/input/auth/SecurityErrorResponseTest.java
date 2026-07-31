package br.com.gestaonumerario.api.adapter.input.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityErrorResponseTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveRetornarJsonPadronizadoQuandoAutenticacaoForNecessaria() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.codError").value(1003));
    }

    @Test
    void devePublicarContratoOpenApiComOperacoesEExemplos() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("API Gestão de Numerário"))
                .andExpect(
                        jsonPath("$.paths['/api/v1/solicitacoes-numerario'].post.summary")
                                .value("Criar solicitação de numerário")
                )
                .andExpect(
                        jsonPath(
                                "$.paths['/api/v1/solicitacoes-numerario'].post.requestBody.content['application/json'].example"
                        ).exists()
                )
                .andExpect(
                        jsonPath("$.paths['/api/v1/solicitacoes-numerario/{id}/receber'].put.summary")
                                .value("Confirmar recebimento")
                );
    }

    @Test
    void deveRetornarJsonPadronizadoQuandoPerfilNaoTiverPermissao() throws Exception {
        mockMvc.perform(get("/api/v1/solicitacoes-numerario").with(user("operador").roles("OPERADOR")))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.codError").value(1004));
    }

    @Test
    void deveRetornarJsonPadronizadoQuandoTokenForInvalido() throws Exception {
        mockMvc.perform(
                get("/api/v1/dashboard").header(
                        "Authorization",
                        "Bearer token-invalido"
                )
        )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.codError").value(1002));
    }

    @Test
    void deveConsultarTodosOsRecursosLegadosAutorizados() throws Exception {
        var gestor = user("gestor").roles("GESTOR");

        mockMvc.perform(get("/api/v1/dashboard").with(gestor))
                .andExpect(status().isOk());
        mockMvc.perform(
                get("/api/v1/agencias").with(gestor)
                        .param(
                                "pagina",
                                "0"
                        )
                        .param(
                                "tamanho",
                                "10"
                        )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens").isArray());
        mockMvc.perform(
                get("/api/v1/movimentacoes").with(gestor)
                        .param(
                                "pagina",
                                "0"
                        )
                        .param(
                                "tamanho",
                                "10"
                        )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens").isArray());
        mockMvc.perform(
                get("/api/v1/solicitacoes").with(gestor)
                        .param(
                                "pagina",
                                "0"
                        )
                        .param(
                                "tamanho",
                                "10"
                        )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens").isArray());
    }

    @Test
    void deveConsultarTodosOsRecursosDeNumerarioAutorizados() throws Exception {
        var gestor = user("gestor").roles("GESTOR");

        mockMvc.perform(get("/api/v1/unidades-operacionais").with(gestor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        mockMvc.perform(
                get("/api/v1/operacoes-numerario").with(gestor)
                        .param(
                                "pagina",
                                "0"
                        )
                        .param(
                                "tamanho",
                                "10"
                        )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens").isArray());
        mockMvc.perform(
                get("/api/v1/solicitacoes-numerario").with(gestor)
                        .param(
                                "pagina",
                                "0"
                        )
                        .param(
                                "tamanho",
                                "10"
                        )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens").isArray());
    }
}
