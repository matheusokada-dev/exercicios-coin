package br.com.coin.bffcadastroprodutos.mappers;

import br.com.coin.bffcadastroprodutos.dtos.backend.ProdutoBackendPageDtoResponse;
import br.com.coin.bffcadastroprodutos.dtos.backend.ProdutoBackendDtoRequest;
import br.com.coin.bffcadastroprodutos.dtos.backend.ProdutoBackendDtoResponse;
import br.com.coin.bffcadastroprodutos.dtos.backend.ProdutoBackendUpdateDtoRequest;
import br.com.coin.bffcadastroprodutos.dtos.frontend.ProdutoBffPageDtoResponse;
import br.com.coin.bffcadastroprodutos.dtos.frontend.ProdutoBffDtoRequest;
import br.com.coin.bffcadastroprodutos.dtos.frontend.ProdutoBffDtoResponse;
import br.com.coin.bffcadastroprodutos.dtos.frontend.ProdutoBffUpdateDtoRequest;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProdutoBffMapper {

    ProdutoBackendDtoRequest toBackendRequest(ProdutoBffDtoRequest dto);

    ProdutoBackendUpdateDtoRequest toBackendUpdate(ProdutoBffUpdateDtoRequest dto);

    ProdutoBffDtoResponse toBffResponse(ProdutoBackendDtoResponse dto);

    default ProdutoBffPageDtoResponse<ProdutoBffDtoResponse> toBffPage(
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
