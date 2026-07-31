package br.com.gestaonumerario.api.adapter.input.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorObject<T> {

    private final int codError;
    private final String msgError;
    private final T value;
    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String code;
    private final String message;
    private final String path;
    private final List<FieldErrorObject> fields;
}
