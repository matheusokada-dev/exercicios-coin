package br.com.gestaonumerario.bff.service;

import br.com.gestaonumerario.bff.client.ApiNumerarioClient;
import br.com.gestaonumerario.bff.dto.MovimentacaoResponse;
import br.com.gestaonumerario.bff.dto.PaginaResponse;
import br.com.gestaonumerario.bff.dto.RegistrarMovimentacaoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MovimentacaoService {

    private final ApiNumerarioClient apiNumerarioClient;

    public PaginaResponse<MovimentacaoResponse> listar(
            String authorization,
            Long agenciaId,
            String tipo,
            LocalDate dataInicio,
            LocalDate dataFim,
            int pagina,
            int tamanho
    ) {
        return apiNumerarioClient.listarMovimentacoes(
                authorization, agenciaId, tipo, dataInicio, dataFim, pagina, tamanho);
    }

    public MovimentacaoResponse criar(String authorization, RegistrarMovimentacaoRequest request) {
        return apiNumerarioClient.criarMovimentacao(authorization, request);
    }
}
