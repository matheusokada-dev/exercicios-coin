package br.com.gestaonumerario.bff.client;

import br.com.gestaonumerario.bff.dto.AjustarDivergenciaRequest;
import br.com.gestaonumerario.bff.dto.AprovarSolicitacaoRequest;
import br.com.gestaonumerario.bff.dto.AtenderSolicitacaoRequest;
import br.com.gestaonumerario.bff.dto.AtualizarAgenciaRequest;
import br.com.gestaonumerario.bff.dto.ConciliarOperacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.CriarAgenciaRequest;
import br.com.gestaonumerario.bff.dto.CriarSolicitacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.DecidirSolicitacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.ExecutarOperacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.LoginRequest;
import br.com.gestaonumerario.bff.dto.OcorrenciaOperacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.OperacaoNumerarioResponse;
import br.com.gestaonumerario.bff.dto.PaginaResponse;
import br.com.gestaonumerario.bff.dto.ReceberOperacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.RejeitarSolicitacaoRequest;
import br.com.gestaonumerario.bff.dto.SolicitarAbastecimentoRequest;
import br.com.gestaonumerario.bff.dto.UnidadeOperacionalResponse;
import br.com.gestaonumerario.bff.dto.VersaoOperacaoNumerarioRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ApiNumerarioClientTest {

    private MockRestServiceServer server;
    private ApiNumerarioClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://api-numerario");
        server = MockRestServiceServer.bindTo(builder)
                .build();
        client = new ApiNumerarioClient(builder.build());
    }

    @Test
    void encaminhaJwtEFiltrosParaApi() {
        server.expect(
                once(),
                requestTo(
                        "http://api-numerario/api/v1/agencias?busca=centro&alerta=true"
                                + "&ordenarPor=saldoAtual&direcao=desc&pagina=1&tamanho=10"
                )
        )
                .andExpect(
                        header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer token"
                        )
                )
                .andRespond(
                        withSuccess(
                                """
                                        {"itens":[],"pagina":1,"tamanho":10,"totalItens":0,"totalPaginas":0}
                                        """,
                                MediaType.APPLICATION_JSON
                        )
                );

        var response = client.listarAgencias(
                "Bearer token",
                "centro",
                null,
                true,
                "saldoAtual",
                "desc",
                1,
                10
        );

        assertThat(response.pagina()).isEqualTo(1);
        assertThat(response.itens()).isEmpty();
        server.verify();
    }

    @Test
    void encaminhaConsultaDeNumerarioComTodosOsFiltros() {
        server.expect(
                once(),
                requestTo(
                        "http://api-numerario/api/v1/solicitacoes-numerario"
                                + "?agenciaId=7&tipo=RECOLHIMENTO&status=EM_EXECUCAO&origemId=8"
                                + "&destinoId=9&dataInicio=2026-07-01&dataFim=2026-07-31" + "&pagina=2&tamanho=25"
                )
        )
                .andExpect(
                        header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer gestor"
                        )
                )
                .andRespond(
                        withSuccess(
                                """
                                        {"itens":[],"pagina":2,"tamanho":25,"totalItens":0,"totalPaginas":0}
                                        """,
                                MediaType.APPLICATION_JSON
                        )
                );

        var response = client.listarSolicitacoesNumerario(
                "Bearer gestor",
                7L,
                "RECOLHIMENTO",
                "EM_EXECUCAO",
                8L,
                9L,
                java.time.LocalDate.of(
                        2026,
                        7,
                        1
                ),
                java.time.LocalDate.of(
                        2026,
                        7,
                        31
                ),
                2,
                25
        );

        assertThat(response.pagina()).isEqualTo(2);
        server.verify();
    }

    @Test
    void encaminhaConsultaDeUnidadesOperacionais() {
        server.expect(
                once(),
                requestTo("http://api-numerario/api/v1/unidades-operacionais?tipo=TESOURARIA")
        )
                .andExpect(
                        header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer gestor"
                        )
                )
                .andRespond(
                        withSuccess(
                                """
                                        [{"id":32,"tipo":"TESOURARIA","codigo":"TES-CENTRAL",
                                          "nome":"Tesouraria Central","saldoAtual":0.00,
                                          "versao":0,"atualizadoEm":"2026-07-24T12:00:00Z"}]
                                        """,
                                MediaType.APPLICATION_JSON
                        )
                );

        var response = client.listarUnidadesOperacionais(
                "Bearer gestor",
                "TESOURARIA"
        );

        assertThat(response).hasSize(1);
        assertThat(
                response.getFirst()
                        .codigo()
        ).isEqualTo("TES-CENTRAL");
        server.verify();
    }

    @Test
    void encaminhaJwtEIdempotenciaNosComandosLogisticos() {
        server.expect(
                once(),
                requestTo("http://api-numerario/api/v1/solicitacoes-numerario/15/programar")
        )
                .andExpect(method(PUT))
                .andExpect(
                        header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer gestor"
                        )
                )
                .andExpect(
                        header(
                                "Idempotency-Key",
                                "prog-15"
                        )
                )
                .andRespond(
                        withSuccess(
                                """
                                        {"id":2,"solicitacaoId":15,"origemId":32,"destinoId":4,
                                         "status":"PROGRAMADA","valorProgramado":100.00,
                                         "dataProgramacao":"2026-07-24T12:00:00Z","versao":0}
                                        """,
                                MediaType.APPLICATION_JSON
                        )
                );

        var response = client.programarOperacao(
                "Bearer gestor",
                15L,
                "prog-15",
                new br.com.gestaonumerario.bff.dto.ProgramarOperacaoNumerarioRequest(
                        32L,
                        1
                )
        );

        assertThat(response.status()).isEqualTo("PROGRAMADA");
        server.verify();
    }

    @Test
    void encaminhaCargaInicialComIdempotencia() {
        server.expect(
                once(),
                requestTo("http://api-numerario/api/v1/tesouraria/carga-inicial")
        )
                .andExpect(method(POST))
                .andExpect(
                        header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer gestor"
                        )
                )
                .andExpect(
                        header(
                                "Idempotency-Key",
                                "carga-1"
                        )
                )
                .andRespond(
                        withSuccess(
                                """
                                        {"id":32,"tipo":"TESOURARIA","codigo":"TES-CENTRAL",
                                         "nome":"Tesouraria Central","saldoAtual":5000.00,
                                         "versao":1,"atualizadoEm":"2026-07-24T12:00:00Z"}
                                        """,
                                MediaType.APPLICATION_JSON
                        )
                );

        var response = client.realizarCargaInicial(
                "Bearer gestor",
                "carga-1",
                new br.com.gestaonumerario.bff.dto.CargaInicialTesourariaRequest(
                        new java.math.BigDecimal("5000.00"),
                        "Carga de implantação",
                        0
                )
        );

        assertThat(response.saldoAtual()).isEqualByComparingTo("5000.00");
        server.verify();
    }

    @Test
    void deveEncaminharAutenticacaoSessaoEDashboard() {
        esperar(
                "/api/v1/auth/login",
                POST,
                "{}"
        );
        esperar(
                "/api/v1/auth/me",
                HttpMethod.GET,
                "{}"
        );
        esperar(
                "/api/v1/dashboard",
                HttpMethod.GET,
                "{}"
        );

        client.autenticar(mock(LoginRequest.class));
        client.consultarSessao("Bearer token");
        client.consultarDashboard("Bearer token");
        server.verify();
    }

    @Test
    void deveEncaminharOperacoesDeAgencia() {
        esperar(
                "/api/v1/agencias/1/detalhe",
                HttpMethod.GET,
                "{}"
        );
        esperar(
                "/api/v1/agencias",
                POST,
                "{}"
        );
        esperar(
                "/api/v1/agencias/1",
                PUT,
                "{}"
        );
        esperarSemCorpo(
                "/api/v1/agencias/1",
                HttpMethod.DELETE
        );
        esperar(
                "/api/v1/agencias",
                POST,
                "{}"
        );

        client.detalharAgencia(
                "Bearer token",
                1L
        );
        client.criarAgencia(
                "Bearer token",
                mock(CriarAgenciaRequest.class)
        );
        client.atualizarAgencia(
                "Bearer token",
                1L,
                mock(AtualizarAgenciaRequest.class)
        );
        client.desativarAgencia(
                "Bearer token",
                1L
        );
        client.criarAgenciaOperacional(
                "Bearer token",
                mock(CriarAgenciaRequest.class)
        );
        server.verify();
    }

    @Test
    void deveEncaminharOperacoesLegadasDeSolicitacaoEMovimentacao() {
        esperar(
                "/api/v1/solicitacoes?pagina=0&tamanho=20",
                HttpMethod.GET,
                "{\"itens\":[],\"pagina\":0,\"tamanho\":20,\"totalItens\":0,\"totalPaginas\":0}"
        );
        esperar(
                "/api/v1/solicitacoes",
                POST,
                "{}"
        );
        esperar(
                "/api/v1/solicitacoes/1/aprovar",
                PUT,
                "{}"
        );
        esperar(
                "/api/v1/solicitacoes/1/rejeitar",
                PUT,
                "{}"
        );
        esperar(
                "/api/v1/solicitacoes/1/atender",
                PUT,
                "{}"
        );
        esperar(
                "/api/v1/movimentacoes?pagina=0&tamanho=20",
                HttpMethod.GET,
                "{\"itens\":[],\"pagina\":0,\"tamanho\":20,\"totalItens\":0,\"totalPaginas\":0}"
        );

        client.listarSolicitacoes(
                "Bearer token",
                null,
                null,
                null,
                null,
                0,
                20
        );
        client.criarSolicitacao(
                "Bearer token",
                mock(SolicitarAbastecimentoRequest.class)
        );
        client.aprovarSolicitacao(
                "Bearer token",
                1L,
                mock(AprovarSolicitacaoRequest.class)
        );
        client.rejeitarSolicitacao(
                "Bearer token",
                1L,
                mock(RejeitarSolicitacaoRequest.class)
        );
        client.atenderSolicitacao(
                "Bearer token",
                1L,
                mock(AtenderSolicitacaoRequest.class)
        );
        client.listarMovimentacoes(
                "Bearer token",
                null,
                null,
                null,
                null,
                0,
                20
        );
        server.verify();
    }

    @Test
    void deveEncaminharConsultasEComandosDeNumerario() {
        esperar(
                "/api/v1/solicitacoes-numerario/1",
                HttpMethod.GET,
                "{}"
        );
        esperar(
                "/api/v1/solicitacoes-numerario/1/historico",
                HttpMethod.GET,
                "[]"
        );
        esperar(
                "/api/v1/operacoes-numerario?pagina=0&tamanho=20",
                HttpMethod.GET,
                "{\"itens\":[],\"pagina\":0,\"tamanho\":20,\"totalItens\":0,\"totalPaginas\":0}"
        );
        esperar(
                "/api/v1/solicitacoes-numerario",
                POST,
                "{}"
        );
        esperar(
                "/api/v1/solicitacoes-numerario/1/aprovar",
                PUT,
                "{}"
        );
        esperar(
                "/api/v1/solicitacoes-numerario/1/iniciar-separacao",
                PUT,
                "{}"
        );
        esperarComIdempotencia(
                "/api/v1/solicitacoes-numerario/1/expedir",
                PUT,
                "exp-1",
                "{}"
        );
        esperar(
                "/api/v1/solicitacoes-numerario/1/registrar-ocorrencia",
                PUT,
                "{}"
        );
        esperarComIdempotencia(
                "/api/v1/solicitacoes-numerario/1/receber",
                PUT,
                "rec-1",
                "{}"
        );
        esperarComIdempotencia(
                "/api/v1/solicitacoes-numerario/1/conciliar",
                PUT,
                "con-1",
                "{}"
        );
        esperarComIdempotencia(
                "/api/v1/solicitacoes-numerario/1/ajustes-divergencia",
                POST,
                "aju-1",
                "{}"
        );

        client.detalharSolicitacaoNumerario(
                "Bearer token",
                1L
        );
        client.consultarHistoricoSolicitacao(
                "Bearer token",
                1L
        );
        client.listarOperacoesNumerario(
                "Bearer token",
                null,
                null,
                null,
                null,
                null,
                0,
                20
        );
        client.criarSolicitacaoNumerario(
                "Bearer token",
                mock(CriarSolicitacaoNumerarioRequest.class)
        );
        client.decidirSolicitacaoNumerario(
                "Bearer token",
                1L,
                "aprovar",
                mock(DecidirSolicitacaoNumerarioRequest.class)
        );
        client.iniciarSeparacao(
                "Bearer token",
                1L,
                mock(VersaoOperacaoNumerarioRequest.class)
        );
        client.expedirOperacao(
                "Bearer token",
                1L,
                "exp-1",
                mock(ExecutarOperacaoNumerarioRequest.class)
        );
        client.registrarOcorrencia(
                "Bearer token",
                1L,
                mock(OcorrenciaOperacaoNumerarioRequest.class)
        );
        client.receberOperacao(
                "Bearer token",
                1L,
                "rec-1",
                mock(ReceberOperacaoNumerarioRequest.class)
        );
        client.conciliarOperacao(
                "Bearer token",
                1L,
                "con-1",
                mock(ConciliarOperacaoNumerarioRequest.class)
        );
        client.ajustarDivergencia(
                "Bearer token",
                1L,
                "aju-1",
                mock(AjustarDivergenciaRequest.class)
        );
        server.verify();
    }

    private void esperar(String path, HttpMethod httpMethod, String response) {
        server.expect(
                once(),
                requestTo("http://api-numerario" + path)
        )
                .andExpect(method(httpMethod))
                .andRespond(
                        withSuccess(
                                response,
                                MediaType.APPLICATION_JSON
                        )
                );
    }

    private void esperarComIdempotencia(String path, HttpMethod httpMethod, String key, String response) {
        server.expect(
                once(),
                requestTo("http://api-numerario" + path)
        )
                .andExpect(method(httpMethod))
                .andExpect(
                        header(
                                "Idempotency-Key",
                                key
                        )
                )
                .andRespond(
                        withSuccess(
                                response,
                                MediaType.APPLICATION_JSON
                        )
                );
    }

    private void esperarSemCorpo(String path, HttpMethod httpMethod) {
        server.expect(
                once(),
                requestTo("http://api-numerario" + path)
        )
                .andExpect(method(httpMethod))
                .andRespond(withSuccess());
    }
}
