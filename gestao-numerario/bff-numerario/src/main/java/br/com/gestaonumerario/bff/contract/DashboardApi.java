package br.com.gestaonumerario.bff.contract;

import br.com.gestaonumerario.bff.dto.DashboardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Dashboard", description = "Indicadores operacionais apresentados pelo frontend.")
public interface DashboardApi {

    @Operation(summary = "Consultar dashboard", description = "Obtém da API os saldos, alertas e pendências consolidados.")
    DashboardResponse consultar(@Parameter(hidden = true) String authorization);
}
