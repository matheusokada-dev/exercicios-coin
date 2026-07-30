package br.com.gestaonumerario.api.adapter.input.contract;

import br.com.gestaonumerario.api.adapter.input.controller.dto.response.OperacaoNumerarioResponse;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.PaginaResponse;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.UnidadeOperacionalResponse;
import br.com.gestaonumerario.api.core.domain.enums.StatusOperacaoNumerario;
import br.com.gestaonumerario.api.core.domain.enums.TipoUnidadeOperacional;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;

@Tag(
        name = "Consultas operacionais",
        description = "Unidades e operações usadas no fluxo evoluído de Tesouraria."
)
public interface ConsultaNumerarioApi {

    @Operation(
            summary = "Consultar unidades operacionais",
            description = "Lista Tesouraria e agências, com filtro opcional pelo tipo da unidade."
    )
    List<UnidadeOperacionalResponse> unidades(TipoUnidadeOperacional tipo);

    @Operation(
            summary = "Consultar operações de numerário",
            description = "Retorna operações logísticas paginadas por situação, origem, destino e período."
    )
    PaginaResponse<OperacaoNumerarioResponse> operacoes(
            StatusOperacaoNumerario status,
            Long origemId,
            Long destinoId,
            LocalDate dataInicio,
            LocalDate dataFim,
            @Min(0) int pagina,
            @Min(1) @Max(100) int tamanho);
}
