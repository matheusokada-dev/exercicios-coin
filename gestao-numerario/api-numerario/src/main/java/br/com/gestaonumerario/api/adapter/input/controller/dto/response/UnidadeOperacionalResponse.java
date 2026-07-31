package br.com.gestaonumerario.api.adapter.input.controller.dto.response;

import br.com.gestaonumerario.api.core.domain.enums.TipoUnidadeOperacional;
import java.math.BigDecimal;
import java.time.Instant;

public record UnidadeOperacionalResponse(
        Long id,
        TipoUnidadeOperacional tipo,
        String codigo,
        String nome,
        BigDecimal saldoAtual,
        long versao,
        Instant atualizadoEm
) {
}
