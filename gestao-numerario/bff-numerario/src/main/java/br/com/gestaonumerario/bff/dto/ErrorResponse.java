package br.com.gestaonumerario.bff.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(int codError, String msgError, String value) {
}
