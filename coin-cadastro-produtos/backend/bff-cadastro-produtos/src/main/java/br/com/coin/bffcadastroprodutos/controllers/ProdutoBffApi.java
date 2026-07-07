package br.com.coin.bffcadastroprodutos.controllers;

import br.com.coin.bffcadastroprodutos.dtos.bff.BffErrorDTO;
import br.com.coin.bffcadastroprodutos.dtos.bff.ProdutoBffFiltroDTO;
import br.com.coin.bffcadastroprodutos.dtos.bff.ProdutoBffPageResponseDTO;
import br.com.coin.bffcadastroprodutos.dtos.bff.ProdutoBffRequestDTO;
import br.com.coin.bffcadastroprodutos.dtos.bff.ProdutoBffResponseDTO;
import br.com.coin.bffcadastroprodutos.dtos.bff.ProdutoBffUpdateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "BFF Produtos", description = "Endpoints da BFF consumidos pelo frontend de cadastro de produtos")
@RequestMapping("/api/bff/produtos")
public interface ProdutoBffApi {

    @Operation(
            summary = "Cadastrar produto",
            description = "Recebe os dados do frontend, encaminha para o backend de produtos e retorna o produto criado."
    )
    @ApiResponse(responseCode = "201", description = "Produto criado com sucesso")
    @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = BffErrorDTO.class))
    )
    @ApiResponse(
            responseCode = "503",
            description = "Serviço de produtos indisponível",
            content = @Content(schema = @Schema(implementation = BffErrorDTO.class))
    )
    @PostMapping
    ResponseEntity<ProdutoBffResponseDTO> criar(@Valid @RequestBody ProdutoBffRequestDTO dto);

    @Operation(
            summary = "Listar produtos",
            description = "Lista produtos com busca, status, faixa de preço, ordenação e paginação."
    )
    @ApiResponse(responseCode = "200", description = "Produtos listados com sucesso")
    @ApiResponse(
            responseCode = "400",
            description = "Filtro inválido",
            content = @Content(schema = @Schema(implementation = BffErrorDTO.class))
    )
    @ApiResponse(
            responseCode = "503",
            description = "Serviço de produtos indisponível",
            content = @Content(schema = @Schema(implementation = BffErrorDTO.class))
    )
    @GetMapping
    ResponseEntity<ProdutoBffPageResponseDTO<ProdutoBffResponseDTO>> listar(
            @Parameter(description = "Filtros de listagem de produtos")
            @Valid @ModelAttribute ProdutoBffFiltroDTO filtro
    );

    @Operation(summary = "Buscar produto por ID")
    @ApiResponse(responseCode = "200", description = "Produto encontrado")
    @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado",
            content = @Content(schema = @Schema(implementation = BffErrorDTO.class))
    )
    @GetMapping("/{id}")
    ResponseEntity<ProdutoBffResponseDTO> buscarPorId(
            @Parameter(description = "ID do produto", example = "1")
            @PathVariable Long id
    );

    @Operation(summary = "Atualizar produto")
    @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso")
    @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos",
            content = @Content(schema = @Schema(implementation = BffErrorDTO.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado",
            content = @Content(schema = @Schema(implementation = BffErrorDTO.class))
    )
    @PutMapping("/{id}")
    ResponseEntity<ProdutoBffResponseDTO> atualizar(
            @Parameter(description = "ID do produto", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ProdutoBffUpdateDTO dto
    );

    @Operation(
            summary = "Desativar produto",
            description = "Executa exclusão lógica marcando o produto como inativo."
    )
    @ApiResponse(responseCode = "204", description = "Produto desativado com sucesso")
    @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado",
            content = @Content(schema = @Schema(implementation = BffErrorDTO.class))
    )
    @DeleteMapping("/{id}")
    ResponseEntity<Void> desativar(
            @Parameter(description = "ID do produto", example = "1")
            @PathVariable Long id
    );
}
