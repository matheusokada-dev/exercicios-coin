package br.com.gestaonumerario.bff.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record SolicitacaoNumerarioResponse(
        Long id,
        String tipoOperacao,
        Long agenciaId,
        Long origemId,
        Long destinoId,
        BigDecimal valorSolicitado,
        String motivo,
        LocalDate dataDesejada,
        String status,
        Long solicitanteId,
        Long aprovadorId,
        String justificativaDecisao,
        Instant dataCriacao,
        Instant dataDecisao,
        Long canceladoPorId,
        String justificativaCancelamento,
        Instant dataCancelamento,
        Instant dataConclusao,
        long versao
) {
}
