package br.com.coin.bffcadastroprodutos.services;

import br.com.coin.bffcadastroprodutos.clients.ProdutoBackendClient;
import br.com.coin.bffcadastroprodutos.dtos.backend.ProdutoBackendPageResponseDTO;
import br.com.coin.bffcadastroprodutos.dtos.backend.ProdutoBackendResponseDTO;
import br.com.coin.bffcadastroprodutos.dtos.bff.ProdutoBffFiltroDTO;
import br.com.coin.bffcadastroprodutos.dtos.bff.ProdutoBffPageResponseDTO;
import br.com.coin.bffcadastroprodutos.dtos.bff.ProdutoBffRequestDTO;
import br.com.coin.bffcadastroprodutos.dtos.bff.ProdutoBffResponseDTO;
import br.com.coin.bffcadastroprodutos.dtos.bff.ProdutoBffUpdateDTO;
import br.com.coin.bffcadastroprodutos.exceptions.ProdutoBffValidationException;
import br.com.coin.bffcadastroprodutos.mappers.ProdutoBffMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProdutoBffService {

    private static final Set<Integer> TAMANHOS_PERMITIDOS = Set.of(5, 10, 20, 50);

    private final ProdutoBackendClient produtoBackendClient;
    private final ProdutoBffMapper produtoBffMapper;

    public ProdutoBffResponseDTO criar(ProdutoBffRequestDTO dto) {
        var backendRequest = produtoBffMapper.toBackendRequest(dto);
        var backendResponse = produtoBackendClient.criar(backendRequest);

        return produtoBffMapper.toBffResponse(backendResponse);
    }

    public ProdutoBffPageResponseDTO<ProdutoBffResponseDTO> listar(ProdutoBffFiltroDTO filtro) {
        validarFiltro(filtro);

        Integer page = filtro.page() == null ? 0 : filtro.page();
        Integer size = filtro.size() == null ? 5 : filtro.size();
        String sort = filtro.sort() == null || filtro.sort().isBlank() ? "id,asc" : filtro.sort();
        String status = filtro.status() == null || filtro.status().isBlank() ? "todos" : filtro.status();

        ProdutoBackendPageResponseDTO<ProdutoBackendResponseDTO> backendPage =
                produtoBackendClient.listar(
                        page,
                        size,
                        sort,
                        filtro.busca(),
                        status,
                        filtro.precoMinimo(),
                        filtro.precoMaximo()
                );

        return produtoBffMapper.toBffPage(backendPage);
    }

    public ProdutoBffResponseDTO buscarPorId(Long id) {
        var backendResponse = produtoBackendClient.buscarPorId(id);
        return produtoBffMapper.toBffResponse(backendResponse);
    }

    public ProdutoBffResponseDTO atualizar(Long id, ProdutoBffUpdateDTO dto) {
        var backendRequest = produtoBffMapper.toBackendUpdate(dto);
        var backendResponse = produtoBackendClient.atualizar(id, backendRequest);

        return produtoBffMapper.toBffResponse(backendResponse);
    }

    public void desativar(Long id) {
        produtoBackendClient.desativar(id);
    }

    private void validarFiltro(ProdutoBffFiltroDTO filtro) {
        if (filtro.page() != null && filtro.page() < 0) {
            throw new ProdutoBffValidationException("Página não pode ser negativa.");
        }

        if (filtro.size() != null && !TAMANHOS_PERMITIDOS.contains(filtro.size())) {
            throw new ProdutoBffValidationException("Tamanho de página inválido.");
        }

        if (filtro.precoMinimo() != null
                && filtro.precoMaximo() != null
                && filtro.precoMinimo().compareTo(filtro.precoMaximo()) > 0) {
            throw new ProdutoBffValidationException("Preço mínimo não pode ser maior que preço máximo.");
        }
    }
}