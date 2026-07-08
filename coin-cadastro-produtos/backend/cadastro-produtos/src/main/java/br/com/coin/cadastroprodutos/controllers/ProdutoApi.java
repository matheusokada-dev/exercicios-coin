package br.com.coin.cadastroprodutos.controllers;

import br.com.coin.cadastroprodutos.dtos.ProdutoFiltroDtoRequest;
import br.com.coin.cadastroprodutos.dtos.ProdutoDtoRequest;
import br.com.coin.cadastroprodutos.dtos.ProdutoDtoResponse;
import br.com.coin.cadastroprodutos.dtos.ProdutoUpdateDtoRequest;
import br.com.coin.cadastroprodutos.exceptions.ErrorObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Produtos", description = "API de domínio para cadastro, listagem, alteração e exclusão lógica de produtos")
@RequestMapping("/produtos")
public interface ProdutoApi {

    @Operation(summary = "Cadastrar produto")
    @ApiResponse(responseCode = "201", description = "Produto criado com sucesso")
    @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErrorObject.class))
    )
    @PostMapping
    ResponseEntity<ProdutoDtoResponse> criar(@Valid @RequestBody ProdutoDtoRequest dto);

    @Operation(
            summary = "Listar produtos",
            description = "Retorna produtos ativos e inativos com filtros, ordenação e paginação."
    )
    @ApiResponse(responseCode = "200", description = "Produtos listados com sucesso")
    @ApiResponse(
            responseCode = "400",
            description = "Filtro inválido",
            content = @Content(schema = @Schema(implementation = ErrorObject.class))
    )
    @GetMapping
    Page<ProdutoDtoResponse> listar(
            @Parameter(description = "Filtros de produto")
            @Valid @ModelAttribute ProdutoFiltroDtoRequest filtro,
            @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    );

    @Operation(summary = "Buscar produto por ID")
    @ApiResponse(responseCode = "200", description = "Produto encontrado")
    @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorObject.class))
    )
    @GetMapping("/{id}")
    ResponseEntity<ProdutoDtoResponse> buscarPorId(
            @Parameter(description = "ID do produto", example = "1")
            @PathVariable Long id
    );

    @Operation(summary = "Atualizar produto")
    @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso")
    @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = ErrorObject.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorObject.class))
    )
    @PutMapping("/{id}")
    ResponseEntity<ProdutoDtoResponse> atualizar(
            @Parameter(description = "ID do produto", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ProdutoUpdateDtoRequest dto
    );

    @Operation(
            summary = "Desativar produto",
            description = "Faz exclusão lógica alterando o status do produto para inativo."
    )
    @ApiResponse(responseCode = "204", description = "Produto desativado com sucesso")
    @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado",
            content = @Content(schema = @Schema(implementation = ErrorObject.class))
    )
    @DeleteMapping("/{id}")
    ResponseEntity<Void> desativar(
            @Parameter(description = "ID do produto", example = "1")
            @PathVariable Long id
    );
}
