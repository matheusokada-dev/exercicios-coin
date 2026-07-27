package br.com.gestaonumerario.bff.service;

import br.com.gestaonumerario.bff.client.ApiNumerarioClient;
import br.com.gestaonumerario.bff.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultaNumerarioService {
    private final ApiNumerarioClient client;

    public PaginaResponse<SolicitacaoNumerarioResponse> solicitacoes(String auth,
            Long agenciaId,String tipo,String status,Long origemId,Long destinoId,
            LocalDate inicio,LocalDate fim,int pagina,int tamanho) {
        return client.listarSolicitacoesNumerario(auth,agenciaId,tipo,status,origemId,destinoId,
                inicio,fim,pagina,tamanho);
    }
    public DetalheSolicitacaoNumerarioResponse detalhe(String auth,Long id) {
        return client.detalharSolicitacaoNumerario(auth,id);
    }
    public List<HistoricoSolicitacaoResponse> historico(String auth,Long id) {
        return client.consultarHistoricoSolicitacao(auth,id);
    }
    public List<UnidadeOperacionalResponse> unidades(String auth,String tipo) {
        return client.listarUnidadesOperacionais(auth,tipo);
    }
    public PaginaResponse<OperacaoNumerarioResponse> operacoes(String auth,String status,
            Long origemId,Long destinoId,LocalDate inicio,LocalDate fim,int pagina,int tamanho) {
        return client.listarOperacoesNumerario(auth,status,origemId,destinoId,inicio,fim,pagina,tamanho);
    }
}
