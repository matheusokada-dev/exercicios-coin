package br.com.gestaonumerario.api.core.domain.model.command;

public record ConciliarOperacaoNumerarioCommand(
        Long solicitacaoId, String justificativa, long versaoOperacao,
        Long usuarioId, String idempotencyKey) {}
