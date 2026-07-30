package br.com.gestaonumerario.relatorios.exception;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RelatorioExceptionHandler {

    @ExceptionHandler(RelatorioInvalidoException.class)
    ProblemDetail handleRelatorioInvalido(RelatorioInvalidoException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
        problem.setType(URI.create("about:blank"));
        problem.setTitle("Bad Request");
        problem.setProperty("code", "GerarRelatorioUseCase.gerar.BAD_REQUEST");
        return problem;
    }
}
