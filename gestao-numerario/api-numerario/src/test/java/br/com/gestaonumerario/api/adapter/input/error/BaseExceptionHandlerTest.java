package br.com.gestaonumerario.api.adapter.input.error;

import br.com.gestaonumerario.api.core.exception.AgenciaNaoEncontradaException;
import br.com.gestaonumerario.api.core.exception.CampoObrigatorioException;
import br.com.gestaonumerario.api.core.exception.CredenciaisInvalidasException;
import br.com.gestaonumerario.api.core.exception.RegraOperacaoNumerarioException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BaseExceptionHandlerTest {

    private final BaseExceptionHandler handler = new BaseExceptionHandler();

    @Test
    void retornaContratoRestCompletoSemRemoverCamposLegados() {
        var request = new MockHttpServletRequest("GET", "/api/v1/agencias/999");

        var response = handler.handleBaseException(new AgenciaNaoEncontradaException(), request);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCodError()).isEqualTo(3000);
        assertThat(response.getBody().getMsgError()).isEqualTo(response.getBody().getMessage());
        assertThat(response.getBody().getCode()).isEqualTo("AGENCIA_NAO_ENCONTRADA");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/agencias/999");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void identificaCampoComTipoInvalido() {
        var request = new MockHttpServletRequest("GET", "/api/v1/agencias");
        var exception = new MethodArgumentTypeMismatchException(
                "abc", Long.class, "agenciaId", null, new IllegalArgumentException("tipo inválido"));

        var response = handler.handleMethodArgumentTypeMismatchException(exception, request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getValue()).isEqualTo("agenciaId");
        assertThat(response.getBody().getFields())
                .containsExactly(new FieldErrorObject(
                        "agenciaId",
                        "O valor informado para 'agenciaId' é inválido."));
    }

    @Test
    void identificaCabecalhoObrigatorioAusente() {
        var response = handler.handleCabecalhoAusente(
                new MissingRequestHeaderException("Idempotency-Key", null),
                new MockHttpServletRequest("PUT", "/api/v1/solicitacoes-numerario/1/expedir"));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getValue()).isEqualTo("Idempotency-Key");
        assertThat(response.getBody().getFields()).containsExactly(
                new FieldErrorObject(
                        "Idempotency-Key",
                        "O cabeçalho 'Idempotency-Key' é obrigatório."));
    }

    @Test
    void preservaCampoERegraEspecificosDoDominio() {
        var campo = handler.handleBaseException(
                new CampoObrigatorioException("agenciaId"),
                new MockHttpServletRequest("POST", "/api/v1/solicitacoes-numerario"));
        assertThat(campo.getBody().getMessage()).isEqualTo(
                "O campo 'agenciaId' é obrigatório.");
        assertThat(campo.getBody().getFields()).containsExactly(
                new FieldErrorObject("agenciaId", "O campo 'agenciaId' é obrigatório."));

        var regra = handler.handleBaseException(
                new RegraOperacaoNumerarioException(
                        "A carga inicial da Tesouraria já foi realizada."),
                new MockHttpServletRequest("POST", "/api/v1/tesouraria/carga-inicial"));
        assertThat(regra.getStatusCode().value()).isEqualTo(422);
        assertThat(regra.getBody().getMessage()).isEqualTo(
                "A carga inicial da Tesouraria já foi realizada.");
    }

    @Test
    void retornaConflitoParaAtualizacaoConcorrente() {
        var response = handler.handleConflitoOtimista(
                new ObjectOptimisticLockingFailureException("Agencia", 1L),
                new MockHttpServletRequest("PUT", "/api/v1/agencias/1"));

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody().getCode()).isEqualTo("CONFLITO_VERSAO");
    }

    @Test
    void informaTentativasRestantesEmCredenciaisInvalidas() {
        Instant bloqueadoAte = Instant.parse("2026-07-25T12:10:00Z");

        var response = handler.handleCredenciaisInvalidas(
                new CredenciaisInvalidasException(2, bloqueadoAte),
                new MockHttpServletRequest("POST", "/api/v1/auth/login"));

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody().getValue().tentativasRestantes()).isEqualTo(2);
        assertThat(response.getBody().getValue().bloqueadoAte()).isEqualTo(bloqueadoAte);
        assertThat(handler.handleCredenciaisInvalidas(
                new CredenciaisInvalidasException(), new MockHttpServletRequest())
                .getBody().getValue()).isNull();
    }

    @Test
    void converteViolacoesDeConstraintEmErrosDeCampo() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("consultar.filtro.dataInicial");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("Data inválida.");

        var response = handler.handleConstraintViolationException(
                new ConstraintViolationException(Set.of(violation)),
                new MockHttpServletRequest("GET", "/api/v1/movimentacoes"));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getFields())
                .containsExactly(new FieldErrorObject("dataInicial", "Data inválida."));
    }

    @Test
    void converteBeanValidationEmContratoPadronizado() throws NoSuchMethodException {
        var binding = new BeanPropertyBindingResult(new Object(), "request");
        binding.addError(new FieldError("request", "nome", "Nome obrigatório."));
        var parameter = new MethodParameter(
                BaseExceptionHandlerTest.class.getDeclaredMethod("metodoParaValidacao", Object.class), 0);

        var response = handler.handleValidationException(
                new MethodArgumentNotValidException(parameter, binding),
                new MockHttpServletRequest("POST", "/api/v1/agencias"));

        assertThat(response.getBody().getValue()).isEqualTo("nome");
        assertThat(response.getBody().getFields())
                .containsExactly(new FieldErrorObject("nome", "Nome obrigatório."));
    }

    @Test
    void explicaCadaCategoriaDeBeanValidation() throws NoSuchMethodException {
        var binding = new BeanPropertyBindingResult(new Object(), "request");
        adicionarErro(binding, "nome", "NotBlank");
        adicionarErro(binding, "agenciaId", "Positive");
        adicionarErro(binding, "versao", "PositiveOrZero");
        adicionarErro(binding, "valor", "DecimalMin");
        adicionarErro(binding, "saldo", "Digits");
        adicionarErro(binding, "motivo", "Size");
        adicionarErro(binding, "dataDesejada", "FutureOrPresent");
        adicionarErro(binding, "pagina", "Min");
        adicionarErro(binding, "campoDesconhecido", "RegraCustomizada");
        var parameter = new MethodParameter(
                BaseExceptionHandlerTest.class.getDeclaredMethod(
                        "metodoParaValidacao", Object.class), 0);

        var response = handler.handleValidationException(
                new MethodArgumentNotValidException(parameter, binding),
                new MockHttpServletRequest("POST", "/api/v1/solicitacoes-numerario"));

        assertThat(response.getBody().getFields())
                .extracting(FieldErrorObject::message)
                .containsExactly(
                        "O campo 'nome' é obrigatório.",
                        "O campo 'agenciaId' deve ser maior que zero.",
                        "O campo 'versao' não pode ser negativo.",
                        "O campo 'valor' deve respeitar o valor mínimo permitido.",
                        "O campo 'saldo' possui precisão ou casas decimais inválidas.",
                        "O campo 'motivo' possui tamanho inválido.",
                        "O campo 'dataDesejada' não pode estar no passado.",
                        "O campo 'pagina' está fora do intervalo permitido.",
                        "Mensagem alternativa.");
    }

    @Test
    void trataErroInesperadoSemExporDetalhesInternos() {
        var response = handler.handleUnexpectedException(
                new IllegalStateException("segredo interno"),
                new MockHttpServletRequest("GET", "/api/v1/recurso"));

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).doesNotContain("segredo interno");
        assertThat(response.getBody().getFields()).isNull();
    }

    @SuppressWarnings("unused")
    private void metodoParaValidacao(Object request) {
    }

    private static void adicionarErro(
            BeanPropertyBindingResult binding,
            String campo,
            String codigo) {
        binding.addError(new FieldError(
                "request",
                campo,
                null,
                false,
                new String[]{codigo},
                null,
                "Mensagem alternativa."));
    }
}
