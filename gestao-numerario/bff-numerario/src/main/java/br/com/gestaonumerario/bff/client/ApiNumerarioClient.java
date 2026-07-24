package br.com.gestaonumerario.bff.client;

import br.com.gestaonumerario.bff.dto.AgenciaResponse;
import br.com.gestaonumerario.bff.dto.AprovarSolicitacaoRequest;
import br.com.gestaonumerario.bff.dto.AtenderSolicitacaoRequest;
import br.com.gestaonumerario.bff.dto.AtualizarAgenciaRequest;
import br.com.gestaonumerario.bff.dto.CriarAgenciaRequest;
import br.com.gestaonumerario.bff.dto.DashboardResponse;
import br.com.gestaonumerario.bff.dto.DetalheAgenciaResponse;
import br.com.gestaonumerario.bff.dto.LoginRequest;
import br.com.gestaonumerario.bff.dto.LoginResponse;
import br.com.gestaonumerario.bff.dto.MovimentacaoResponse;
import br.com.gestaonumerario.bff.dto.PaginaResponse;
import br.com.gestaonumerario.bff.dto.RegistrarMovimentacaoRequest;
import br.com.gestaonumerario.bff.dto.RejeitarSolicitacaoRequest;
import br.com.gestaonumerario.bff.dto.SolicitacaoResponse;
import br.com.gestaonumerario.bff.dto.SolicitarAbastecimentoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApiNumerarioClient {

    private static final ParameterizedTypeReference<PaginaResponse<AgenciaResponse>> PAGINA_AGENCIAS =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<PaginaResponse<SolicitacaoResponse>> PAGINA_SOLICITACOES =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<PaginaResponse<MovimentacaoResponse>> PAGINA_MOVIMENTACOES =
            new ParameterizedTypeReference<>() { };

    private final RestClient apiNumerarioRestClient;

    public LoginResponse autenticar(LoginRequest request) {
        return apiNumerarioRestClient.post()
                .uri("/api/v1/auth/login")
                .body(request)
                .retrieve()
                .body(LoginResponse.class);
    }

    public DashboardResponse consultarDashboard(String authorization) {
        return apiNumerarioRestClient.get()
                .uri("/api/v1/dashboard")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(DashboardResponse.class);
    }

    public PaginaResponse<AgenciaResponse> listarAgencias(
            String authorization,
            String busca,
            Boolean ativo,
            Boolean alerta,
            String ordenarPor,
            String direcao,
            int pagina,
            int tamanho
    ) {
        return apiNumerarioRestClient.get()
                .uri(builder -> builder.path("/api/v1/agencias")
                        .queryParamIfPresent("busca", Optional.ofNullable(busca))
                        .queryParamIfPresent("ativo", Optional.ofNullable(ativo))
                        .queryParamIfPresent("alerta", Optional.ofNullable(alerta))
                        .queryParamIfPresent("ordenarPor", Optional.ofNullable(ordenarPor))
                        .queryParamIfPresent("direcao", Optional.ofNullable(direcao))
                        .queryParam("pagina", pagina)
                        .queryParam("tamanho", tamanho)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(PAGINA_AGENCIAS);
    }

    public DetalheAgenciaResponse detalharAgencia(String authorization, Long id) {
        return apiNumerarioRestClient.get()
                .uri("/api/v1/agencias/{id}/detalhe", id)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(DetalheAgenciaResponse.class);
    }

    public AgenciaResponse criarAgencia(String authorization, CriarAgenciaRequest request) {
        return apiNumerarioRestClient.post()
                .uri("/api/v1/agencias")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .body(request)
                .retrieve()
                .body(AgenciaResponse.class);
    }

    public AgenciaResponse atualizarAgencia(
            String authorization,
            Long id,
            AtualizarAgenciaRequest request
    ) {
        return apiNumerarioRestClient.put()
                .uri("/api/v1/agencias/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .body(request)
                .retrieve()
                .body(AgenciaResponse.class);
    }

    public void desativarAgencia(String authorization, Long id) {
        apiNumerarioRestClient.delete()
                .uri("/api/v1/agencias/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .toBodilessEntity();
    }

    public PaginaResponse<SolicitacaoResponse> listarSolicitacoes(
            String authorization,
            Long agenciaId,
            String status,
            LocalDate dataInicio,
            LocalDate dataFim,
            int pagina,
            int tamanho
    ) {
        return apiNumerarioRestClient.get()
                .uri(builder -> builder.path("/api/v1/solicitacoes")
                        .queryParamIfPresent("agenciaId", Optional.ofNullable(agenciaId))
                        .queryParamIfPresent("status", Optional.ofNullable(status))
                        .queryParamIfPresent("dataInicio", Optional.ofNullable(dataInicio))
                        .queryParamIfPresent("dataFim", Optional.ofNullable(dataFim))
                        .queryParam("pagina", pagina)
                        .queryParam("tamanho", tamanho)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(PAGINA_SOLICITACOES);
    }

    public SolicitacaoResponse criarSolicitacao(
            String authorization,
            SolicitarAbastecimentoRequest request
    ) {
        return apiNumerarioRestClient.post()
                .uri("/api/v1/solicitacoes")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .body(request)
                .retrieve()
                .body(SolicitacaoResponse.class);
    }

    public SolicitacaoResponse aprovarSolicitacao(
            String authorization,
            Long id,
            AprovarSolicitacaoRequest request
    ) {
        return apiNumerarioRestClient.put()
                .uri("/api/v1/solicitacoes/{id}/aprovar", id)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .body(request)
                .retrieve()
                .body(SolicitacaoResponse.class);
    }

    public SolicitacaoResponse rejeitarSolicitacao(
            String authorization,
            Long id,
            RejeitarSolicitacaoRequest request
    ) {
        return apiNumerarioRestClient.put()
                .uri("/api/v1/solicitacoes/{id}/rejeitar", id)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .body(request)
                .retrieve()
                .body(SolicitacaoResponse.class);
    }

    public SolicitacaoResponse atenderSolicitacao(
            String authorization,
            Long id,
            AtenderSolicitacaoRequest request
    ) {
        return apiNumerarioRestClient.put()
                .uri("/api/v1/solicitacoes/{id}/atender", id)
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .body(request)
                .retrieve()
                .body(SolicitacaoResponse.class);
    }

    public PaginaResponse<MovimentacaoResponse> listarMovimentacoes(
            String authorization,
            Long agenciaId,
            String tipo,
            LocalDate dataInicio,
            LocalDate dataFim,
            int pagina,
            int tamanho
    ) {
        return apiNumerarioRestClient.get()
                .uri(builder -> builder.path("/api/v1/movimentacoes")
                        .queryParamIfPresent("agenciaId", Optional.ofNullable(agenciaId))
                        .queryParamIfPresent("tipo", Optional.ofNullable(tipo))
                        .queryParamIfPresent("dataInicio", Optional.ofNullable(dataInicio))
                        .queryParamIfPresent("dataFim", Optional.ofNullable(dataFim))
                        .queryParam("pagina", pagina)
                        .queryParam("tamanho", tamanho)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(PAGINA_MOVIMENTACOES);
    }

    public MovimentacaoResponse criarMovimentacao(
            String authorization,
            RegistrarMovimentacaoRequest request
    ) {
        return apiNumerarioRestClient.post()
                .uri("/api/v1/movimentacoes")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .body(request)
                .retrieve()
                .body(MovimentacaoResponse.class);
    }
}
