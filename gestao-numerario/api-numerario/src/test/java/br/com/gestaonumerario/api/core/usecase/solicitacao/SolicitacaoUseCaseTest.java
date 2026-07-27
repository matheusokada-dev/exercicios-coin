package br.com.gestaonumerario.api.core.usecase.solicitacao;

import br.com.gestaonumerario.api.core.domain.enums.PerfilUsuario;
import br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacao;
import br.com.gestaonumerario.api.core.domain.model.*;
import br.com.gestaonumerario.api.core.domain.model.command.*;
import br.com.gestaonumerario.api.core.exception.*;
import br.com.gestaonumerario.api.port.output.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SolicitacaoUseCaseTest {
    private AgenciaOutputPort agencias;
    private UsuarioOutputPort usuarios;
    private SolicitacaoAbastecimentoOutputPort solicitacoes;
    private MovimentacaoOutputPort movimentacoes;
    private RelogioOutputPort relogio;
    private SolicitacaoUseCase useCase;

    private final LocalDate hoje = LocalDate.of(2026, 7, 25);
    private final Instant agora = Instant.parse("2026-07-25T12:00:00Z");

    @BeforeEach
    void configurar() {
        agencias = mock(AgenciaOutputPort.class);
        usuarios = mock(UsuarioOutputPort.class);
        solicitacoes = mock(SolicitacaoAbastecimentoOutputPort.class);
        movimentacoes = mock(MovimentacaoOutputPort.class);
        relogio = mock(RelogioOutputPort.class);
        TransacaoOutputPort transacao = mock(TransacaoOutputPort.class);
        when(transacao.executar(any())).thenAnswer(invocation ->
                ((TransacaoOutputPort.AcaoTransacional<?>) invocation.getArgument(0)).executar());
        when(relogio.hoje()).thenReturn(hoje);
        when(relogio.agora()).thenReturn(agora);
        when(solicitacoes.salvar(any())).thenAnswer(i -> i.getArgument(0));
        useCase = new SolicitacaoUseCase(
                agencias, usuarios, solicitacoes, movimentacoes, relogio, transacao);
    }

    @Test
    void deveSolicitarAprovarERejeitar() {
        Agencia agencia = agencia();
        Usuario operador = usuario(1L, PerfilUsuario.OPERADOR, true);
        Usuario gestor = usuario(2L, PerfilUsuario.GESTOR, true);
        when(agencias.buscarPorId(10L)).thenReturn(Optional.of(agencia));
        when(usuarios.buscarPorId(1L)).thenReturn(Optional.of(operador));
        when(usuarios.buscarPorId(2L)).thenReturn(Optional.of(gestor));

        SolicitacaoAbastecimento criada = useCase.solicitar(new SolicitarAbastecimentoCommand(
                10L, new BigDecimal("1000"), "Reposição", hoje.plusDays(1), 1L));
        when(solicitacoes.buscarPorId(20L)).thenReturn(Optional.of(criada));
        SolicitacaoAbastecimento aprovada = useCase.aprovar(new AprovarSolicitacaoCommand(
                20L, 2L, "Necessidade confirmada", null));

        SolicitacaoAbastecimento outra = SolicitacaoAbastecimento.criar(
                agencia, new BigDecimal("2000"), "Demanda", hoje.plusDays(2), operador, hoje, agora);
        when(solicitacoes.buscarPorId(21L)).thenReturn(Optional.of(outra));
        SolicitacaoAbastecimento rejeitada = useCase.rejeitar(new RejeitarSolicitacaoCommand(
                21L, 2L, "Saldo suficiente"));

        assertThat(aprovada.getStatus()).isEqualTo(StatusSolicitacao.APROVADA);
        assertThat(rejeitada.getStatus()).isEqualTo(StatusSolicitacao.REJEITADA);
        verify(solicitacoes, times(3)).salvar(any());
    }

    @Test
    void deveAtenderERegistrarMovimentacaoIdempotente() {
        Agencia agencia = agencia();
        Usuario operador = usuario(1L, PerfilUsuario.OPERADOR, true);
        Usuario gestor = usuario(2L, PerfilUsuario.GESTOR, true);
        SolicitacaoAbastecimento aprovada = SolicitacaoAbastecimento.reconstituir(
                30L, agencia, new BigDecimal("500"), "Reposição", hoje,
                StatusSolicitacao.APROVADA, operador, gestor, "Aprovada", null,
                agora.minusSeconds(100), agora.minusSeconds(50), null, 1);
        when(solicitacoes.buscarPorId(30L)).thenReturn(Optional.of(aprovada));
        when(usuarios.buscarPorId(1L)).thenReturn(Optional.of(operador));
        when(agencias.buscarPorIdParaAtualizacao(10L)).thenReturn(Optional.of(agencia));
        when(movimentacoes.salvar(any())).thenAnswer(i -> i.getArgument(0));

        SolicitacaoAbastecimento atendida = useCase.atender(
                new AtenderSolicitacaoCommand(30L, 1L, " atender-30 "));

        assertThat(atendida.getStatus()).isEqualTo(StatusSolicitacao.ATENDIDA);
        assertThat(agencia.getSaldoAtual()).isEqualByComparingTo("1500.00");
        verify(movimentacoes).salvar(argThat(m -> m.getIdempotencyKey().equals("atender-30")));
    }

    @Test
    void deveConsultarPagina() {
        FiltroSolicitacao filtro = new FiltroSolicitacao(
                10L, null, null, null, 0, 20);
        Pagina<SolicitacaoAbastecimento> pagina = new Pagina<>(
                java.util.List.of(), 0, 20, 0, 0);
        when(solicitacoes.buscar(filtro)).thenReturn(pagina);

        assertThat(useCase.consultar(filtro)).isSameAs(pagina);
        assertThatThrownBy(() -> useCase.consultar(null))
                .isInstanceOf(CampoObrigatorioException.class);
    }

    @Test
    void deveRejeitarComandosEReferenciasInvalidas() {
        assertThatThrownBy(() -> useCase.solicitar(null))
                .isInstanceOf(CampoObrigatorioException.class);
        assertThatThrownBy(() -> useCase.aprovar(new AprovarSolicitacaoCommand(
                null, 1L, "x", null))).isInstanceOf(CampoObrigatorioException.class);
        assertThatThrownBy(() -> useCase.rejeitar(new RejeitarSolicitacaoCommand(
                1L, null, "x"))).isInstanceOf(CampoObrigatorioException.class);
        assertThatThrownBy(() -> useCase.atender(new AtenderSolicitacaoCommand(
                1L, 1L, " "))).isInstanceOf(SolicitacaoNaoEncontradaException.class);

        when(agencias.buscarPorId(10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.solicitar(new SolicitarAbastecimentoCommand(
                10L, BigDecimal.ONE, "x", hoje, 1L)))
                .isInstanceOf(AgenciaNaoEncontradaException.class);
    }

    @Test
    void deveImpedirDuplicidadeUsuarioInativoEIdempotenciaRepetida() {
        Agencia agencia = agencia();
        Usuario inativo = usuario(1L, PerfilUsuario.OPERADOR, false);
        when(agencias.buscarPorId(10L)).thenReturn(Optional.of(agencia));
        when(usuarios.buscarPorId(1L)).thenReturn(Optional.of(inativo));
        assertThatThrownBy(() -> useCase.solicitar(new SolicitarAbastecimentoCommand(
                10L, BigDecimal.TEN, "x", hoje, 1L)))
                .isInstanceOf(RegraOperacaoNumerarioException.class);

        Usuario ativo = usuario(1L, PerfilUsuario.OPERADOR, true);
        when(usuarios.buscarPorId(1L)).thenReturn(Optional.of(ativo));
        when(solicitacoes.existeSolicitacaoAbertaParaAgencia(10L)).thenReturn(true);
        assertThatThrownBy(() -> useCase.solicitar(new SolicitarAbastecimentoCommand(
                10L, BigDecimal.TEN, "x", hoje, 1L)))
                .isInstanceOf(SolicitacaoAbertaDuplicadaException.class);

        SolicitacaoAbastecimento aprovada = SolicitacaoAbastecimento.reconstituir(
                30L, agencia, BigDecimal.TEN, "x", hoje, StatusSolicitacao.APROVADA,
                ativo, usuario(2L, PerfilUsuario.GESTOR, true), "ok", null,
                agora, agora, null, 0);
        when(solicitacoes.buscarPorId(30L)).thenReturn(Optional.of(aprovada));
        when(agencias.buscarPorIdParaAtualizacao(10L)).thenReturn(Optional.of(agencia));
        when(movimentacoes.existePorIdempotencyKey("repetida")).thenReturn(true);
        assertThatThrownBy(() -> useCase.atender(
                new AtenderSolicitacaoCommand(30L, 1L, "repetida")))
                .isInstanceOf(IdempotencyKeyDuplicadaException.class);
    }

    private Agencia agencia() {
        return new Agencia(10L, "0101", "Agência", "São Paulo",
                new BigDecimal("1000"), new BigDecimal("100"), true, 0);
    }

    private Usuario usuario(Long id, PerfilUsuario perfil, boolean ativo) {
        return new Usuario(id, "Usuário " + id, "usuario" + id, "hash", perfil,
                ativo, agora.minusSeconds(1000), 0, null);
    }
}
