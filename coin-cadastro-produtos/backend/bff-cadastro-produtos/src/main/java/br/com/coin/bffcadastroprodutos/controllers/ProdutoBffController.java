package br.com.coin.bffcadastroprodutos.controllers;

import br.com.coin.bffcadastroprodutos.dtos.frontend.request.ProdutoBffFiltroDtoRequest;
import br.com.coin.bffcadastroprodutos.dtos.frontend.response.ProdutoBffPageDtoResponse;
import br.com.coin.bffcadastroprodutos.dtos.frontend.request.ProdutoBffDtoRequest;
import br.com.coin.bffcadastroprodutos.dtos.frontend.response.ProdutoBffDtoResponse;
import br.com.coin.bffcadastroprodutos.dtos.frontend.request.ProdutoBffUpdateDtoRequest;
import br.com.coin.bffcadastroprodutos.services.ProdutoBffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProdutoBffController implements ProdutoBffApi {

    private final ProdutoBffService produtoBffService;

    @Override
    public ResponseEntity<ProdutoBffDtoResponse> criar(
            @Valid ProdutoBffDtoRequest dto
    ) {
        log.info("BFF recebeu requisicao para criar produto");
        ProdutoBffDtoResponse response = produtoBffService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<ProdutoBffPageDtoResponse<ProdutoBffDtoResponse>> listar(
            @Valid ProdutoBffFiltroDtoRequest filtro
    ) {
        log.info(
                "BFF recebeu requisicao para listar produtos page={} size={} status={}",
                filtro.page(),
                filtro.size(),
                filtro.status()
        );
        return ResponseEntity.ok(produtoBffService.listar(filtro));
    }

    @Override
    public ResponseEntity<ProdutoBffDtoResponse> buscarPorId(Long id) {
        log.info("BFF recebeu requisicao para buscar produto id={}", id);
        return ResponseEntity.ok(produtoBffService.buscarPorId(id));
    }

    @Override
    public ResponseEntity<ProdutoBffDtoResponse> atualizar(
            Long id,
            @Valid ProdutoBffUpdateDtoRequest dto
    ) {
        log.info("BFF recebeu requisicao para atualizar produto id={}", id);
        return ResponseEntity.ok(produtoBffService.atualizar(id, dto));
    }

    @Override
    public ResponseEntity<Void> desativar(Long id) {
        log.info("BFF recebeu requisicao para desativar produto id={}", id);
        produtoBffService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
