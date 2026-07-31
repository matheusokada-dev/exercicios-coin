package br.com.gestaonumerario.api.adapter.input.controller.dto.response;

import br.com.gestaonumerario.api.core.domain.enums.StatusOperacaoNumerario;
import java.math.BigDecimal;
import java.time.Instant;

public record OperacaoNumerarioResponse(
        Long id,
        Long solicitacaoId,
        Long origemId,
        Long destinoId,
        StatusOperacaoNumerario status,
        BigDecimal valorProgramado,
        BigDecimal valorExpedido,
        BigDecimal valorRecebido,
        BigDecimal valorDivergencia,
        Instant dataProgramacao,
        Instant dataExpedicao,
        Instant dataRecebimento,
        Instant dataConciliacao,
        String justificativaDivergencia,
        String descricaoOcorrencia,
        long versao
) {
}
