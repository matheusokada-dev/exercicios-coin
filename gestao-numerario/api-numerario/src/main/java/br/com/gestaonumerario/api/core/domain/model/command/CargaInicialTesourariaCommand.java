package br.com.gestaonumerario.api.core.domain.model.command;

import java.math.BigDecimal;

public record CargaInicialTesourariaCommand(
        BigDecimal valor,
        String justificativa,
        long versaoUnidade,
        Long usuarioId,
        String idempotencyKey
) {
}
