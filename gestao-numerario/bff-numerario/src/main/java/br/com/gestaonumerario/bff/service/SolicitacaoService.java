package br.com.gestaonumerario.bff.service;

import br.com.gestaonumerario.bff.client.ApiNumerarioClient;
import br.com.gestaonumerario.bff.dto.AprovarSolicitacaoRequest;
import br.com.gestaonumerario.bff.dto.AtenderSolicitacaoRequest;
import br.com.gestaonumerario.bff.dto.PaginaResponse;
import br.com.gestaonumerario.bff.dto.RejeitarSolicitacaoRequest;
import br.com.gestaonumerario.bff.dto.SolicitacaoResponse;
import br.com.gestaonumerario.bff.dto.SolicitarAbastecimentoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SolicitacaoService {

    private final ApiNumerarioClient apiNumerarioClient;

    public PaginaResponse<SolicitacaoResponse> listar(
            String authorization,
            Long agenciaId,
            String status,
            LocalDate dataInicio,
            LocalDate dataFim,
            int pagina,
            int tamanho
    ) {
        return apiNumerarioClient.listarSolicitacoes(
                authorization, agenciaId, status, dataInicio, dataFim, pagina, tamanho);
    }

    public SolicitacaoResponse criar(String authorization, SolicitarAbastecimentoRequest request) {
        return apiNumerarioClient.criarSolicitacao(authorization, request);
    }

    public SolicitacaoResponse aprovar(String authorization, Long id, AprovarSolicitacaoRequest request) {
        return apiNumerarioClient.aprovarSolicitacao(authorization, id, request);
    }

    public SolicitacaoResponse rejeitar(String authorization, Long id, RejeitarSolicitacaoRequest request) {
        return apiNumerarioClient.rejeitarSolicitacao(authorization, id, request);
    }

    public SolicitacaoResponse atender(String authorization, Long id, AtenderSolicitacaoRequest request) {
        return apiNumerarioClient.atenderSolicitacao(authorization, id, request);
    }
}
