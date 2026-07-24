package br.com.gestaonumerario.api.core.domain.model.command;

public record AprovarSolicitacaoCommand(
        Long solicitacaoId,
        Long decisorId,
        String justificativaDecisao,
        String justificativaEspecial
) {
}
