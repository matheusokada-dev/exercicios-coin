package br.com.gestaonumerario.api.core.domain.model;

import java.math.BigDecimal;

public record ResumoMovimentacaoDiaria(BigDecimal valorEntradas, BigDecimal valorSaidas) {
}
