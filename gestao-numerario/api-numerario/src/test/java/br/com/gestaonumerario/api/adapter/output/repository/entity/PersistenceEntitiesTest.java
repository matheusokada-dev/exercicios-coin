package br.com.gestaonumerario.api.adapter.output.repository.entity;

import br.com.gestaonumerario.api.core.domain.enums.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PersistenceEntitiesTest {

    private static final Instant AGORA = Instant.parse("2026-07-25T12:00:00Z");

    @Test
    void representaUnidadeAgenciaEUsuarioPersistidos() {
        var unidade = unidade();
        var agencia = new AgenciaEntity("001", "Centro", "São Paulo", unidade, new BigDecimal("50"));
        var agenciaLegada = new AgenciaEntity(2L, "002", "Norte", "Campinas",
                new BigDecimal("99"), new BigDecimal("30"), false, 3);
        var usuario = usuario();

        assertThat(unidade.getId()).isEqualTo(10L);
        assertThat(unidade.getTipo()).isEqualTo(TipoUnidadeOperacional.TESOURARIA);
        assertThat(unidade.getCodigo()).isEqualTo("TES-01");
        assertThat(unidade.getNome()).isEqualTo("Tesouraria");
        assertThat(unidade.isControlaSaldo()).isTrue();
        assertThat(unidade.getSaldoAtual()).isEqualByComparingTo("100.00");
        assertThat(unidade.isAtivo()).isTrue();
        assertThat(unidade.getVersao()).isEqualTo(1);
        assertThat(unidade.getCriadoEm()).isEqualTo(AGORA);
        assertThat(unidade.getAtualizadoEm()).isEqualTo(AGORA);
        assertThat(agencia.getCodigo()).isEqualTo("001");
        assertThat(agencia.getSaldoAtual()).isEqualByComparingTo("100.00");
        assertThat(agencia.getLimiteMinimo()).isEqualByComparingTo("50.00");
        assertThat(agencia.isAtivo()).isTrue();
        assertThat(agenciaLegada.getId()).isEqualTo(2L);
        assertThat(agenciaLegada.getSaldoAtual()).isZero();
        assertThat(usuario.getId()).isEqualTo(7L);
        assertThat(usuario.getPerfil()).isEqualTo(PerfilUsuario.GESTOR);
    }

    @Test
    void representaSolicitacaoNosDoisContratosEConverteStatusLegado() {
        var unidade = unidade();
        var agencia = new AgenciaEntity("001", "Centro", "São Paulo", unidade, BigDecimal.TEN);
        var usuario = usuario();

        for (StatusSolicitacao status : StatusSolicitacao.values()) {
            var entity = new SolicitacaoAbastecimentoEntity(1L, agencia, BigDecimal.TEN,
                    "motivo", LocalDate.of(2026, 7, 26), status, usuario, usuario,
                    "decisão", null, AGORA, AGORA, AGORA, 2);
            assertThat(entity.getStatusLegado()).isEqualTo(status);
            assertThat(entity.getTipoOperacao()).isEqualTo(TipoOperacaoNumerario.SUPRIMENTO);
            assertThat(entity.getJustificativaEspecial()).isNull();
        }

        var completa = new SolicitacaoAbastecimentoEntity(2L, TipoOperacaoNumerario.RECOLHIMENTO,
                agencia, unidade, unidade, new BigDecimal("25"), "motivo",
                LocalDate.of(2026, 7, 27), StatusSolicitacaoNumerario.CANCELADA, usuario,
                usuario, "decisão", AGORA, AGORA, AGORA, usuario,
                "cancelamento", AGORA, 4);

        assertThat(completa.getId()).isEqualTo(2L);
        assertThat(completa.getOrigem()).isSameAs(unidade);
        assertThat(completa.getDestino()).isSameAs(unidade);
        assertThat(completa.getValor()).isEqualByComparingTo("25.00");
        assertThat(completa.getMotivo()).isEqualTo("motivo");
        assertThat(completa.getStatus()).isEqualTo(StatusSolicitacaoNumerario.CANCELADA);
        assertThat(completa.getSolicitante()).isSameAs(usuario);
        assertThat(completa.getDecisor()).isSameAs(usuario);
        assertThat(completa.getCanceladoPor()).isSameAs(usuario);
        assertThat(completa.getJustificativaCancelamento()).isEqualTo("cancelamento");
        assertThat(completa.getDataCancelamento()).isEqualTo(AGORA);
        assertThat(completa.getVersao()).isEqualTo(4);
    }

    @Test
    void representaOperacaoEMovimentacaoPersistidas() {
        var unidade = unidade();
        var usuario = usuario();
        var solicitacao = new SolicitacaoAbastecimentoEntity(2L, TipoOperacaoNumerario.SUPRIMENTO,
                null, unidade, unidade, BigDecimal.TEN, "motivo", LocalDate.now(),
                StatusSolicitacaoNumerario.APROVADA, usuario, null, null, AGORA,
                null, null, null, null, null, 0);
        var operacao = new OperacaoNumerarioEntity(3L, solicitacao, unidade, unidade,
                StatusOperacaoNumerario.PROGRAMADA, BigDecimal.TEN, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ZERO, usuario, usuario, usuario, usuario,
                AGORA, AGORA, AGORA, AGORA, "justificativa", "ocorrência", "idem", 5);
        var movimentacao = new MovimentacaoEntity(unidade, solicitacao, operacao,
                TipoMovimentacao.DEPOSITO, true, BigDecimal.ONE, BigDecimal.TEN,
                new BigDecimal("11"), "descrição", AGORA, usuario, "mov-1");
        var semAgencia = new MovimentacaoEntity(4L, null, solicitacao, TipoMovimentacao.SAQUE,
                false, BigDecimal.ONE, BigDecimal.TEN, new BigDecimal("9"),
                null, AGORA, usuario, null);

        assertThat(operacao.getId()).isEqualTo(3L);
        assertThat(operacao.getSolicitacao()).isSameAs(solicitacao);
        assertThat(operacao.getOrigem()).isSameAs(unidade);
        assertThat(operacao.getDestino()).isSameAs(unidade);
        assertThat(operacao.getStatus()).isEqualTo(StatusOperacaoNumerario.PROGRAMADA);
        assertThat(operacao.getValorProgramado()).isEqualByComparingTo("10.00");
        assertThat(operacao.getValorExpedido()).isEqualByComparingTo("1.00");
        assertThat(operacao.getValorRecebido()).isEqualByComparingTo("1.00");
        assertThat(operacao.getValorDivergencia()).isZero();
        assertThat(operacao.getProgramadoPor()).isSameAs(usuario);
        assertThat(operacao.getIdempotencyKey()).isEqualTo("idem");
        assertThat(operacao.getVersao()).isEqualTo(5);
        assertThat(movimentacao.getUnidade()).isSameAs(unidade);
        assertThat(movimentacao.getOperacao()).isSameAs(operacao);
        assertThat(movimentacao.getTipo()).isEqualTo(TipoMovimentacao.DEPOSITO);
        assertThat(movimentacao.isEntrada()).isTrue();
        assertThat(movimentacao.getSaldoPosterior()).isEqualByComparingTo("11.00");
        assertThat(movimentacao.getUsuario()).isSameAs(usuario);
        assertThat(semAgencia.getAgencia()).isNull();
    }

    @Test
    void refreshTokenControlaValidadeERevogacaoIdempotente() {
        var token = new RefreshTokenEntity(usuario(), "hash", AGORA, AGORA.plusSeconds(60));

        assertThat(token.validoEm(AGORA)).isTrue();
        token.revogar(AGORA.plusSeconds(1));
        token.revogar(AGORA.plusSeconds(2));

        assertThat(token.validoEm(AGORA.plusSeconds(2))).isFalse();
        assertThat(token.getRevogadoEm()).isEqualTo(AGORA.plusSeconds(1));
    }

    private UnidadeOperacionalEntity unidade() {
        return new UnidadeOperacionalEntity(10L, TipoUnidadeOperacional.TESOURARIA,
                "TES-01", "Tesouraria", true, new BigDecimal("100"), true,
                1, AGORA, AGORA);
    }

    private UsuarioEntity usuario() {
        return new UsuarioEntity(7L, "Gestor", "gestor", "hash",
                PerfilUsuario.GESTOR, true, AGORA, 0, null);
    }
}
