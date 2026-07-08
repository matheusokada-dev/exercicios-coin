package br.com.coin.bffcadastroprodutos.controllers;

import br.com.coin.bffcadastroprodutos.dtos.frontend.ProdutoBffFiltroDtoRequest;
import br.com.coin.bffcadastroprodutos.dtos.frontend.ProdutoBffPageDtoResponse;
import br.com.coin.bffcadastroprodutos.dtos.frontend.ProdutoBffDtoRequest;
import br.com.coin.bffcadastroprodutos.dtos.frontend.ProdutoBffDtoResponse;
import br.com.coin.bffcadastroprodutos.dtos.frontend.ProdutoBffUpdateDtoRequest;
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
    public ResponseEntity<ProdutoBffDtoResponse> criar(
            @Valid ProdutoBffDtoRequest dto
    ) {
        ProdutoBffDtoResponse response = produtoBffService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<ProdutoBffPageDtoResponse<ProdutoBffDtoResponse>> listar(
            @Valid ProdutoBffFiltroDtoRequest filtro
    ) {
        return ResponseEntity.ok(produtoBffService.listar(filtro));
    }

    @Override
    public ResponseEntity<ProdutoBffDtoResponse> buscarPorId(Long id) {
        return ResponseEntity.ok(produtoBffService.buscarPorId(id));
    }

    @Override
    public ResponseEntity<ProdutoBffDtoResponse> atualizar(
            Long id,
            @Valid ProdutoBffUpdateDtoRequest dto
    ) {
        return ResponseEntity.ok(produtoBffService.atualizar(id, dto));
    }

    @Override
    public ResponseEntity<Void> desativar(Long id) {
        produtoBffService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
