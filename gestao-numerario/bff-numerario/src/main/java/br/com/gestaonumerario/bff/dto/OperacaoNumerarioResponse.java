package br.com.gestaonumerario.bff.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OperacaoNumerarioResponse(
        Long id,
        Long solicitacaoId,
        Long origemId,
        Long destinoId,
        String status,
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
