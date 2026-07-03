package br.com.coin.cadastroprodutos.controllers;

import br.com.coin.cadastroprodutos.dtos.FiltroProdutoDTO;
import br.com.coin.cadastroprodutos.dtos.ProdutoRequestDTO;
import br.com.coin.cadastroprodutos.dtos.ProdutoResponseDTO;
import br.com.coin.cadastroprodutos.dtos.ProdutoUpdateDTO;
import br.com.coin.cadastroprodutos.services.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/produtos")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class ProdutoController {

    private final ProdutoService produtoService;

    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> criar(@Valid @RequestBody ProdutoRequestDTO dto) {
        log.info("Recebida solicitacao para criar produto");
        ProdutoResponseDTO produtoCriado = produtoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoCriado);
    }

    @GetMapping
    public Page<ProdutoResponseDTO> listar(
            @Valid FiltroProdutoDTO filtro,
            @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        log.info("Recebida solicitacao para listar produtos. pagina={}, tamanho={}, ordenacao={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        return produtoService.listar(filtro, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> buscarPorId(@PathVariable Long id) {
        log.info("Recebida solicitacao para buscar produto por id={}", id);
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProdutoUpdateDTO dto
    ) {
        log.info("Recebida solicitacao para atualizar produto id={}", id);
        return ResponseEntity.ok(produtoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        log.info("Recebida solicitacao para desativar produto id={}", id);
        produtoService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}