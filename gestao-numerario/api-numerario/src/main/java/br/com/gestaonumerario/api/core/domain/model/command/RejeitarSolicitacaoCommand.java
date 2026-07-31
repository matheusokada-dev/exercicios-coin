package br.com.gestaonumerario.api.core.domain.model.command;

public record RejeitarSolicitacaoCommand(Long solicitacaoId, Long decisorId, String justificativaDecisao) {
}
