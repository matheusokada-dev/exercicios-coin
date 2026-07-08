package br.com.coin.bffcadastroprodutos.clients;

import br.com.coin.bffcadastroprodutos.dtos.backend.*;
import br.com.coin.bffcadastroprodutos.exceptions.BackendIndisponivelException;
import br.com.coin.bffcadastroprodutos.exceptions.BackendResponseException;
import br.com.coin.bffcadastroprodutos.exceptions.BackendTimeoutException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;

@Component
@RequiredArgsConstructor
public class ProdutoBackendClient {

    private final RestClient produtoRestClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProdutoBackendDtoResponse criar(ProdutoBackendDtoRequest dto) {
        try {
            return produtoRestClient.post()
                    .uri("/produtos")
                    .body(dto)
                    .retrieve()
                    .body(ProdutoBackendDtoResponse.class);
        } catch (Exception ex) {
            throw tratarErro(ex);
        }
    }

    public ProdutoBackendPageDtoResponse<ProdutoBackendDtoResponse> listar(
            Integer page,
            Integer size,
            String sort,
            String busca,
            String status,
            BigDecimal precoMinimo,
            BigDecimal precoMaximo
    ) {
        try {
            return produtoRestClient.get()
                    .uri(uriBuilder -> montarUriListagem(
                            uriBuilder,
                            page,
                            size,
                            sort,
                            busca,
                            status,
                            precoMinimo,
                            precoMaximo
                    ))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception ex) {
            throw tratarErro(ex);
        }
    }

    public ProdutoBackendDtoResponse buscarPorId(Long id) {
        try {
            return produtoRestClient.get()
                    .uri("/produtos/{id}", id)
                    .retrieve()
                    .body(ProdutoBackendDtoResponse.class);
        } catch (Exception ex) {
            throw tratarErro(ex);
        }
    }

    public ProdutoBackendDtoResponse atualizar(Long id, ProdutoBackendUpdateDtoRequest dto) {
        try {
            return produtoRestClient.put()
                    .uri("/produtos/{id}", id)
                    .body(dto)
                    .retrieve()
                    .body(ProdutoBackendDtoResponse.class);
        } catch (Exception ex) {
            throw tratarErro(ex);
        }
    }

    public void desativar(Long id) {
        try {
            produtoRestClient.delete()
                    .uri("/produtos/{id}", id)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            throw tratarErro(ex);
        }
    }

    private java.net.URI montarUriListagem(
            UriBuilder uriBuilder,
            Integer page,
            Integer size,
            String sort,
            String busca,
            String status,
            BigDecimal precoMinimo,
            BigDecimal precoMaximo
    ) {
        UriBuilder builder = uriBuilder
                .path("/produtos")
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sort", sort);

        if (busca != null && !busca.isBlank()) {
            builder.queryParam("busca", busca);
        }

        if (status != null && !status.isBlank()) {
            builder.queryParam("status", status);
        }

        if (precoMinimo != null) {
            builder.queryParam("precoMinimo", precoMinimo);
        }

        if (precoMaximo != null) {
            builder.queryParam("precoMaximo", precoMaximo);
        }

        return builder.build();
    }

    private RuntimeException tratarErro(Exception ex) {
        if (ex instanceof RestClientResponseException responseException) {
            HttpStatus status = HttpStatus.valueOf(responseException.getStatusCode().value());

            if (status.is5xxServerError()) {
                return new BackendIndisponivelException();
            }

            ProdutoBackendErrorDtoResponse backendError = extrairErro(responseException);

            return new BackendResponseException(
                    status,
                    backendError.codError(),
                    backendError.msgError()
            );
        }

        if (ex instanceof ResourceAccessException) {
            if (ex.getCause() instanceof SocketTimeoutException) {
                return new BackendTimeoutException();
            }

            return new BackendIndisponivelException();
        }

        return new BackendIndisponivelException();
    }

    private ProdutoBackendErrorDtoResponse extrairErro(RestClientResponseException ex) {
        String body = ex.getResponseBodyAsString();

        if (body != null && !body.isBlank()) {
            try {
                ProdutoBackendErrorDtoResponse backendError = objectMapper.readValue(body, ProdutoBackendErrorDtoResponse.class);

                return new ProdutoBackendErrorDtoResponse(
                        backendError.codError() != null ? backendError.codError() : ex.getStatusCode().value(),
                        backendError.msgError() != null ? backendError.msgError() : "Erro ao consultar servi\u00e7o de produtos."
                );
            } catch (JsonProcessingException ignored) {
                return new ProdutoBackendErrorDtoResponse(ex.getStatusCode().value(), "Erro ao consultar servi\u00e7o de produtos.");
            }
        }

        return new ProdutoBackendErrorDtoResponse(ex.getStatusCode().value(), "Erro ao consultar servi\u00e7o de produtos.");
    }
}
