package br.com.gestaonumerario.api.core.domain.model.command;

import java.math.BigDecimal;

public record CriarAgenciaCommand(
        String codigo,
        String nome,
        String cidade,
        BigDecimal saldoAtual,
        BigDecimal limiteMinimo
) {
}
