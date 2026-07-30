package br.com.gestaonumerario.api.core.domain.model;

import br.com.gestaonumerario.api.core.domain.enums.EventoHistoricoSolicitacao;
import br.com.gestaonumerario.api.core.domain.enums.PerfilUsuario;
import br.com.gestaonumerario.api.core.domain.enums.StatusOperacaoNumerario;
import br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.enums.TipoOperacaoNumerario;
import br.com.gestaonumerario.api.core.domain.enums.TipoUnidadeOperacional;
import br.com.gestaonumerario.api.core.exception.ApenasGestorPodeDecidirException;
import br.com.gestaonumerario.api.core.exception.ConflitoVersaoException;
import br.com.gestaonumerario.api.core.exception.JustificativaObrigatoriaException;
import br.com.gestaonumerario.api.core.exception.RegraOperacaoNumerarioException;
import br.com.gestaonumerario.api.core.exception.TransicaoStatusInvalidaException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolicitacaoNumerarioTest {

    private static final Instant AGORA = Instant.parse("2026-07-24T12:00:00Z");
    private static final LocalDate HOJE = LocalDate.of(
            2026,
            7,
            24
    );
    private Usuario gestor;
    private Usuario operador;
    private UnidadeOperacional agencia;
    private UnidadeOperacional tesouraria;

    @BeforeEach
    void configurar() {
        gestor = usuario(
                1L,
                PerfilUsuario.GESTOR
        );
        operador = usuario(
                2L,
                PerfilUsuario.OPERADOR
        );
        agencia = unidade(
                10L,
                TipoUnidadeOperacional.AGENCIA,
                "AGE-0001",
                "100.00"
        );
        tesouraria = unidade(
                20L,
                TipoUnidadeOperacional.TESOURARIA,
                "TES-CENTRAL",
                "1000000.00"
        );
    }

    @Test
    void devePermitirAutoaprovacaoSemJustificativaEspecialPorValor() {
        SolicitacaoNumerario solicitacao = criarSuprimento(
                "600000.00",
                gestor
        );

        solicitacao.aprovar(
                gestor,
                "Necessidade validada",
                0,
                AGORA.plusSeconds(1)
        );

        assertEquals(
                StatusSolicitacaoNumerario.APROVADA,
                solicitacao.getStatus()
        );
        assertEquals(
                0,
                solicitacao.getVersao()
        );
        assertEquals(
                2,
                solicitacao.getEventosNovos()
                        .size()
        );
    }

    @Test
    void somenteGestorPodeCriarSolicitacao() {
        assertThrows(
                ApenasGestorPodeDecidirException.class,
                () -> criarSuprimento(
                        "100.00",
                        operador
                )
        );
    }

    @Test
    void deveRejeitarOuCancelarSomenteEnquantoPendente() {
        SolicitacaoNumerario rejeitada = criarSuprimento(
                "100.00",
                gestor
        );
        rejeitada.rejeitar(
                gestor,
                "Demanda não comprovada",
                0,
                AGORA.plusSeconds(1)
        );
        assertEquals(
                StatusSolicitacaoNumerario.REJEITADA,
                rejeitada.getStatus()
        );

        SolicitacaoNumerario cancelada = criarSuprimento(
                "100.00",
                gestor
        );
        cancelada.cancelar(
                gestor,
                "Demanda retirada",
                0,
                AGORA.plusSeconds(1)
        );
        assertEquals(
                StatusSolicitacaoNumerario.CANCELADA,
                cancelada.getStatus()
        );

        assertThrows(
                TransicaoStatusInvalidaException.class,
                () -> cancelada.aprovar(
                        gestor,
                        "Tentar reabrir",
                        0,
                        AGORA.plusSeconds(2)
                )
        );
    }

    @Test
    void deveExigirJustificativasEControlarVersao() {
        SolicitacaoNumerario solicitacao = criarSuprimento(
                "100.00",
                gestor
        );

        assertThrows(
                JustificativaObrigatoriaException.class,
                () -> solicitacao.aprovar(
                        gestor,
                        " ",
                        0,
                        AGORA
                )
        );
        assertThrows(
                ConflitoVersaoException.class,
                () -> solicitacao.aprovar(
                        gestor,
                        "Aprovada",
                        9,
                        AGORA
                )
        );
    }

    @Test
    void deveProgramarCompletandoRotaSemAlterarSaldo() {
        SolicitacaoNumerario solicitacao = criarSuprimento(
                "100.00",
                gestor
        );
        solicitacao.aprovar(
                gestor,
                "Aprovada",
                0,
                AGORA.plusSeconds(1)
        );

        OperacaoNumerario operacao = solicitacao.programar(
                tesouraria,
                gestor,
                "PROGRAMAR-1",
                0,
                AGORA.plusSeconds(2)
        );

        assertEquals(
                StatusSolicitacaoNumerario.EM_EXECUCAO,
                solicitacao.getStatus()
        );
        assertEquals(
                StatusOperacaoNumerario.PROGRAMADA,
                operacao.getStatus()
        );
        assertEquals(
                tesouraria,
                solicitacao.getOrigem()
        );
        assertEquals(
                agencia,
                solicitacao.getDestino()
        );
        assertEquals(
                new BigDecimal("1000000.00"),
                tesouraria.getSaldoAtual()
        );
    }

    @Test
    void naoDeveProgramarRotaComMesmaUnidadeOuSemControleDeSaldo() {
        SolicitacaoNumerario recolhimento = SolicitacaoNumerario.criar(
                TipoOperacaoNumerario.RECOLHIMENTO,
                1L,
                agencia,
                new BigDecimal("100.00"),
                "Excesso",
                HOJE,
                gestor,
                HOJE,
                AGORA
        );
        recolhimento.aprovar(
                gestor,
                "Aprovada",
                0,
                AGORA.plusSeconds(1)
        );

        assertThrows(
                RegraOperacaoNumerarioException.class,
                () -> recolhimento.programar(
                        agencia,
                        gestor,
                        "PROGRAMAR-2",
                        0,
                        AGORA.plusSeconds(2)
                )
        );
    }

    @Test
    void historicoExpostoDeveSerImutavel() {
        SolicitacaoNumerario solicitacao = criarSuprimento(
                "100.00",
                gestor
        );

        assertEquals(
                EventoHistoricoSolicitacao.SOLICITACAO_CRIADA,
                solicitacao.getEventosNovos()
                        .getFirst()
                        .evento()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> solicitacao.getEventosNovos()
                        .clear()
        );
    }

    @Test
    void reconstituicaoNaoDeveCriarEventosNovos() {
        SolicitacaoNumerario solicitacao = SolicitacaoNumerario.reconstituir(
                50L,
                TipoOperacaoNumerario.SUPRIMENTO,
                1L,
                tesouraria,
                agencia,
                new BigDecimal("100.00"),
                "Legado",
                HOJE,
                StatusSolicitacaoNumerario.CONCLUIDA,
                gestor,
                gestor,
                "Aprovada",
                AGORA,
                AGORA,
                null,
                null,
                null,
                AGORA,
                4
        );

        assertEquals(
                50L,
                solicitacao.getId()
        );
        assertEquals(
                4,
                solicitacao.getVersao()
        );
        assertTrue(
                solicitacao.getEventosNovos()
                        .isEmpty()
        );
    }

    private SolicitacaoNumerario criarSuprimento(String valor, Usuario solicitante) {
        return SolicitacaoNumerario.criar(
                TipoOperacaoNumerario.SUPRIMENTO,
                1L,
                agencia,
                new BigDecimal(valor),
                "Reposição",
                HOJE.plusDays(1),
                solicitante,
                HOJE,
                AGORA
        );
    }

    private static Usuario usuario(Long id, PerfilUsuario perfil) {
        return new Usuario(
                id,
                perfil.name(),
                perfil.name()
                        .toLowerCase(),
                "hash",
                perfil,
                true,
                AGORA,
                0,
                null
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
