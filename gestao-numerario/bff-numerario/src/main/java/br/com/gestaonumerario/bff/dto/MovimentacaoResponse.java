package br.com.gestaonumerario.bff.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record MovimentacaoResponse(
        Long id,
        Long agenciaId,
        Long solicitacaoId,
        String tipo,
        boolean entrada,
        BigDecimal valor,
        BigDecimal saldoAnterior,
        BigDecimal saldoPosterior,
        String descricao,
        Instant dataMovimento,
        Long usuarioId
) {
}
