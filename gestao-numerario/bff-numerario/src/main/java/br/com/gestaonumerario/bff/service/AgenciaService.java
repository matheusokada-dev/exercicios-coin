package br.com.gestaonumerario.bff.service;

import br.com.gestaonumerario.bff.client.ApiNumerarioClient;
import br.com.gestaonumerario.bff.dto.AgenciaResponse;
import br.com.gestaonumerario.bff.dto.AtualizarAgenciaRequest;
import br.com.gestaonumerario.bff.dto.CriarAgenciaRequest;
import br.com.gestaonumerario.bff.dto.DetalheAgenciaResponse;
import br.com.gestaonumerario.bff.dto.PaginaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgenciaService {

    private final ApiNumerarioClient apiNumerarioClient;

    public PaginaResponse<AgenciaResponse> listar(
            String authorization,
            String busca,
            Boolean ativo,
            Boolean alerta,
            String ordenarPor,
            String direcao,
            int pagina,
            int tamanho
    ) {
        return apiNumerarioClient.listarAgencias(
                authorization, busca, ativo, alerta, ordenarPor, direcao, pagina, tamanho);
    }

    public DetalheAgenciaResponse detalhar(String authorization, Long id) {
        return apiNumerarioClient.detalharAgencia(authorization, id);
    }

    public AgenciaResponse criar(String authorization, CriarAgenciaRequest request) {
        return apiNumerarioClient.criarAgencia(authorization, request);
    }

    public AgenciaResponse atualizar(String authorization, Long id, AtualizarAgenciaRequest request) {
        return apiNumerarioClient.atualizarAgencia(authorization, id, request);
    }

    public void desativar(String authorization, Long id) {
        apiNumerarioClient.desativarAgencia(authorization, id);
    }
}
