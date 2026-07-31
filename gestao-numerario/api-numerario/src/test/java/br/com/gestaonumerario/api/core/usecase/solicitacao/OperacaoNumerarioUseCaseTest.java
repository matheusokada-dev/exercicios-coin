package br.com.gestaonumerario.api.core.usecase.solicitacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.gestaonumerario.api.core.domain.enums.PerfilUsuario;
import br.com.gestaonumerario.api.core.domain.enums.StatusOperacaoNumerario;
import br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao;
import br.com.gestaonumerario.api.core.domain.enums.TipoOperacaoNumerario;
import br.com.gestaonumerario.api.core.domain.enums.TipoUnidadeOperacional;
import br.com.gestaonumerario.api.core.domain.model.DetalheSolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.model.OperacaoNumerario;
import br.com.gestaonumerario.api.core.domain.model.SolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.model.UnidadeOperacional;
import br.com.gestaonumerario.api.core.domain.model.Usuario;
import br.com.gestaonumerario.api.core.domain.model.command.ConciliarOperacaoNumerarioCommand;
import br.com.gestaonumerario.api.core.domain.model.command.ExecutarOperacaoNumerarioCommand;
import br.com.gestaonumerario.api.core.domain.model.command.ProgramarOperacaoNumerarioCommand;
import br.com.gestaonumerario.api.core.domain.model.command.ReceberOperacaoNumerarioCommand;
import br.com.gestaonumerario.api.core.domain.model.command.RegistrarOcorrenciaOperacaoCommand;
import br.com.gestaonumerario.api.core.exception.CampoObrigatorioException;
import br.com.gestaonumerario.api.core.exception.IdempotencyKeyDuplicadaException;
import br.com.gestaonumerario.api.port.output.NumerarioOutputPort;
import br.com.gestaonumerario.api.port.output.RelogioOutputPort;
import br.com.gestaonumerario.api.port.output.TransacaoOutputPort;
import br.com.gestaonumerario.api.port.output.UsuarioOutputPort;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OperacaoNumerarioUseCaseTest {

    private static final Instant AGORA = Instant.parse("2026-07-30T12:00:00Z");
    private NumerarioOutputPort port;
    private UsuarioOutputPort usuarios;
    private OperacaoNumerarioUseCase useCase;
    private Usuario gestor;
    private UnidadeOperacional agencia;
    private UnidadeOperacional tesouraria;
    private SolicitacaoNumerario solicitacao;

    @BeforeEach
    void configurar() {
        port = mock(NumerarioOutputPort.class);
        usuarios = mock(UsuarioOutputPort.class);
        RelogioOutputPort relogio = mock(RelogioOutputPort.class);
        TransacaoOutputPort transacao = mock(TransacaoOutputPort.class);
        when(transacao.executar(any())).thenAnswer(
                invocation -> ((TransacaoOutputPort.AcaoTransacional<?>) invocation.getArgument(0)).executar()
        );
        when(relogio.agora()).thenReturn(AGORA);
        when(port.salvarProgramacao(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(port.salvarOperacao(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(port.salvarOperacaoFinanceira(any(), any(), any(), any(), any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        gestor = new Usuario(
                1L,
                "Gestor",
                "gestor",
                "hash",
                PerfilUsuario.GESTOR,
                true,
                AGORA.minusSeconds(3600),
                0,
                null
        );
        agencia = unidade(10L, TipoUnidadeOperacional.AGENCIA, "AGE-1", "100.00");
        tesouraria = unidade(20L, TipoUnidadeOperacional.TESOURARIA, "TES-1", "1000.00");
        solicitacao = SolicitacaoNumerario.criar(
                TipoOperacaoNumerario.SUPRIMENTO,
                10L,
                agencia,
                new BigDecimal("200.00"),
                "Reposição",
                LocalDate.of(2026, 7, 31),
                gestor,
                LocalDate.of(2026, 7, 30),
                AGORA.minusSeconds(120)
        );
        solicitacao.aprovar(gestor, "Aprovada", 0, AGORA.minusSeconds(60));

        when(usuarios.buscarPorId(1L)).thenReturn(Optional.of(gestor));
        when(port.buscarUnidade(20L)).thenReturn(Optional.of(tesouraria));
        useCase = new OperacaoNumerarioUseCase(port, usuarios, relogio, transacao);
    }

    @Test
    void deveExecutarFluxoCompletoComDivergenciaEIdempotencia() {
        when(port.buscarDetalhe(100L)).thenReturn(
                Optional.of(new DetalheSolicitacaoNumerario(solicitacao, null, List.of()))
        );

        OperacaoNumerario operacao = useCase.programar(
                new ProgramarOperacaoNumerarioCommand(100L, 20L, 0, 1L, "programar-100")
        );
        when(port.buscarDetalhe(100L)).thenReturn(
                Optional.of(new DetalheSolicitacaoNumerario(solicitacao, operacao, List.of()))
        );
        when(port.buscarUnidadeParaAtualizacao(20L)).thenReturn(Optional.of(tesouraria));
        when(port.buscarUnidadeParaAtualizacao(10L)).thenReturn(Optional.of(agencia));

        useCase.registrarOcorrencia(
                new RegistrarOcorrenciaOperacaoCommand(100L, "Veículo substituído", 0, 1L)
        );
        useCase.iniciarSeparacao(new ExecutarOperacaoNumerarioCommand(100L, 0, 0, 1L, null));
        useCase.expedir(new ExecutarOperacaoNumerarioCommand(100L, 0, 0, 1L, "expedir-100"));
        useCase.receber(
                new ReceberOperacaoNumerarioCommand(
                        100L,
                        new BigDecimal("180.00"),
                        "Diferença na conferência",
                        0,
                        0,
                        1L,
                        "receber-100"
                )
        );
        OperacaoNumerario conciliada = useCase.conciliar(
                new ConciliarOperacaoNumerarioCommand(
                        100L,
                        "Divergência reconhecida",
                        0,
                        1L,
                        "conciliar-100"
                )
        );

        assertThat(conciliada.getStatus()).isEqualTo(StatusOperacaoNumerario.CONCILIADA);
        assertThat(solicitacao.getStatus()).isEqualTo(StatusSolicitacaoNumerario.CONCLUIDA);
        assertThat(tesouraria.getSaldoAtual()).isEqualByComparingTo("800.00");
        assertThat(agencia.getSaldoAtual()).isEqualByComparingTo("280.00");
        verify(port).salvarOperacaoFinanceira(
                operacao,
                TipoMovimentacao.SAIDA_PARA_TRANSITO,
                1L,
                "expedir-100",
                "Expedição da solicitação 100",
                AGORA
        );
        verify(port).salvarOperacaoFinanceira(
                operacao,
                TipoMovimentacao.ENTRADA_DE_TRANSITO,
                1L,
                "receber-100",
                "Recebimento da solicitação 100",
                AGORA
        );
    }

    @Test
    void deveRejeitarComandoSemChaveEChaveDuplicada() {
        assertThatThrownBy(
                () -> useCase.programar(new ProgramarOperacaoNumerarioCommand(100L, 20L, 0, 1L, " "))
        ).isInstanceOf(CampoObrigatorioException.class);

        when(port.existeIdempotencyKey("duplicada")).thenReturn(true);
        assertThatThrownBy(
                () -> useCase.programar(
                        new ProgramarOperacaoNumerarioCommand(100L, 20L, 0, 1L, "duplicada")
                )
        ).isInstanceOf(IdempotencyKeyDuplicadaException.class);
    }

    private static UnidadeOperacional unidade(
            Long id,
            TipoUnidadeOperacional tipo,
            String codigo,
            String saldo) {
        return new UnidadeOperacional(
                id,
                tipo,
                codigo,
                codigo,
                true,
                new BigDecimal(saldo),
                true,
                0,
                AGORA,
                AGORA
        );
    }
}
