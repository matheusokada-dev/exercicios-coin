package br.com.gestaonumerario.api.adapter.input.controller.dto.response;

import br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacao;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record SolicitacaoAbastecimentoResponse(
        Long id,
        Long agenciaId,
        BigDecimal valor,
        String motivo,
        LocalDate dataDesejada,
        StatusSolicitacao status,
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

