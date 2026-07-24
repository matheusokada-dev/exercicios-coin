package br.com.gestaonumerario.api.core.domain.model.command;

import br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao;

import java.math.BigDecimal;

public record RegistrarMovimentacaoCommand(
        Long agenciaId,
        Long usuarioId,
        TipoMovimentacao tipo,
        Boolean entradaAjuste,
        BigDecimal valor,
        String descricao,
        String idempotencyKey
) {
}

