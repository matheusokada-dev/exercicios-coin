package br.com.gestaonumerario.api.adapter.input.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AprovarSolicitacaoRequest(
        @NotBlank @Size(max = 500) String justificativaDecisao,
        @Size(max = 500) String justificativaEspecial
) {
}

