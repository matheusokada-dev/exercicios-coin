package br.com.coin.bffcadastroprodutos.mappers;

import br.com.coin.bffcadastroprodutos.dtos.backend.response.ProdutoBackendPageDtoResponse;
import br.com.coin.bffcadastroprodutos.dtos.backend.request.ProdutoBackendDtoRequest;
import br.com.coin.bffcadastroprodutos.dtos.backend.response.ProdutoBackendDtoResponse;
import br.com.coin.bffcadastroprodutos.dtos.backend.request.ProdutoBackendUpdateDtoRequest;
import br.com.coin.bffcadastroprodutos.dtos.frontend.response.ProdutoBffPageDtoResponse;
import br.com.coin.bffcadastroprodutos.dtos.frontend.request.ProdutoBffDtoRequest;
import br.com.coin.bffcadastroprodutos.dtos.frontend.response.ProdutoBffDtoResponse;
import br.com.coin.bffcadastroprodutos.dtos.frontend.request.ProdutoBffUpdateDtoRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProdutoBffMapper {

    public ProdutoBackendDtoRequest toBackendRequest(ProdutoBffDtoRequest dto) {
        return new ProdutoBackendDtoRequest(
                dto.nome(),
                dto.preco()
        );
    }

    public ProdutoBackendUpdateDtoRequest toBackendUpdate(ProdutoBffUpdateDtoRequest dto) {
        return new ProdutoBackendUpdateDtoRequest(
                dto.nome(),
                dto.preco(),
                dto.ativo()
        );
    }

    public ProdutoBffDtoResponse toBffResponse(ProdutoBackendDtoResponse dto) {
        return new ProdutoBffDtoResponse(
                dto.id(),
                dto.nome(),
                dto.preco(),
                dto.ativo()
        );
    }

    public ProdutoBffPageDtoResponse<ProdutoBffDtoResponse> toBffPage(
            ProdutoBackendPageDtoResponse<ProdutoBackendDtoResponse> page
    ) {
        List<ProdutoBffDtoResponse> content = page.content()
                .stream()
                .map(this::toBffResponse)
                .toList();

        return new ProdutoBffPageDtoResponse<>(
                content,
                page.totalElements(),
                page.totalPages(),
                page.size(),
                page.number()
        );
    }
}
