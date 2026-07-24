package br.com.gestaonumerario.bff.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record SolicitacaoResponse(
        Long id,
        Long agenciaId,
        BigDecimal valor,
        String motivo,
        LocalDate dataDesejada,
        String status,
        Long solicitanteId,
        Long decisorId,
        String justificativaDecisao,
        String justificativaEspecial,
        Instant dataCriacao,
        Instant dataDecisao,
        Instant dataAtendimento,
        long versao
) {
}
