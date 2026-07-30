package br.com.gestaonumerario.api.adapter.input.error;

import java.time.Instant;

public record DetalheFalhaAutenticacao(Integer tentativasRestantes, Instant bloqueadoAte) {
}
