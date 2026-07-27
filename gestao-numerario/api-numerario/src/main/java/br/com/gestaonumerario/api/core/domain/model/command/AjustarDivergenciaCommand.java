package br.com.gestaonumerario.api.core.domain.model.command;

import java.math.BigDecimal;

public record AjustarDivergenciaCommand(
        Long solicitacaoId,
        Long unidadeId,
        BigDecimal valor,
        boolean entrada,
        String justificativa,
        long versaoUnidade,
        Long usuarioId,
        String idempotencyKey
) {}
