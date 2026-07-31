package br.com.gestaonumerario.api.adapter.input.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtenderSolicitacaoRequest(@NotBlank @Size(max = 80) String idempotencyKey) {
}
