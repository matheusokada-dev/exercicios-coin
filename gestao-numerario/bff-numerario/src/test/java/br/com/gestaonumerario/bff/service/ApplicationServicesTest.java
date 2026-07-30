package br.com.gestaonumerario.bff.service;

import br.com.gestaonumerario.bff.client.ApiNumerarioClient;
import br.com.gestaonumerario.bff.dto.AprovarSolicitacaoRequest;
import br.com.gestaonumerario.bff.dto.AtenderSolicitacaoRequest;
import br.com.gestaonumerario.bff.dto.AtualizarAgenciaRequest;
import br.com.gestaonumerario.bff.dto.CriarAgenciaRequest;
import br.com.gestaonumerario.bff.dto.LoginRequest;
import br.com.gestaonumerario.bff.dto.RejeitarSolicitacaoRequest;
import br.com.gestaonumerario.bff.dto.SolicitarAbastecimentoRequest;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ApplicationServicesTest {
    private final ApiNumerarioClient client = mock(ApiNumerarioClient.class);

    @Test
    void deveDelegarServicosDeAgencia() {
        AgenciaService service = new AgenciaService(client);
        CriarAgenciaRequest criar = mock(CriarAgenciaRequest.class);
        AtualizarAgenciaRequest atualizar = mock(AtualizarAgenciaRequest.class);

        service.listar(
                "Bearer token",
                "centro",
                true,
                false,
                "codigo",
                "asc",
                0,
                20
        );
        service.detalhar(
                "Bearer token",
                1L
        );
        service.criar(
                "Bearer token",
                criar
        );
        service.atualizar(
                "Bearer token",
                1L,
                atualizar
        );
        service.desativar(
                "Bearer token",
                1L
        );

        verify(client).listarAgencias(
                "Bearer token",
                "centro",
                true,
                false,
                "codigo",
                "asc",
                0,
                20
        );
        verify(client).detalharAgencia(
                "Bearer token",
                1L
        );
        verify(client).criarAgencia(
                "Bearer token",
                criar
        );
        verify(client).atualizarAgencia(
                "Bearer token",
                1L,
                atualizar
        );
        verify(client).desativarAgencia(
                "Bearer token",
                1L
        );
    }

    @Test
    void deveDelegarServicosDeAutenticacao() {
        AuthService service = new AuthService(client);
        LoginRequest login = mock(LoginRequest.class);

        service.autenticar(login);
        service.consultarSessao("Bearer token");

        verify(client).autenticar(login);
        verify(client).consultarSessao("Bearer token");
    }

    @Test
    void deveDelegarServicosDeSolicitacao() {
        SolicitacaoService service = new SolicitacaoService(client);
        LocalDate inicio = LocalDate.of(
                2026,
                7,
                1
        );
        LocalDate fim = LocalDate.of(
                2026,
                7,
                31
        );
        SolicitarAbastecimentoRequest criar = mock(SolicitarAbastecimentoRequest.class);
        AprovarSolicitacaoRequest aprovar = mock(AprovarSolicitacaoRequest.class);
        RejeitarSolicitacaoRequest rejeitar = mock(RejeitarSolicitacaoRequest.class);
        AtenderSolicitacaoRequest atender = mock(AtenderSolicitacaoRequest.class);

        service.listar(
                "Bearer token",
                1L,
                "PENDENTE",
                inicio,
                fim,
                0,
                20
        );
        service.criar(
                "Bearer token",
                criar
        );
        service.aprovar(
                "Bearer token",
                1L,
                aprovar
        );
        service.rejeitar(
                "Bearer token",
                1L,
                rejeitar
        );
        service.atender(
                "Bearer token",
                1L,
                atender
        );

        verify(client).listarSolicitacoes(
                "Bearer token",
                1L,
                "PENDENTE",
                inicio,
                fim,
                0,
                20
        );
        verify(client).criarSolicitacao(
                "Bearer token",
                criar
        );
        verify(client).aprovarSolicitacao(
                "Bearer token",
                1L,
                aprovar
        );
        verify(client).rejeitarSolicitacao(
                "Bearer token",
                1L,
                rejeitar
        );
        verify(client).atenderSolicitacao(
                "Bearer token",
                1L,
                atender
        );
    }

    @Test
    void deveDelegarMovimentacaoEDashboard() {
        MovimentacaoService movimentacao = new MovimentacaoService(client);
        DashboardService dashboard = new DashboardService(client);
        LocalDate data = LocalDate.of(
                2026,
                7,
                25
        );

        movimentacao.listar(
                "Bearer token",
                1L,
                "DEPOSITO",
                data,
                data,
                0,
                20
        );
        dashboard.consultar("Bearer token");

        verify(client).listarMovimentacoes(
                "Bearer token",
                1L,
                "DEPOSITO",
                data,
                data,
                0,
                20
        );
        verify(client).consultarDashboard("Bearer token");
    }
}
