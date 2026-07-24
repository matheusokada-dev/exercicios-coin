package br.com.gestaonumerario.api.adapter.input.controller.dto.response;

import java.math.BigDecimal;

public record AgenciaResponse(
        Long id, String codigo, String nome, String cidade, BigDecimal saldoAtual,
        BigDecimal limiteMinimo, boolean ativo, boolean abaixoDoLimite,
        BigDecimal sugestaoAbastecimento, long versao
) {
}

