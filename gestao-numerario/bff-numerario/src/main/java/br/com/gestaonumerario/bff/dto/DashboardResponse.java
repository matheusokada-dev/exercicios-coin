package br.com.gestaonumerario.bff.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardResponse(
        LocalDate dataReferencia,
        BigDecimal numerarioTotal,
        long quantidadeAgenciasEmAlerta,
        long quantidadeSolicitacoesPendentes,
        long quantidadeAbastecimentosHoje,
        BigDecimal valorAbastecidoHoje
) {
}
