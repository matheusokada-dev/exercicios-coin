package br.com.gestaonumerario.api.adapter.input.controller;

import br.com.gestaonumerario.api.adapter.input.auth.UsuarioAutenticado;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.*;
import br.com.gestaonumerario.api.adapter.input.mapper.*;
import br.com.gestaonumerario.api.core.domain.enums.PerfilUsuario;
import br.com.gestaonumerario.api.core.domain.model.SessaoAutenticacao;
import br.com.gestaonumerario.api.core.domain.model.UnidadeOperacional;
import br.com.gestaonumerario.api.core.domain.model.Usuario;
import br.com.gestaonumerario.api.core.domain.model.Pagina;
import br.com.gestaonumerario.api.core.exception.CredenciaisInvalidasException;
import br.com.gestaonumerario.api.port.input.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CommandControllersTest {

    private static final UsuarioAutenticado GESTOR =
            new UsuarioAutenticado(7L, "gestor", PerfilUsuario.GESTOR);

    @Test
    void operacaoDelegaTodasAsTransicoesComUsuarioEIdempotencia() {
        var useCase = mock(OperacaoNumerarioInputPort.class);
        var mapper = mock(SolicitacaoNumerarioRestMapper.class);
        var controller = new OperacaoNumerarioController(useCase, mapper);

        controller.programar(1L, mock(ProgramarOperacaoNumerarioRequest.class), "p-1", GESTOR);
        controller.separar(1L, mock(VersaoOperacaoNumerarioRequest.class), GESTOR);
        controller.expedir(1L, mock(ExecutarOperacaoNumerarioRequest.class), "e-1", GESTOR);
        controller.ocorrencia(1L, mock(OcorrenciaOperacaoNumerarioRequest.class), GESTOR);
        controller.receber(1L, mock(ReceberOperacaoNumerarioRequest.class), "r-1", GESTOR);
        controller.conciliar(1L, mock(ConciliarOperacaoNumerarioRequest.class), "c-1", GESTOR);

        verify(useCase).programar(any());
        verify(useCase).iniciarSeparacao(any());
        verify(useCase).expedir(any());
        verify(useCase).registrarOcorrencia(any());
        verify(useCase).receber(any());
        verify(useCase).conciliar(any());
    }

    @Test
    void financeiroDelegaCargaInicialEAjuste() {
        var useCase = mock(FinanceiroNumerarioInputPort.class);
        var unidade = mock(UnidadeOperacional.class);
        when(useCase.realizarCargaInicial(any())).thenReturn(unidade);
        when(useCase.ajustarDivergencia(any())).thenReturn(unidade);
        var controller = new FinanceiroNumerarioController(useCase);

        controller.cargaInicial(mock(CargaInicialTesourariaRequest.class), "carga-1", GESTOR);
        controller.ajustar(1L, mock(AjustarDivergenciaRequest.class), "ajuste-1", GESTOR);

        verify(useCase).realizarCargaInicial(any());
        verify(useCase).ajustarDivergencia(any());
    }

    @Test
    void autenticacaoMapeiaLoginRefreshLogoutESessao() {
        var autenticacao = mock(AutenticarInputPort.class);
        var usuarios = mock(UsuarioInputPort.class);
        Instant agora = Instant.parse("2026-07-25T12:00:00Z");
        var sessao = new SessaoAutenticacao(
                "access", agora.plusSeconds(60), "refresh", agora.plusSeconds(120),
                7L, "Gestor", "GESTOR");
        when(autenticacao.autenticar(any())).thenReturn(sessao);
        when(autenticacao.renovar("refresh")).thenReturn(sessao);
        var usuario = new Usuario(7L, "Gestor", "gestor", "hash",
                PerfilUsuario.GESTOR, true, agora, 0, null);
        when(usuarios.buscarPorId(7L)).thenReturn(usuario);
        var controller = new AuthController(autenticacao, usuarios);

        var login = controller.login(new AuthController.LoginRequest("gestor", "senha"));
        var refresh = controller.refresh(new AuthController.RefreshRequest("refresh"));
        controller.logout(new AuthController.RefreshRequest("refresh"));
        var atual = controller.me(GESTOR);

        assertThat(login.accessToken()).isEqualTo("access");
        assertThat(login.tokenType()).isEqualTo("Bearer");
        assertThat(refresh.refreshToken()).isEqualTo("refresh");
        assertThat(atual.usuarioId()).isEqualTo(7L);
        verify(autenticacao).encerrar("refresh");

        usuario.desativar();
        assertThatThrownBy(() -> controller.me(GESTOR))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void solicitacaoNumerarioDelegaCriacaoConsultaDetalheEDecisoes() {
        var useCase = mock(SolicitacaoNumerarioInputPort.class);
        var mapper = mock(SolicitacaoNumerarioRestMapper.class);
        when(useCase.consultar(any())).thenReturn(new Pagina<>(List.of(), 0, 20, 0, 0));
        var controller = new SolicitacaoNumerarioController(useCase, mapper);

        assertThat(controller.criar(mock(CriarSolicitacaoNumerarioRequest.class), GESTOR)
                .getStatusCode().value()).isEqualTo(201);
        assertThat(controller.consultar(null, null, null, null, null,
                null, null, 0, 20).itens()).isEmpty();
        controller.detalhar(1L);
        controller.aprovar(1L, mock(DecidirSolicitacaoNumerarioRequest.class), GESTOR);
        controller.rejeitar(1L, mock(DecidirSolicitacaoNumerarioRequest.class), GESTOR);
        controller.cancelar(1L, mock(DecidirSolicitacaoNumerarioRequest.class), GESTOR);

        verify(useCase).criar(any());
        verify(useCase).consultar(any());
        verify(useCase).detalhar(1L);
        verify(useCase).aprovar(any());
        verify(useCase).rejeitar(any());
        verify(useCase).cancelar(any());
    }

    @Test
    void endpointsExistentesDeEscritaPermanecemAtivosNaV1() {
        var agenciaPort = mock(AgenciaInputPort.class);
        var agenciaMapper = mock(AgenciaRestMapper.class);
        var agenciaController = new AgenciaController(
                agenciaPort, agenciaPort, agenciaPort, agenciaPort, agenciaPort,
                agenciaMapper);
        var solicitacaoPort = mock(SolicitacaoInputPort.class);
        var solicitacaoMapper = mock(SolicitacaoAbastecimentoRestMapper.class);
        var solicitacaoController = new SolicitacaoAbastecimentoController(
                solicitacaoPort, solicitacaoPort, solicitacaoPort, solicitacaoPort,
                solicitacaoPort, solicitacaoMapper);
        var movimentacaoPort = mock(MovimentacaoInputPort.class);
        var movimentacaoController = new MovimentacaoController(
                movimentacaoPort, movimentacaoPort, mock(MovimentacaoRestMapper.class));
        var usuarioPort = mock(UsuarioInputPort.class);
        var usuarioController = new UsuarioController(
                usuarioPort, usuarioPort, mock(UsuarioRestMapper.class));

        agenciaController.criar(mock(CriarAgenciaRequest.class));
        agenciaController.atualizar(1L, mock(AtualizarAgenciaRequest.class));
        agenciaController.desativar(1L);
        solicitacaoController.solicitar(mock(SolicitarAbastecimentoRequest.class), GESTOR);
        solicitacaoController.aprovar(1L, mock(AprovarSolicitacaoRequest.class), GESTOR);
        solicitacaoController.rejeitar(1L, mock(RejeitarSolicitacaoRequest.class), GESTOR);
        solicitacaoController.atender(1L, mock(AtenderSolicitacaoRequest.class), GESTOR);
        movimentacaoController.registrar(mock(RegistrarMovimentacaoRequest.class), GESTOR);
        usuarioController.criar(mock(CriarUsuarioRequest.class));

        verify(agenciaPort).criar(any());
        verify(agenciaPort).atualizar(any());
        verify(agenciaPort).desativar(1L);
        verify(solicitacaoPort).solicitar(any());
        verify(solicitacaoPort).aprovar(any());
        verify(solicitacaoPort).rejeitar(any());
        verify(solicitacaoPort).atender(any());
        verify(movimentacaoPort).registrar(any());
        verify(usuarioPort).criar(any());
    }

    @Test
    void mappersDeEntradaConvertemTodosOsComandos() {
        var agencia = new AgenciaRestMapper();
        var solicitacao = new SolicitacaoAbastecimentoRestMapper();
        var movimentacao = new MovimentacaoRestMapper();
        var usuario = new UsuarioRestMapper();

        assertThat(agencia.toCommand(mock(CriarAgenciaRequest.class))).isNotNull();
        assertThat(agencia.toCommand(1L, mock(AtualizarAgenciaRequest.class))).isNotNull();
        assertThat(solicitacao.toCommand(mock(SolicitarAbastecimentoRequest.class), GESTOR)).isNotNull();
        assertThat(solicitacao.toCommand(1L, mock(AprovarSolicitacaoRequest.class), GESTOR)).isNotNull();
        assertThat(solicitacao.toCommand(1L, mock(RejeitarSolicitacaoRequest.class), GESTOR)).isNotNull();
        assertThat(solicitacao.toCommand(1L, mock(AtenderSolicitacaoRequest.class), GESTOR)).isNotNull();
        assertThat(movimentacao.toCommand(mock(RegistrarMovimentacaoRequest.class), GESTOR)).isNotNull();
        assertThat(usuario.toCommand(mock(CriarUsuarioRequest.class))).isNotNull();
    }
}
