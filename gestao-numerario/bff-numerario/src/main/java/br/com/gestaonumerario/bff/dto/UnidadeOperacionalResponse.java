package br.com.gestaonumerario.bff.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record UnidadeOperacionalResponse(
        Long id,
        String tipo,
        String codigo,
        String nome,
        BigDecimal saldoAtual,
        long versao,
        Instant atualizadoEm
) {
}
