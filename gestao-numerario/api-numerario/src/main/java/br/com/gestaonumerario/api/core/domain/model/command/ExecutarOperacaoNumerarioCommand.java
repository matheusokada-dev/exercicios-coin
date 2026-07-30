package br.com.gestaonumerario.api.core.domain.model.command;

public record ExecutarOperacaoNumerarioCommand(
        Long solicitacaoId,
        long versaoOperacao,
        long versaoUnidade,
        Long usuarioId,
        String idempotencyKey
) {
}
