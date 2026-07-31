package br.com.gestaonumerario.api.core.domain.model.command;

import java.math.BigDecimal;

public record AtualizarAgenciaCommand(Long agenciaId, String nome, String cidade, BigDecimal limiteMinimo) {
}
