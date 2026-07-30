package br.com.gestaonumerario.api.core.domain.model.command;

import java.math.BigDecimal;

public record ReceberOperacaoNumerarioCommand(
        Long solicitacaoId,
        BigDecimal valorRecebido,
        String justificativaDivergencia,
        long versaoOperacao,
        long versaoUnidade,
        Long usuarioId,
        String idempotencyKey
) {
}
