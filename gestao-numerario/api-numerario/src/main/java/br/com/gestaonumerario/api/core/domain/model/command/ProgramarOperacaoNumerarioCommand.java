package br.com.gestaonumerario.api.core.domain.model.command;

public record ProgramarOperacaoNumerarioCommand(
        Long solicitacaoId, Long unidadeFaltanteId, long versaoSolicitacao,
        Long usuarioId, String idempotencyKey) {}
