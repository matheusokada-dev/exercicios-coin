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
import br.com.gestaonumerario.bff.dto.SolicitacaoNumerarioResponse;
import br.com.gestaonumerario.bff.dto.DetalheSolicitacaoNumerarioResponse;
import br.com.gestaonumerario.bff.dto.OperacaoNumerarioResponse;
import br.com.gestaonumerario.bff.dto.UnidadeOperacionalResponse;
import br.com.gestaonumerario.bff.dto.HistoricoSolicitacaoResponse;
import br.com.gestaonumerario.bff.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ApiNumerarioClient {

    private static final ParameterizedTypeReference<PaginaResponse<AgenciaResponse>> PAGINA_AGENCIAS =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<PaginaResponse<SolicitacaoResponse>> PAGINA_SOLICITACOES =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<PaginaResponse<MovimentacaoResponse>> PAGINA_MOVIMENTACOES =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<PaginaResponse<SolicitacaoNumerarioResponse>> PAGINA_SOLICITACOES_NUMERARIO =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<PaginaResponse<OperacaoNumerarioResponse>> PAGINA_OPERACOES_NUMERARIO =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<List<UnidadeOperacionalResponse>> UNIDADES_OPERACIONAIS =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<List<HistoricoSolicitacaoResponse>> HISTORICO_SOLICITACAO =
            new ParameterizedTypeReference<>() { };

    private final RestClient apiNumerarioRestClient;

    public LoginResponse autenticar(LoginRequest request) {
        return apiNumerarioRestClient.post()
                .uri("/api/v1/auth/login")
                .body(request)
                .retrieve()
                .body(LoginResponse.class);
    }

    public LoginResponse renovar(String refreshToken) {
        return apiNumerarioRestClient.post()
                .uri("/api/v1/auth/refresh")
                .body(new RefreshRequest(refreshToken))
                .retrieve()
                .body(LoginResponse.class);
    }

    public void encerrar(String refreshToken) {
        apiNumerarioRestClient.post()
                .uri("/api/v1/auth/logout")
                .body(new RefreshRequest(refreshToken))
                .retrieve()
                .toBodilessEntity();
    }

    public SessaoResponse consultarSessao(String authorization) {
        return apiNumerarioRestClient.get()
                .uri("/api/v1/auth/me")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .retrieve()
                .body(SessaoResponse.class);
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

    public PaginaResponse<SolicitacaoNumerarioResponse> listarSolicitacoesNumerario(
            String authorization,Long agenciaId,String tipo,String status,Long origemId,
            Long destinoId,LocalDate dataInicio,LocalDate dataFim,int pagina,int tamanho) {
        return apiNumerarioRestClient.get()
                .uri(builder -> builder.path("/api/v1/solicitacoes-numerario")
                        .queryParamIfPresent("agenciaId",Optional.ofNullable(agenciaId))
                        .queryParamIfPresent("tipo",Optional.ofNullable(tipo))
                        .queryParamIfPresent("status",Optional.ofNullable(status))
                        .queryParamIfPresent("origemId",Optional.ofNullable(origemId))
                        .queryParamIfPresent("destinoId",Optional.ofNullable(destinoId))
                        .queryParamIfPresent("dataInicio",Optional.ofNullable(dataInicio))
                        .queryParamIfPresent("dataFim",Optional.ofNullable(dataFim))
                        .queryParam("pagina",pagina).queryParam("tamanho",tamanho).build())
                .header(HttpHeaders.AUTHORIZATION,authorization).retrieve().body(PAGINA_SOLICITACOES_NUMERARIO);
    }

    public DetalheSolicitacaoNumerarioResponse detalharSolicitacaoNumerario(
            String authorization,Long id) {
        return apiNumerarioRestClient.get().uri("/api/v1/solicitacoes-numerario/{id}",id)
                .header(HttpHeaders.AUTHORIZATION,authorization).retrieve()
                .body(DetalheSolicitacaoNumerarioResponse.class);
    }

    public List<HistoricoSolicitacaoResponse> consultarHistoricoSolicitacao(
            String authorization,Long id) {
        return apiNumerarioRestClient.get()
                .uri("/api/v1/solicitacoes-numerario/{id}/historico",id)
                .header(HttpHeaders.AUTHORIZATION,authorization).retrieve().body(HISTORICO_SOLICITACAO);
    }

    public List<UnidadeOperacionalResponse> listarUnidadesOperacionais(
            String authorization,String tipo) {
        return apiNumerarioRestClient.get()
                .uri(builder -> builder.path("/api/v1/unidades-operacionais")
                        .queryParamIfPresent("tipo",Optional.ofNullable(tipo)).build())
                .header(HttpHeaders.AUTHORIZATION,authorization).retrieve().body(UNIDADES_OPERACIONAIS);
    }

    public PaginaResponse<OperacaoNumerarioResponse> listarOperacoesNumerario(
            String authorization,String status,Long origemId,Long destinoId,
            LocalDate dataInicio,LocalDate dataFim,int pagina,int tamanho) {
        return apiNumerarioRestClient.get()
                .uri(builder -> builder.path("/api/v1/operacoes-numerario")
                        .queryParamIfPresent("status",Optional.ofNullable(status))
                        .queryParamIfPresent("origemId",Optional.ofNullable(origemId))
                        .queryParamIfPresent("destinoId",Optional.ofNullable(destinoId))
                        .queryParamIfPresent("dataInicio",Optional.ofNullable(dataInicio))
                        .queryParamIfPresent("dataFim",Optional.ofNullable(dataFim))
                        .queryParam("pagina",pagina).queryParam("tamanho",tamanho).build())
                .header(HttpHeaders.AUTHORIZATION,authorization).retrieve().body(PAGINA_OPERACOES_NUMERARIO);
    }

    public SolicitacaoNumerarioResponse criarSolicitacaoNumerario(String auth,
            CriarSolicitacaoNumerarioRequest request) {
        return executarPost("/api/v1/solicitacoes-numerario",auth,null,request,
                SolicitacaoNumerarioResponse.class);
    }
    public SolicitacaoNumerarioResponse decidirSolicitacaoNumerario(String auth,Long id,
            String acao,DecidirSolicitacaoNumerarioRequest request) {
        return executarPut("/api/v1/solicitacoes-numerario/"+id+"/"+acao,auth,null,request,
                SolicitacaoNumerarioResponse.class);
    }
    public OperacaoNumerarioResponse programarOperacao(String auth,Long id,String key,
            ProgramarOperacaoNumerarioRequest request) {
        return executarPut("/api/v1/solicitacoes-numerario/"+id+"/programar",auth,key,request,
                OperacaoNumerarioResponse.class);
    }
    public OperacaoNumerarioResponse iniciarSeparacao(String auth,Long id,
            VersaoOperacaoNumerarioRequest request) {
        return executarPut("/api/v1/solicitacoes-numerario/"+id+"/iniciar-separacao",auth,null,
                request,OperacaoNumerarioResponse.class);
    }
    public OperacaoNumerarioResponse expedirOperacao(String auth,Long id,String key,
            ExecutarOperacaoNumerarioRequest request) {
        return executarPut("/api/v1/solicitacoes-numerario/"+id+"/expedir",auth,key,request,
                OperacaoNumerarioResponse.class);
    }
    public OperacaoNumerarioResponse registrarOcorrencia(String auth,Long id,
            OcorrenciaOperacaoNumerarioRequest request) {
        return executarPut("/api/v1/solicitacoes-numerario/"+id+"/registrar-ocorrencia",
                auth,null,request,OperacaoNumerarioResponse.class);
    }
    public OperacaoNumerarioResponse receberOperacao(String auth,Long id,String key,
            ReceberOperacaoNumerarioRequest request) {
        return executarPut("/api/v1/solicitacoes-numerario/"+id+"/receber",auth,key,request,
                OperacaoNumerarioResponse.class);
    }
    public OperacaoNumerarioResponse conciliarOperacao(String auth,Long id,String key,
            ConciliarOperacaoNumerarioRequest request) {
        return executarPut("/api/v1/solicitacoes-numerario/"+id+"/conciliar",auth,key,request,
                OperacaoNumerarioResponse.class);
    }
    public UnidadeOperacionalResponse realizarCargaInicial(String auth,String key,
            CargaInicialTesourariaRequest request) {
        return executarPost("/api/v1/tesouraria/carga-inicial",auth,key,request,
                UnidadeOperacionalResponse.class);
    }
    public UnidadeOperacionalResponse ajustarDivergencia(String auth,Long id,String key,
            AjustarDivergenciaRequest request) {
        return executarPost("/api/v1/solicitacoes-numerario/"+id+"/ajustes-divergencia",
                auth,key,request,UnidadeOperacionalResponse.class);
    }
    public AgenciaResponse criarAgenciaOperacional(String auth,CriarAgenciaRequest request) {
        return executarPost("/api/v1/agencias",auth,null,request,AgenciaResponse.class);
    }
    public MovimentacaoResponse registrarMovimentacaoOperacional(String auth,RegistrarMovimentacaoRequest request) {
        return executarPost("/api/v1/movimentacoes",auth,null,request,MovimentacaoResponse.class);
    }

    private <T> T executarPut(String uri,String auth,String key,Object body,Class<T> response) {
        var request=apiNumerarioRestClient.put().uri(uri)
                .header(HttpHeaders.AUTHORIZATION,auth);
        if(key!=null) request=request.header("Idempotency-Key",key);
        return request.body(body).retrieve().body(response);
    }
    private <T> T executarPost(String uri,String auth,String key,Object body,Class<T> response) {
        var request=apiNumerarioRestClient.post().uri(uri)
                .header(HttpHeaders.AUTHORIZATION,auth);
        if(key!=null) request=request.header("Idempotency-Key",key);
        return request.body(body).retrieve().body(response);
    }
}
