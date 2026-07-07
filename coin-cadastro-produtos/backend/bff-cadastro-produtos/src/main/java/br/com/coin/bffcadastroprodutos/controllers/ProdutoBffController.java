package br.com.coin.bffcadastroprodutos.controllers;

import br.com.coin.bffcadastroprodutos.dtos.bff.ProdutoBffFiltroDTO;
import br.com.coin.bffcadastroprodutos.dtos.bff.ProdutoBffPageResponseDTO;
import br.com.coin.bffcadastroprodutos.dtos.bff.ProdutoBffRequestDTO;
import br.com.coin.bffcadastroprodutos.dtos.bff.ProdutoBffResponseDTO;
import br.com.coin.bffcadastroprodutos.dtos.bff.ProdutoBffUpdateDTO;
import br.com.coin.bffcadastroprodutos.services.ProdutoBffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProdutoBffController implements ProdutoBffApi {

    private final ProdutoBffService produtoBffService;

    @Override
    public ResponseEntity<ProdutoBffResponseDTO> criar(
            @Valid ProdutoBffRequestDTO dto
    ) {
        ProdutoBffResponseDTO response = produtoBffService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<ProdutoBffPageResponseDTO<ProdutoBffResponseDTO>> listar(
            @Valid ProdutoBffFiltroDTO filtro
    ) {
        return ResponseEntity.ok(produtoBffService.listar(filtro));
    }

    @Override
    public ResponseEntity<ProdutoBffResponseDTO> buscarPorId(Long id) {
        return ResponseEntity.ok(produtoBffService.buscarPorId(id));
    }

    @Override
    public ResponseEntity<ProdutoBffResponseDTO> atualizar(
            Long id,
            @Valid ProdutoBffUpdateDTO dto
    ) {
        return ResponseEntity.ok(produtoBffService.atualizar(id, dto));
    }

    @Override
    public ResponseEntity<Void> desativar(Long id) {
        produtoBffService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
