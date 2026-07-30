package br.com.gestaonumerario.bff.contract;

import br.com.gestaonumerario.bff.dto.MovimentacaoResponse;
import br.com.gestaonumerario.bff.dto.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;

@Tag(name = "Movimentações", description = "Consulta do razão financeiro usada pela auditoria e pelo Livro Caixa.")
public interface MovimentacaoApi {

    @Operation(summary = "Listar movimentações", description = "Consulta movimentações paginadas por agência, tipo e período.")
    PaginaResponse<MovimentacaoResponse> listar(@Parameter(hidden = true) String authorization, Long agenciaId,
            String tipo,
            LocalDate dataInicio,
            @PastOrPresent(message = "A data final não pode ser maior que a data de hoje.") LocalDate dataFim,
            @Min(0) int pagina,
            @Min(1) @Max(100) int tamanho);
}
