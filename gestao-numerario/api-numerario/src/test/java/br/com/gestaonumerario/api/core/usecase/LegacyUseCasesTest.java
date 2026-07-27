package br.com.gestaonumerario.api.core.usecase;

import br.com.gestaonumerario.api.core.domain.enums.PerfilUsuario;
import br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao;
import br.com.gestaonumerario.api.core.domain.model.*;
import br.com.gestaonumerario.api.core.domain.model.command.*;
import br.com.gestaonumerario.api.core.exception.*;
import br.com.gestaonumerario.api.core.usecase.agencia.AgenciaUseCase;
import br.com.gestaonumerario.api.core.usecase.movimentacao.MovimentacaoUseCase;
import br.com.gestaonumerario.api.core.usecase.usuario.UsuarioUseCase;
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

class LegacyUseCasesTest {
    private AgenciaOutputPort agencias;
    private UsuarioOutputPort usuarios;
    private MovimentacaoOutputPort movimentacoes;
    private SolicitacaoAbastecimentoOutputPort solicitacoes;
    private RelogioOutputPort relogio;
    private TransacaoOutputPort transacao;

    @BeforeEach
    void configurar() {
        agencias = mock(AgenciaOutputPort.class);
        usuarios = mock(UsuarioOutputPort.class);
        movimentacoes = mock(MovimentacaoOutputPort.class);
        solicitacoes = mock(SolicitacaoAbastecimentoOutputPort.class);
        relogio = mock(RelogioOutputPort.class);
        transacao = mock(TransacaoOutputPort.class);
        when(transacao.executar(any())).thenAnswer(invocation ->
                ((TransacaoOutputPort.AcaoTransacional<?>) invocation.getArgument(0)).executar());
    }

    @Test
    void deveCriarAtualizarListarDetalharEDesativarAgencia() {
        AgenciaUseCase useCase = new AgenciaUseCase(
                agencias, solicitacoes, movimentacoes, relogio, transacao);
        Agencia agencia = agenciaAtiva();
        when(agencias.salvar(any())).thenAnswer(i -> i.getArgument(0));
        when(agencias.buscarPorId(1L)).thenReturn(Optional.of(agencia));
        when(agencias.buscar(any())).thenReturn(new Pagina<>(java.util.List.of(agencia), 0, 20, 1, 1));
        when(relogio.hoje()).thenReturn(LocalDate.of(2026, 7, 25));
        when(movimentacoes.resumirDiaPorAgencia(anyLong(), any()))
                .thenReturn(new ResumoMovimentacaoDiaria(new BigDecimal("20"), new BigDecimal("5")));
        when(solicitacoes.buscarAprovadaPorAgenciaId(1L)).thenReturn(Optional.empty());

        Agencia criada = useCase.criar(new CriarAgenciaCommand(
                " 0101 ", "Agência Sé", "São Paulo",
                new BigDecimal("100"), new BigDecimal("20")));
        Agencia atualizada = useCase.atualizar(new AtualizarAgenciaCommand(
                1L, "Agência Central", "São Paulo", new BigDecimal("30")));
        Pagina<Agencia> pagina = useCase.listar(new FiltroAgencia(
                "  central ", true, false, null, null, 0, 20));
        DetalheAgencia detalhe = useCase.detalhar(1L);
        useCase.desativar(1L);

        assertThat(criada.getCodigo()).isEqualTo("0101");
        assertThat(atualizada.getNome()).isEqualTo("Agência Central");
        assertThat(pagina.totalItens()).isOne();
        assertThat(detalhe.saldoPrevistoAposAbastecimentoAprovado()).isEqualByComparingTo("100.00");
        assertThat(agencia.isAtivo()).isFalse();
        verify(agencias, atLeast(3)).salvar(any());
    }

    @Test
    void deveRejeitarDadosInvalidosDeAgencia() {
        AgenciaUseCase useCase = new AgenciaUseCase(
                agencias, solicitacoes, movimentacoes, relogio, transacao);
        when(agencias.existePorCodigo("0101")).thenReturn(true);

        assertThatThrownBy(() -> useCase.criar(null)).isInstanceOf(CampoObrigatorioException.class);
        assertThatThrownBy(() -> useCase.criar(new CriarAgenciaCommand(
                " ", "Nome", "Cidade", BigDecimal.ONE, BigDecimal.ZERO)))
                .isInstanceOf(CampoObrigatorioException.class);
        assertThatThrownBy(() -> useCase.criar(new CriarAgenciaCommand(
                "0101", "Nome", "Cidade", BigDecimal.ONE, BigDecimal.ZERO)))
                .isInstanceOf(CodigoAgenciaDuplicadoException.class);
        assertThatThrownBy(() -> useCase.buscarPorId(null)).isInstanceOf(CampoObrigatorioException.class);
        assertThatThrownBy(() -> useCase.buscarPorId(99L)).isInstanceOf(AgenciaNaoEncontradaException.class);
        assertThatThrownBy(() -> useCase.listar(null)).isInstanceOf(CampoObrigatorioException.class);
        assertThatThrownBy(() -> useCase.desativar(null)).isInstanceOf(CampoObrigatorioException.class);
    }

    @Test
    void deveCriarEBuscarUsuario() {
        CodificadorSenhaOutputPort codificador = mock(CodificadorSenhaOutputPort.class);
        when(codificador.codificar("segredo")).thenReturn("hash");
        when(relogio.agora()).thenReturn(Instant.parse("2026-07-25T10:00:00Z"));
        when(usuarios.salvar(any())).thenAnswer(i -> i.getArgument(0));
        Usuario existente = usuarioAtivo();
        when(usuarios.buscarPorId(1L)).thenReturn(Optional.of(existente));
        UsuarioUseCase useCase = new UsuarioUseCase(usuarios, codificador, relogio, transacao);

        Usuario criado = useCase.criar(new CriarUsuarioCommand(
                "Gestor", " gestor ", " segredo ", PerfilUsuario.GESTOR));

        assertThat(criado.getLogin()).isEqualTo("gestor");
        assertThat(criado.getSenhaHash()).isEqualTo("hash");
        assertThat(useCase.buscarPorId(1L)).isSameAs(existente);
    }

    @Test
    void deveRejeitarUsuarioInvalidoOuDuplicado() {
        CodificadorSenhaOutputPort codificador = mock(CodificadorSenhaOutputPort.class);
        UsuarioUseCase useCase = new UsuarioUseCase(usuarios, codificador, relogio, transacao);
        when(usuarios.existePorLogin("gestor")).thenReturn(true);

        assertThatThrownBy(() -> useCase.criar(null)).isInstanceOf(CampoObrigatorioException.class);
        assertThatThrownBy(() -> useCase.criar(new CriarUsuarioCommand(
                "Nome", " ", "senha", PerfilUsuario.GESTOR)))
                .isInstanceOf(CampoObrigatorioException.class);
        assertThatThrownBy(() -> useCase.criar(new CriarUsuarioCommand(
                "Nome", "gestor", "senha", PerfilUsuario.GESTOR)))
                .isInstanceOf(LoginDuplicadoException.class);
        assertThatThrownBy(() -> useCase.buscarPorId(null)).isInstanceOf(CampoObrigatorioException.class);
        assertThatThrownBy(() -> useCase.buscarPorId(9L)).isInstanceOf(UsuarioNaoEncontradoException.class);
    }

    @Test
    void deveRegistrarEntradaESaidaDeMovimentacao() {
        MovimentacaoUseCase useCase = new MovimentacaoUseCase(
                agencias, usuarios, movimentacoes, relogio, transacao);
        Agencia agencia = agenciaAtiva();
        Usuario usuario = usuarioAtivo();
        when(agencias.buscarPorIdParaAtualizacao(1L)).thenReturn(Optional.of(agencia));
        when(usuarios.buscarPorId(1L)).thenReturn(Optional.of(usuario));
        when(relogio.agora()).thenReturn(Instant.parse("2026-07-25T10:00:00Z"));
        when(movimentacoes.salvar(any())).thenAnswer(i -> i.getArgument(0));

        Movimentacao deposito = useCase.registrar(new RegistrarMovimentacaoCommand(
                1L, 1L, TipoMovimentacao.DEPOSITO, null, new BigDecimal("10"),
                "Depósito", " deposito-1 "));
        Movimentacao ajuste = useCase.registrar(new RegistrarMovimentacaoCommand(
                1L, 1L, TipoMovimentacao.AJUSTE, false, new BigDecimal("5"),
                "Ajuste", "ajuste-1"));

        assertThat(deposito.isEntrada()).isTrue();
        assertThat(ajuste.isEntrada()).isFalse();
        assertThat(agencia.getSaldoAtual()).isEqualByComparingTo("105.00");
        verify(agencias, times(2)).salvar(agencia);
    }

    @Test
    void deveRejeitarMovimentacaoInvalida() {
        MovimentacaoUseCase useCase = new MovimentacaoUseCase(
                agencias, usuarios, movimentacoes, relogio, transacao);
        when(agencias.buscarPorIdParaAtualizacao(1L)).thenReturn(Optional.of(agenciaAtiva()));
        when(usuarios.buscarPorId(1L)).thenReturn(Optional.of(usuarioAtivo()));
        when(movimentacoes.existePorIdempotencyKey("duplicada")).thenReturn(true);

        assertThatThrownBy(() -> useCase.registrar(null)).isInstanceOf(CampoObrigatorioException.class);
        assertThatThrownBy(() -> useCase.registrar(new RegistrarMovimentacaoCommand(
                1L, 1L, TipoMovimentacao.ABASTECIMENTO, null, BigDecimal.ONE, "x", "k")))
                .isInstanceOf(TipoMovimentacaoNaoPermitidoException.class);
        assertThatThrownBy(() -> useCase.registrar(new RegistrarMovimentacaoCommand(
                1L, 1L, TipoMovimentacao.AJUSTE, null, BigDecimal.ONE, "x", "k")))
                .isInstanceOf(CampoObrigatorioException.class);
        assertThatThrownBy(() -> useCase.registrar(new RegistrarMovimentacaoCommand(
                1L, 1L, TipoMovimentacao.DEPOSITO, null, BigDecimal.ONE, "x", "duplicada")))
                .isInstanceOf(IdempotencyKeyDuplicadaException.class);
        assertThatThrownBy(() -> useCase.consultar(null)).isInstanceOf(CampoObrigatorioException.class);
    }

    private Agencia agenciaAtiva() {
        return new Agencia(1L, "0101", "Agência Sé", "São Paulo",
                new BigDecimal("100"), new BigDecimal("20"), true, 0);
    }

    private Usuario usuarioAtivo() {
        return new Usuario(1L, "Gestor", "gestor", "hash", PerfilUsuario.GESTOR,
                true, Instant.parse("2026-01-01T00:00:00Z"), 0, null);
    }
}
