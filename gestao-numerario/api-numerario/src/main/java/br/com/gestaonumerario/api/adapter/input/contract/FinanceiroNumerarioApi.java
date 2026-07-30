package br.com.gestaonumerario.api.adapter.input.contract;

import br.com.gestaonumerario.api.adapter.input.auth.UsuarioAutenticado;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.AjustarDivergenciaRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.CargaInicialTesourariaRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.UnidadeOperacionalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(
        name = "Financeiro da Tesouraria",
        description = "Comandos financeiros administrativos e auditáveis."
)
public interface FinanceiroNumerarioApi {

    @Operation(
            summary = "Realizar carga inicial da Tesouraria",
            description = "Operação única. Exige exatamente uma Tesouraria ativa."
    )
    UnidadeOperacionalResponse cargaInicial(
            @Valid CargaInicialTesourariaRequest request,
            @Parameter(
                    name = "Idempotency-Key",
                    required = true,
                    description = "Chave única da tentativa"
            ) String idempotencyKey,
            @Parameter(hidden = true) UsuarioAutenticado usuario);

    @Operation(
            summary = "Ajustar divergência financeira",
            description = "O valor não pode exceder a divergência e a unidade deve pertencer à rota."
    )
    UnidadeOperacionalResponse ajustar(
            Long id,
            @Valid AjustarDivergenciaRequest request,
            @Parameter(
                    name = "Idempotency-Key",
                    required = true,
                    description = "Chave única da tentativa"
            ) String idempotencyKey,
            @Parameter(hidden = true) UsuarioAutenticado usuario);
}
