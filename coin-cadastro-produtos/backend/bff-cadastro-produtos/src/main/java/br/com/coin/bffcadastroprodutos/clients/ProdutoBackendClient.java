package br.com.coin.bffcadastroprodutos.clients;

import br.com.coin.bffcadastroprodutos.dtos.backend.request.ProdutoBackendDtoRequest;
import br.com.coin.bffcadastroprodutos.dtos.backend.request.ProdutoBackendUpdateDtoRequest;
import br.com.coin.bffcadastroprodutos.dtos.backend.response.ProdutoBackendDtoResponse;
import br.com.coin.bffcadastroprodutos.dtos.backend.response.ProdutoBackendErrorDtoResponse;
import br.com.coin.bffcadastroprodutos.dtos.backend.response.ProdutoBackendPageDtoResponse;
import br.com.coin.bffcadastroprodutos.exceptions.BackendIndisponivelException;
import br.com.coin.bffcadastroprodutos.exceptions.BackendResponseException;
import br.com.coin.bffcadastroprodutos.exceptions.BackendTimeoutException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.http.HttpTimeoutException;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProdutoBackendClient {

    private static final String PRODUTOS_PATH = "/produtos";

    private static final String PRODUTO_POR_ID_PATH =
            PRODUTOS_PATH + "/{id}";

    private static final String MENSAGEM_ERRO_BACKEND =
            "Erro ao consultar serviço de produtos.";

    private final RestClient produtoRestClient;
    private final ObjectMapper objectMapper;

    public ProdutoBackendDtoResponse criar(
            ProdutoBackendDtoRequest dto
    ) {
        return executarChamada("criar produto", () -> {
            log.debug("BFF chamando backend para criar produto");

            ProdutoBackendDtoResponse response = produtoRestClient.post()
                    .uri(PRODUTOS_PATH)
                    .body(dto)
                    .retrieve()
                    .body(ProdutoBackendDtoResponse.class);

            response = exigirCorpo(response, "criar produto");

            log.debug(
                    "Backend retornou produto criado id={}",
                    response.id()
            );

            return response;
        });
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
        ProdutoListagemQuery query = new ProdutoListagemQuery(
                page,
                size,
                sort,
                busca,
                status,
                precoMinimo,
                precoMaximo
        );

        return executarChamada("listar produtos", () -> {
            log.debug(
                    "BFF chamando backend para listar produtos: "
                            + "page={}, size={}, status={}, buscaInformada={}",
                    query.page(),
                    query.size(),
                    query.status(),
                    possuiTexto(query.busca())
            );

            ProdutoBackendPageDtoResponse<ProdutoBackendDtoResponse> response =
                    produtoRestClient.get()
                            .uri(uriBuilder ->
                                    montarUriListagem(uriBuilder, query)
                            )
                            .retrieve()
                            .body(
                                    new ParameterizedTypeReference<
                                            ProdutoBackendPageDtoResponse<
                                                    ProdutoBackendDtoResponse
                                                    >
                                            >() {
                                    }
                            );

            response = exigirCorpo(response, "listar produtos");

            log.debug(
                    "Backend retornou {} produtos de um total de {}",
                    contarRegistros(response),
                    response.totalElements()
            );

            return response;
        });
    }

    public ProdutoBackendDtoResponse buscarPorId(Long id) {
        return executarChamada("buscar produto por ID", () -> {
            log.debug(
                    "BFF chamando backend para buscar produto id={}",
                    id
            );

            ProdutoBackendDtoResponse response = produtoRestClient.get()
                    .uri(PRODUTO_POR_ID_PATH, id)
                    .retrieve()
                    .body(ProdutoBackendDtoResponse.class);

            response = exigirCorpo(
                    response,
                    "buscar produto por ID"
            );

            log.debug(
                    "Backend retornou produto id={}",
                    response.id()
            );

            return response;
        });
    }

    public ProdutoBackendDtoResponse atualizar(
            Long id,
            ProdutoBackendUpdateDtoRequest dto
    ) {
        return executarChamada("atualizar produto", () -> {
            log.debug(
                    "BFF chamando backend para atualizar produto id={}",
                    id
            );

            ProdutoBackendDtoResponse response = produtoRestClient.put()
                    .uri(PRODUTO_POR_ID_PATH, id)
                    .body(dto)
                    .retrieve()
                    .body(ProdutoBackendDtoResponse.class);

            response = exigirCorpo(
                    response,
                    "atualizar produto"
            );

            log.debug(
                    "Backend retornou produto atualizado id={}",
                    response.id()
            );

            return response;
        });
    }

    public void desativar(Long id) {
        executarChamada("desativar produto", () -> {
            log.debug(
                    "BFF chamando backend para desativar produto id={}",
                    id
            );

            produtoRestClient.delete()
                    .uri(PRODUTO_POR_ID_PATH, id)
                    .retrieve()
                    .toBodilessEntity();

            log.debug(
                    "Backend confirmou desativação do produto id={}",
                    id
            );

            return null;
        });
    }

    private <T> T executarChamada(
            String operacao,
            Supplier<T> chamada
    ) {
        try {
            return chamada.get();
        } catch (RestClientResponseException ex) {
            throw tratarErroResposta(operacao, ex);
        } catch (ResourceAccessException ex) {
            throw tratarErroAcesso(operacao, ex);
        }
    }

    private RuntimeException tratarErroResposta(
            String operacao,
            RestClientResponseException ex
    ) {
        int codigoStatus = ex.getStatusCode().value();

        log.warn(
                "Backend respondeu com erro na operação '{}': status={}",
                operacao,
                codigoStatus
        );

        if (ex.getStatusCode().is5xxServerError()) {
            log.error(
                    "Backend de produtos apresentou erro interno "
                            + "na operação '{}'",
                    operacao,
                    ex
            );

            return new BackendIndisponivelException();
        }

        HttpStatus status = HttpStatus.resolve(codigoStatus);

        if (status == null) {
            log.error(
                    "Backend respondeu com status HTTP não reconhecido: {}",
                    codigoStatus
            );

            return new BackendIndisponivelException();
        }

        ProdutoBackendErrorDtoResponse backendError =
                extrairErro(ex);

        return new BackendResponseException(
                status,
                backendError.codError(),
                backendError.msgError()
        );
    }

    private RuntimeException tratarErroAcesso(
            String operacao,
            ResourceAccessException ex
    ) {
        if (ehTimeout(ex)) {
            log.error(
                    "Timeout ao chamar backend na operação '{}'",
                    operacao,
                    ex
            );

            return new BackendTimeoutException();
        }

        log.error(
                "Falha de conexão com o backend na operação '{}'",
                operacao,
                ex
        );

        return new BackendIndisponivelException();
    }

    private ProdutoBackendErrorDtoResponse extrairErro(
            RestClientResponseException ex
    ) {
        String responseBody = ex.getResponseBodyAsString();
        int codigoStatus = ex.getStatusCode().value();

        if (!possuiTexto(responseBody)) {
            return criarErroPadrao(codigoStatus);
        }

        try {
            ProdutoBackendErrorDtoResponse backendError =
                    objectMapper.readValue(
                            responseBody,
                            ProdutoBackendErrorDtoResponse.class
                    );

            Integer codigoErro = backendError.codError() != null
                    ? backendError.codError()
                    : codigoStatus;

            String mensagemErro =
                    possuiTexto(backendError.msgError())
                            ? backendError.msgError()
                            : MENSAGEM_ERRO_BACKEND;

            return new ProdutoBackendErrorDtoResponse(
                    codigoErro,
                    mensagemErro
            );
        } catch (JsonProcessingException exParse) {
            log.warn(
                    "Não foi possível interpretar o corpo de erro "
                            + "retornado pelo backend. status={}",
                    codigoStatus
            );

            return criarErroPadrao(codigoStatus);
        }
    }

    private ProdutoBackendErrorDtoResponse criarErroPadrao(
            int codigoStatus
    ) {
        return new ProdutoBackendErrorDtoResponse(
                codigoStatus,
                MENSAGEM_ERRO_BACKEND
        );
    }

    private <T> T exigirCorpo(
            T response,
            String operacao
    ) {
        if (response == null) {
            log.error(
                    "Backend retornou resposta sem corpo "
                            + "na operação '{}'",
                    operacao
            );

            throw new BackendIndisponivelException();
        }

        return response;
    }

    private URI montarUriListagem(
            UriBuilder uriBuilder,
            ProdutoListagemQuery query
    ) {
        UriBuilder builder = uriBuilder.path(PRODUTOS_PATH);

        if (query.page() != null) {
            builder.queryParam("page", query.page());
        }

        if (query.size() != null) {
            builder.queryParam("size", query.size());
        }

        if (possuiTexto(query.sort())) {
            builder.queryParam("sort", query.sort());
        }

        if (possuiTexto(query.busca())) {
            builder.queryParam("busca", query.busca());
        }

        if (possuiTexto(query.status())) {
            builder.queryParam("status", query.status());
        }

        if (query.precoMinimo() != null) {
            builder.queryParam(
                    "precoMinimo",
                    query.precoMinimo()
            );
        }

        if (query.precoMaximo() != null) {
            builder.queryParam(
                    "precoMaximo",
                    query.precoMaximo()
            );
        }

        return builder.build();
    }

    private int contarRegistros(
            ProdutoBackendPageDtoResponse<ProdutoBackendDtoResponse> response
    ) {
        if (response.content() == null) {
            return 0;
        }

        return response.content().size();
    }

    private boolean possuiTexto(String valor) {
        return valor != null && !valor.isBlank();
    }

    private boolean ehTimeout(Throwable throwable) {
        return possuiCausa(
                throwable,
                SocketTimeoutException.class
        ) || possuiCausa(
                throwable,
                HttpTimeoutException.class
        );
    }

    private boolean possuiCausa(
            Throwable throwable,
            Class<? extends Throwable> tipo
    ) {
        Throwable causaAtual = throwable;

        while (causaAtual != null) {
            if (tipo.isInstance(causaAtual)) {
                return true;
            }

            if (causaAtual == causaAtual.getCause()) {
                break;
            }

            causaAtual = causaAtual.getCause();
        }

        return false;
    }

    private record ProdutoListagemQuery(
            Integer page,
            Integer size,
            String sort,
            String busca,
            String status,
            BigDecimal precoMinimo,
            BigDecimal precoMaximo
    ) {
    }
}