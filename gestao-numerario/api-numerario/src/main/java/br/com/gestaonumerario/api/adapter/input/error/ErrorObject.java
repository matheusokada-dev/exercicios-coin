package br.com.gestaonumerario.api.adapter.input.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorObject<T> {

    private final int codError;
    private final String msgError;
    private final T value;
}
