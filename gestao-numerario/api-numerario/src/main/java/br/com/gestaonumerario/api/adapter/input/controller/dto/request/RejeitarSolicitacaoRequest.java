package br.com.gestaonumerario.api.adapter.input.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejeitarSolicitacaoRequest(@NotBlank @Size(max = 500) String justificativaDecisao) {
}
