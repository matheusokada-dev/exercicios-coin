package br.com.gestaonumerario.api.adapter.input.contract;

import br.com.gestaonumerario.api.adapter.input.controller.dto.response.MovimentacaoResponse;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.PaginaResponse;
import br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;

@Tag(name = "Movimentações", description = "Razão financeiro imutável das agências.")
public interface MovimentacaoApi {

    @Operation(
            summary = "Consultar movimentações",
            description = "Lista movimentações paginadas por agência, tipo e período para auditoria e Livro Caixa."
    )
    PaginaResponse<MovimentacaoResponse> consultar(
            Long agenciaId,
            TipoMovimentacao tipo,
            LocalDate dataInicio,
            @PastOrPresent(message = "A data final não pode ser maior que a data de hoje.")
            LocalDate dataFim,
            @Min(0) int pagina,
            @Min(1) @Max(100) int tamanho);
}
