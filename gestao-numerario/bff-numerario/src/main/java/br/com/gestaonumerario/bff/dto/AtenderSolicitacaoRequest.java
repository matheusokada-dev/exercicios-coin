package br.com.gestaonumerario.bff.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtenderSolicitacaoRequest(
        @NotBlank @Size(max = 80) String idempotencyKey
) {
}
