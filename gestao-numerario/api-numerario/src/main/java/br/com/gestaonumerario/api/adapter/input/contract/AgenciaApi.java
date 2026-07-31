package br.com.gestaonumerario.api.adapter.input.contract;

import br.com.gestaonumerario.api.adapter.input.controller.dto.request.AtualizarAgenciaRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.CriarAgenciaRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.AgenciaResponse;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.DetalheAgenciaResponse;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.PaginaResponse;
import br.com.gestaonumerario.api.core.domain.enums.CampoOrdenacaoAgencia;
import br.com.gestaonumerario.api.core.domain.enums.DirecaoOrdenacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;

@Tag(
        name = "Agências",
        description = "Cadastro, consulta, atualização e desativação de agências."
)
public interface AgenciaApi {

    @Operation(
            summary = "Criar agência",
            description = "Cadastra uma agência ativa com limites e saldo inicial validados."
    )
    ResponseEntity<AgenciaResponse> criar(@Valid CriarAgenciaRequest request);

    @Operation(
            summary = "Listar agências",
            description = "Consulta paginada com busca, situação, alerta de saldo e ordenação opcionais."
    )
    PaginaResponse<AgenciaResponse> listar(
            String busca,
            Boolean ativo,
            Boolean alerta,
            CampoOrdenacaoAgencia ordenarPor,
            DirecaoOrdenacao direcao,
            @Min(0) int pagina,
            @Min(1) @Max(100) int tamanho);

    @Operation(
            summary = "Consultar agência por identificador",
            description = "Retorna os dados cadastrais e financeiros atuais da agência."
    )
    AgenciaResponse buscarPorId(Long agenciaId);

    @Operation(
            summary = "Detalhar agência",
            description = "Retorna a agência e os indicadores financeiros consolidados do dia."
    )
    DetalheAgenciaResponse detalhar(Long agenciaId);

    @Operation(
            summary = "Atualizar agência",
            description = "Atualiza os dados permitidos usando controle otimista de versão."
    )
    AgenciaResponse atualizar(Long agenciaId, @Valid AtualizarAgenciaRequest request);

    @Operation(
            summary = "Desativar agência",
            description = "Realiza exclusão lógica; o histórico financeiro permanece preservado."
    )
    ResponseEntity<Void> desativar(Long agenciaId);
}
