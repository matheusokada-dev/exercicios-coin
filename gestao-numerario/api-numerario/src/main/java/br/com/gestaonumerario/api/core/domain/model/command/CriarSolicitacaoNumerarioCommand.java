package br.com.gestaonumerario.api.core.domain.model.command;

import br.com.gestaonumerario.api.core.domain.enums.TipoOperacaoNumerario;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CriarSolicitacaoNumerarioCommand(
        TipoOperacaoNumerario tipo, Long agenciaId, BigDecimal valor,
        String motivo, LocalDate dataDesejada, Long solicitanteId) {}
