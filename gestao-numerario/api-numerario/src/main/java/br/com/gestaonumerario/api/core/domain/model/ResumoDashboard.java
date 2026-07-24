package br.com.gestaonumerario.api.core.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResumoDashboard(
        LocalDate dataReferencia,
        BigDecimal numerarioTotal,
        long quantidadeAgenciasEmAlerta,
        long quantidadeSolicitacoesPendentes,
        long quantidadeAbastecimentosHoje,
        BigDecimal valorAbastecidoHoje
) {
}
