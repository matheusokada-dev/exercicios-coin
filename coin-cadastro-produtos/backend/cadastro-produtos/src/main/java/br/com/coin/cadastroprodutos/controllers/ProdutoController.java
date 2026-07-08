package br.com.coin.cadastroprodutos.controllers;

import br.com.coin.cadastroprodutos.dtos.ProdutoFiltroDtoRequest;
import br.com.coin.cadastroprodutos.dtos.ProdutoDtoRequest;
import br.com.coin.cadastroprodutos.dtos.ProdutoDtoResponse;
import br.com.coin.cadastroprodutos.dtos.ProdutoUpdateDtoRequest;
import br.com.coin.cadastroprodutos.services.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProdutoController implements ProdutoApi {

    private final ProdutoService produtoService;

    @Override
    public ResponseEntity<ProdutoDtoResponse> criar(@Valid ProdutoDtoRequest dto) {
        log.info("Recebida solicitacao para criar produto");
        ProdutoDtoResponse produtoCriado = produtoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoCriado);
    }

    @Override
    public Page<ProdutoDtoResponse> listar(
            @Valid ProdutoFiltroDtoRequest filtro,
            Pageable pageable
    ) {
        log.info("Recebida solicitacao para listar produtos. pagina={}, tamanho={}, ordenacao={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        return produtoService.listar(filtro, pageable);
    }

    @Override
    public ResponseEntity<ProdutoDtoResponse> buscarPorId(Long id) {
        log.info("Recebida solicitacao para buscar produto por id={}", id);
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    @Override
    public ResponseEntity<ProdutoDtoResponse> atualizar(
            Long id,
            @Valid ProdutoUpdateDtoRequest dto
    ) {
        log.info("Recebida solicitacao para atualizar produto id={}", id);
        return ResponseEntity.ok(produtoService.atualizar(id, dto));
    }

    @Override
    public ResponseEntity<Void> desativar(Long id) {
        log.info("Recebida solicitacao para desativar produto id={}", id);
        produtoService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
