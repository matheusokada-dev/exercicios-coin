package br.com.gestaonumerario.api.adapter.input.contract;

import br.com.gestaonumerario.api.adapter.input.controller.dto.response.DashboardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Dashboard", description = "Indicadores consolidados da operação de numerário.")
public interface DashboardApi {

    @Operation(
            summary = "Consultar dashboard",
            description = "Retorna saldos, alertas e solicitações pendentes para acompanhamento operacional."
    )
    DashboardResponse consultar();
}
