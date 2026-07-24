package br.com.gestaonumerario.api.core.domain.model.command;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SolicitarAbastecimentoCommand(
        Long agenciaId,
        BigDecimal valor,
        String motivo,
        LocalDate dataDesejada,
        Long solicitanteId
) {
}
