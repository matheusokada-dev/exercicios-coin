package br.com.gestaonumerario.bff.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DetalheAgenciaResponse(
        AgenciaResponse agencia,
        LocalDate dataReferencia,
        BigDecimal valorEntradasHoje,
        BigDecimal valorSaidasHoje,
        BigDecimal valorAbastecimentoAprovado,
        BigDecimal saldoPrevistoAposAbastecimentoAprovado
) {
}
