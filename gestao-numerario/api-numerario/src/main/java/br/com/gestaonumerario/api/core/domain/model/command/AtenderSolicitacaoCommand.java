package br.com.gestaonumerario.api.core.domain.model.command;

public record AtenderSolicitacaoCommand(Long solicitacaoId, Long usuarioId, String idempotencyKey) {
}
