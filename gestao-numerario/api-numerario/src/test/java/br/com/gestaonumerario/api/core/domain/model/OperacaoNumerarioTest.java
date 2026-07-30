package br.com.gestaonumerario.api.core.domain.model;

import br.com.gestaonumerario.api.core.domain.enums.EventoHistoricoSolicitacao;
import br.com.gestaonumerario.api.core.domain.enums.PerfilUsuario;
import br.com.gestaonumerario.api.core.domain.enums.StatusOperacaoNumerario;
import br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.enums.TipoOperacaoNumerario;
import br.com.gestaonumerario.api.core.domain.enums.TipoUnidadeOperacional;
import br.com.gestaonumerario.api.core.exception.JustificativaObrigatoriaException;
import br.com.gestaonumerario.api.core.exception.RegraOperacaoNumerarioException;
import br.com.gestaonumerario.api.core.exception.SaldoInsuficienteException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperacaoNumerarioTest {

    private static final Instant AGORA = Instant.parse("2026-07-24T12:00:00Z");
    private Usuario gestor;
    private UnidadeOperacional agencia;
    private UnidadeOperacional tesouraria;
    private SolicitacaoNumerario solicitacao;
    private OperacaoNumerario operacao;

    @BeforeEach
    void configurar() {
        gestor = new Usuario(
                1L,
                "Gestor",
                "gestor",
                "hash",
                PerfilUsuario.GESTOR,
                true,
                AGORA,
                0,
                null
        );
        agencia = unidade(
                10L,
                TipoUnidadeOperacional.AGENCIA,
                "AGE-1",
                "100.00"
        );
        tesouraria = unidade(
                20L,
                TipoUnidadeOperacional.TESOURARIA,
                "TES-1",
                "1000.00"
        );
        solicitacao = SolicitacaoNumerario.criar(
                TipoOperacaoNumerario.SUPRIMENTO,
                1L,
                agencia,
                new BigDecimal("200.00"),
                "Reposição",
                LocalDate.of(
                        2026,
                        7,
                        25
                ),
                gestor,
                LocalDate.of(
                        2026,
                        7,
                        24
                ),
                AGORA
        );
        solicitacao.aprovar(
                gestor,
                "Aprovada",
                0,
                AGORA.plusSeconds(1)
        );
        operacao = solicitacao.programar(
                tesouraria,
                gestor,
                "PROGRAMAR-1",
                0,
                AGORA.plusSeconds(2)
        );
    }

    @Test
    void deveExpedirDiretamenteSemSeparacaoEConcluirRecebimentoIntegral() {
        operacao.expedir(
                gestor,
                0,
                0,
                "EXPEDIR-1",
                AGORA.plusSeconds(3)
        );
        operacao.receber(
                new BigDecimal("200.00"),
                null,
                gestor,
                0,
                0,
                "RECEBER-1",
                AGORA.plusSeconds(4)
        );

        assertEquals(
                new BigDecimal("800.00"),
                tesouraria.getSaldoAtual()
        );
        assertEquals(
                new BigDecimal("300.00"),
                agencia.getSaldoAtual()
        );
        assertEquals(
                StatusOperacaoNumerario.RECEBIDA,
                operacao.getStatus()
        );
        assertEquals(
                StatusSolicitacaoNumerario.CONCLUIDA,
                solicitacao.getStatus()
        );
    }

    @Test
    void devePermitirSeparacaoOpcionalAntesDaExpedicao() {
        operacao.iniciarSeparacao(
                gestor,
                0,
                AGORA.plusSeconds(3)
        );
        operacao.expedir(
                gestor,
                0,
                0,
                "EXPEDIR-2",
                AGORA.plusSeconds(4)
        );

        assertEquals(
                StatusOperacaoNumerario.EM_TRANSITO,
                operacao.getStatus()
        );
    }

    @Test
    void deveRegistrarOcorrenciaSemTrocarEstagioEPermitirContinuacao() {
        operacao.registrarOcorrencia(
                "Veículo substituído",
                gestor,
                0,
                AGORA.plusSeconds(3)
        );

        assertEquals(
                StatusOperacaoNumerario.PROGRAMADA,
                operacao.getStatus()
        );
        operacao.expedir(
                gestor,
                0,
                0,
                "EXPEDIR-3",
                AGORA.plusSeconds(4)
        );
        assertEquals(
                StatusOperacaoNumerario.EM_TRANSITO,
                operacao.getStatus()
        );
        assertTrue(
                solicitacao.getEventosNovos()
                        .stream()
                        .anyMatch(e -> e.evento() == EventoHistoricoSolicitacao.OCORRENCIA_REGISTRADA)
        );
    }

    @Test
    void deveRegistrarEConciliarDivergenciaSemAjusteAutomatico() {
        operacao.expedir(
                gestor,
                0,
                0,
                "EXPEDIR-4",
                AGORA.plusSeconds(3)
        );
        operacao.receber(
                new BigDecimal("180.00"),
                "Faltaram R$ 20",
                gestor,
                0,
                0,
                "RECEBER-4",
                AGORA.plusSeconds(4)
        );

        assertEquals(
                StatusOperacaoNumerario.COM_DIVERGENCIA,
                operacao.getStatus()
        );
        assertEquals(
                StatusSolicitacaoNumerario.COM_DIVERGENCIA,
                solicitacao.getStatus()
        );
        assertEquals(
                new BigDecimal("20.00"),
                operacao.getValorDivergencia()
        );
        assertEquals(
                new BigDecimal("280.00"),
                agencia.getSaldoAtual()
        );

        operacao.conciliar(
                "Diferença reconhecida",
                gestor,
                0,
                "CONCILIAR-4",
                AGORA.plusSeconds(5)
        );

        assertEquals(
                StatusOperacaoNumerario.CONCILIADA,
                operacao.getStatus()
        );
        assertEquals(
                StatusSolicitacaoNumerario.CONCLUIDA,
                solicitacao.getStatus()
        );
        assertEquals(
                new BigDecimal("280.00"),
                agencia.getSaldoAtual()
        );
    }

    @Test
    void deveExigirJustificativaQuandoRecebidoForMenor() {
        operacao.expedir(
                gestor,
                0,
                0,
                "EXPEDIR-5",
                AGORA.plusSeconds(3)
        );

        assertThrows(
                JustificativaObrigatoriaException.class,
                () -> operacao.receber(
                        new BigDecimal("180.00"),
                        " ",
                        gestor,
                        0,
                        0,
                        "RECEBER-5",
                        AGORA.plusSeconds(4)
                )
        );
    }

    @Test
    void naoDeveReceberValorSuperiorAoExpedido() {
        operacao.expedir(
                gestor,
                0,
                0,
                "EXPEDIR-6",
                AGORA.plusSeconds(3)
        );

        assertThrows(
                RegraOperacaoNumerarioException.class,
                () -> operacao.receber(
                        new BigDecimal("201.00"),
                        "Excesso",
                        gestor,
                        0,
                        0,
                        "RECEBER-6",
                        AGORA.plusSeconds(4)
                )
        );
        assertEquals(
                new BigDecimal("100.00"),
                agencia.getSaldoAtual()
        );
    }

    @Test
    void saldoInsuficienteNaoDeveAlterarOrigemNemOperacao() {
        UnidadeOperacional origemSemSaldo = unidade(
                30L,
                TipoUnidadeOperacional.TESOURARIA,
                "TES-SEM-SALDO",
                "50.00"
        );
        SolicitacaoNumerario s = SolicitacaoNumerario.criar(
                TipoOperacaoNumerario.SUPRIMENTO,
                1L,
                agencia,
                new BigDecimal("200.00"),
                "Reposição",
                LocalDate.of(
                        2026,
                        7,
                        25
                ),
                gestor,
                LocalDate.of(
                        2026,
                        7,
                        24
                ),
                AGORA
        );
        s.aprovar(
                gestor,
                "Aprovada",
                0,
                AGORA.plusSeconds(1)
        );
        OperacaoNumerario o = s.programar(
                origemSemSaldo,
                gestor,
                "PROGRAMAR-SEM-SALDO",
                0,
                AGORA.plusSeconds(2)
        );

        assertThrows(
                SaldoInsuficienteException.class,
                () -> o.expedir(
                        gestor,
                        0,
                        0,
                        "EXPEDIR-SEM-SALDO",
                        AGORA.plusSeconds(3)
                )
        );
        assertEquals(
                new BigDecimal("50.00"),
                origemSemSaldo.getSaldoAtual()
        );
        assertEquals(
                StatusOperacaoNumerario.PROGRAMADA,
                o.getStatus()
        );
    }

    private static UnidadeOperacional unidade(Long id, TipoUnidadeOperacional tipo, String codigo, String saldo) {
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
