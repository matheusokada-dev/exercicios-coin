package br.com.coin.bffcadastroprodutos.services;

import br.com.coin.bffcadastroprodutos.clients.ProdutoBackendClient;
import br.com.coin.bffcadastroprodutos.dtos.backend.response.ProdutoBackendPageDtoResponse;
import br.com.coin.bffcadastroprodutos.dtos.backend.response.ProdutoBackendDtoResponse;
import br.com.coin.bffcadastroprodutos.dtos.frontend.request.ProdutoBffFiltroDtoRequest;
import br.com.coin.bffcadastroprodutos.dtos.frontend.response.ProdutoBffPageDtoResponse;
import br.com.coin.bffcadastroprodutos.dtos.frontend.request.ProdutoBffDtoRequest;
import br.com.coin.bffcadastroprodutos.dtos.frontend.response.ProdutoBffDtoResponse;
import br.com.coin.bffcadastroprodutos.dtos.frontend.request.ProdutoBffUpdateDtoRequest;
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

    public ProdutoBffDtoResponse criar(ProdutoBffDtoRequest dto) {
        var backendRequest = produtoBffMapper.toBackendRequest(dto);
        var backendResponse = produtoBackendClient.criar(backendRequest);

        return produtoBffMapper.toBffResponse(backendResponse);
    }

    public ProdutoBffPageDtoResponse<ProdutoBffDtoResponse> listar(ProdutoBffFiltroDtoRequest filtro) {
        validarFiltro(filtro);

        Integer page = filtro.page() == null ? 0 : filtro.page();
        Integer size = filtro.size() == null ? 5 : filtro.size();
        String sort = filtro.sort() == null || filtro.sort().isBlank() ? "id,asc" : filtro.sort();
        String status = filtro.status() == null || filtro.status().isBlank() ? "todos" : filtro.status();

        ProdutoBackendPageDtoResponse<ProdutoBackendDtoResponse> backendPage =
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

    public ProdutoBffDtoResponse buscarPorId(Long id) {
        var backendResponse = produtoBackendClient.buscarPorId(id);
        return produtoBffMapper.toBffResponse(backendResponse);
    }

    public ProdutoBffDtoResponse atualizar(Long id, ProdutoBffUpdateDtoRequest dto) {
        var backendRequest = produtoBffMapper.toBackendUpdate(dto);
        var backendResponse = produtoBackendClient.atualizar(id, backendRequest);

        return produtoBffMapper.toBffResponse(backendResponse);
    }

    public void desativar(Long id) {
        produtoBackendClient.desativar(id);
    }

    private void validarFiltro(ProdutoBffFiltroDtoRequest filtro) {
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