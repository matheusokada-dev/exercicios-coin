package br.com.gestaonumerario.api.core.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DetalheAgencia(
        Agencia agencia,
        LocalDate dataReferencia,
        BigDecimal valorEntradasHoje,
        BigDecimal valorSaidasHoje,
        BigDecimal valorAbastecimentoAprovado,
        BigDecimal saldoPrevistoAposAbastecimentoAprovado
) {
}
