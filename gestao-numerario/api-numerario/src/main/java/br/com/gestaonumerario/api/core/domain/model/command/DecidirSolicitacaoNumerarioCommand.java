package br.com.gestaonumerario.api.core.domain.model.command;

public record DecidirSolicitacaoNumerarioCommand(
        Long solicitacaoId,
        String justificativa,
        long versao,
        Long usuarioId
) {
}
