package br.com.gestaonumerario.api.adapter.input.controller.dto.response;

import br.com.gestaonumerario.api.core.domain.enums.*;
import java.math.BigDecimal;
import java.time.*;

public record SolicitacaoNumerarioResponse(
        Long id, TipoOperacaoNumerario tipoOperacao, Long agenciaId,
        Long origemId, Long destinoId, BigDecimal valorSolicitado,
        String motivo, LocalDate dataDesejada, StatusSolicitacaoNumerario status,
        Long solicitanteId, Long aprovadorId, String justificativaDecisao,
        Instant dataCriacao, Instant dataDecisao, Long canceladoPorId,
        String justificativaCancelamento, Instant dataCancelamento,
        Instant dataConclusao, long versao) {}
