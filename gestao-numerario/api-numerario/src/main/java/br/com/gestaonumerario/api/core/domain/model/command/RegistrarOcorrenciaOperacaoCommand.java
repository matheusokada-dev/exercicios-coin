package br.com.gestaonumerario.api.core.domain.model.command;

public record RegistrarOcorrenciaOperacaoCommand(
        Long solicitacaoId,
        String descricao,
        long versaoOperacao,
        Long usuarioId
) {
}
