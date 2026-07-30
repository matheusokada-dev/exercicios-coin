package br.com.gestaonumerario.bff.controller;

import br.com.gestaonumerario.bff.exception.BffExceptionHandler;
import br.com.gestaonumerario.bff.service.SolicitacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SolicitacaoControllerTest {

    private SolicitacaoService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(SolicitacaoService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new SolicitacaoController(service))
                .setControllerAdvice(new BffExceptionHandler())
                .build();
    }

    @Test
    void criarRetornaStatusCreated() throws Exception {
        mockMvc.perform(
                post("/api/v1/solicitacoes").header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer token"
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"agenciaId":1,"valor":1000.00,"motivo":"Reposicao","dataDesejada":"2099-01-01"}
                                """)
        )
                .andExpect(status().isCreated());

        verify(service).criar(
                anyString(),
                any()
        );
    }

    @Test
    void rejeitaDecisaoSemJustificativaAntesDeChamarService() throws Exception {
        mockMvc.perform(
                put("/api/v1/solicitacoes/1/rejeitar").header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer token"
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"justificativaDecisao\":\"\"}")
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codError").value(1000))
                .andExpect(jsonPath("$.value").value("justificativaDecisao"));
    }

    @Test
    void naoAceitaAcaoGenerica() throws Exception {
        mockMvc.perform(
                put("/api/v1/solicitacoes/1/acao-inexistente").header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer token"
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
        )
                .andExpect(status().isNotFound());
    }
}
