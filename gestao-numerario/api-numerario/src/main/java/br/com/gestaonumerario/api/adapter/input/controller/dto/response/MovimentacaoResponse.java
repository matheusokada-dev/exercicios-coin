package br.com.gestaonumerario.api.adapter.input.controller.dto.response;

import br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao;

import java.math.BigDecimal;
import java.time.Instant;

public record MovimentacaoResponse(
        Long id, Long agenciaId, Long solicitacaoId, TipoMovimentacao tipo, boolean entrada,
        BigDecimal valor, BigDecimal saldoAnterior, BigDecimal saldoPosterior, String descricao,
        Instant dataMovimento, Long usuarioId
) {
}

