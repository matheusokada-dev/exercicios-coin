package br.com.coin.bffcadastroprodutos.mappers;

import br.com.coin.bffcadastroprodutos.dtos.backend.ProdutoBackendPageResponseDTO;
import br.com.coin.bffcadastroprodutos.dtos.backend.ProdutoBackendRequestDTO;
import br.com.coin.bffcadastroprodutos.dtos.backend.ProdutoBackendResponseDTO;
import br.com.coin.bffcadastroprodutos.dtos.backend.ProdutoBackendUpdateDTO;
import br.com.coin.bffcadastroprodutos.dtos.bff.ProdutoBffPageResponseDTO;
import br.com.coin.bffcadastroprodutos.dtos.bff.ProdutoBffRequestDTO;
import br.com.coin.bffcadastroprodutos.dtos.bff.ProdutoBffResponseDTO;
import br.com.coin.bffcadastroprodutos.dtos.bff.ProdutoBffUpdateDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProdutoBffMapper {

    ProdutoBackendRequestDTO toBackendRequest(ProdutoBffRequestDTO dto);

    ProdutoBackendUpdateDTO toBackendUpdate(ProdutoBffUpdateDTO dto);

    ProdutoBffResponseDTO toBffResponse(ProdutoBackendResponseDTO dto);

    default ProdutoBffPageResponseDTO<ProdutoBffResponseDTO> toBffPage(
            ProdutoBackendPageResponseDTO<ProdutoBackendResponseDTO> page
    ) {
        List<ProdutoBffResponseDTO> content = page.content()
                .stream()
                .map(this::toBffResponse)
                .toList();

        return new ProdutoBffPageResponseDTO<>(
                content,
                page.totalElements(),
                page.totalPages(),
                page.size(),
                page.number()
        );
    }
}
