package br.com.gestaonumerario.bff.contract;

import br.com.gestaonumerario.bff.dto.AgenciaResponse;
import br.com.gestaonumerario.bff.dto.AtualizarAgenciaRequest;
import br.com.gestaonumerario.bff.dto.CriarAgenciaRequest;
import br.com.gestaonumerario.bff.dto.DetalheAgenciaResponse;
import br.com.gestaonumerario.bff.dto.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;

@Tag(name = "Agências", description = "Contrato de agências exposto ao frontend.")
public interface AgenciaApi {

    @Operation(summary = "Listar agências", description = "Consulta agências paginadas com busca, situação, alerta e ordenação opcionais.")
    PaginaResponse<AgenciaResponse> listar(@Parameter(hidden = true) String authorization, String busca,
            Boolean ativo, Boolean alerta, String ordenarPor, String direcao,
            @Min(0) int pagina, @Min(1) @Max(100) int tamanho);

    @Operation(summary = "Detalhar agência", description = "Retorna dados cadastrais e indicadores financeiros consolidados da agência.")
    DetalheAgenciaResponse detalhe(@Parameter(hidden = true) String authorization, Long id);

    @Operation(summary = "Criar agência", description = "Valida e encaminha à API o cadastro de uma nova agência.")
    ResponseEntity<AgenciaResponse> criar(
            @Parameter(hidden = true) String authorization,
            @Valid CriarAgenciaRequest request);

    @Operation(summary = "Atualizar agência", description = "Atualiza os campos permitidos preservando o controle de versão.")
    AgenciaResponse atualizar(
            @Parameter(hidden = true) String authorization,
            Long id,
            @Valid AtualizarAgenciaRequest request);

    @Operation(summary = "Desativar agência", description = "Solicita a desativação lógica da agência e preserva o histórico.")
    ResponseEntity<Void> desativar(@Parameter(hidden = true) String authorization, Long id);
}
