package br.com.gestaonumerario.api.adapter.input.contract;

import br.com.gestaonumerario.api.adapter.input.auth.UsuarioAutenticado;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.*;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.OperacaoNumerarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Operações de numerário",
        description = "Programação, separação, expedição, recebimento, ocorrência e conciliação.")
public interface OperacaoNumerarioApi {

    @Operation(summary = "Programar operação",
            description = "Define a unidade de origem ou destino que faltava na solicitação aprovada.")
    OperacaoNumerarioResponse programar(
            Long id, @Valid ProgramarOperacaoNumerarioRequest request,
            @Parameter(name = "Idempotency-Key", required = true,
                    description = "Chave única da tentativa",
                    example = "2a0ea555-52ef-42b0-aad2-70827116e997")
            String idempotencyKey,
            @Parameter(hidden = true) UsuarioAutenticado usuario);

    @Operation(summary = "Iniciar separação")
    OperacaoNumerarioResponse separar(
            Long id, @Valid VersaoOperacaoNumerarioRequest request,
            @Parameter(hidden = true) UsuarioAutenticado usuario);

    @Operation(summary = "Expedir numerário",
            description = "Debita a origem e registra o valor como numerário em trânsito.")
    OperacaoNumerarioResponse expedir(
            Long id, @Valid ExecutarOperacaoNumerarioRequest request,
            @Parameter(name = "Idempotency-Key", required = true,
                    description = "Chave única da tentativa")
            String idempotencyKey,
            @Parameter(hidden = true) UsuarioAutenticado usuario);

    @Operation(summary = "Registrar ocorrência logística")
    OperacaoNumerarioResponse ocorrencia(
            Long id, @Valid OcorrenciaOperacaoNumerarioRequest request,
            @Parameter(hidden = true) UsuarioAutenticado usuario);

    @Operation(summary = "Confirmar recebimento",
            description = "Credita o destino. Valor diferente do expedido abre divergência.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(examples = @ExampleObject(value = """
                    {
                      "valorRecebido": 1050.00,
                      "justificativaDivergencia": null,
                      "versaoOperacao": 2,
                      "versaoUnidade": 4
                    }
                    """)))
    OperacaoNumerarioResponse receber(
            Long id, @Valid ReceberOperacaoNumerarioRequest request,
            @Parameter(name = "Idempotency-Key", required = true,
                    description = "Chave única da tentativa")
            String idempotencyKey,
            @Parameter(hidden = true) UsuarioAutenticado usuario);

    @Operation(summary = "Conciliar divergência",
            description = "Conclui a análise de uma operação recebida com divergência.")
    OperacaoNumerarioResponse conciliar(
            Long id, @Valid ConciliarOperacaoNumerarioRequest request,
            @Parameter(name = "Idempotency-Key", required = true,
                    description = "Chave única da tentativa")
            String idempotencyKey,
            @Parameter(hidden = true) UsuarioAutenticado usuario);
}
