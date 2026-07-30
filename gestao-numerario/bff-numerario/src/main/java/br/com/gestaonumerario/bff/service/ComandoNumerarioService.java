package br.com.gestaonumerario.bff.service;

import br.com.gestaonumerario.bff.client.ApiNumerarioClient;
import br.com.gestaonumerario.bff.dto.AjustarDivergenciaRequest;
import br.com.gestaonumerario.bff.dto.CargaInicialTesourariaRequest;
import br.com.gestaonumerario.bff.dto.ConciliarOperacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.CriarSolicitacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.DecidirSolicitacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.ExecutarOperacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.OcorrenciaOperacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.OperacaoNumerarioResponse;
import br.com.gestaonumerario.bff.dto.ProgramarOperacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.ReceberOperacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.SolicitacaoNumerarioResponse;
import br.com.gestaonumerario.bff.dto.UnidadeOperacionalResponse;
import br.com.gestaonumerario.bff.dto.VersaoOperacaoNumerarioRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ComandoNumerarioService {
    private final ApiNumerarioClient client;
    public SolicitacaoNumerarioResponse criar(String a, CriarSolicitacaoNumerarioRequest r) {
        return client.criarSolicitacaoNumerario(
                a,
                r
        );
    }

    public SolicitacaoNumerarioResponse decidir(String a, Long id, String acao, DecidirSolicitacaoNumerarioRequest r) {
        return client.decidirSolicitacaoNumerario(
                a,
                id,
                acao,
                r
        );
    }

    public OperacaoNumerarioResponse programar(String a, Long id, String k, ProgramarOperacaoNumerarioRequest r) {
        return client.programarOperacao(
                a,
                id,
                k,
                r
        );
    }

    public OperacaoNumerarioResponse separar(String a, Long id, VersaoOperacaoNumerarioRequest r) {
        return client.iniciarSeparacao(
                a,
                id,
                r
        );
    }

    public OperacaoNumerarioResponse expedir(String a, Long id, String k, ExecutarOperacaoNumerarioRequest r) {
        return client.expedirOperacao(
                a,
                id,
                k,
                r
        );
    }

    public OperacaoNumerarioResponse ocorrencia(String a, Long id, OcorrenciaOperacaoNumerarioRequest r) {
        return client.registrarOcorrencia(
                a,
                id,
                r
        );
    }

    public OperacaoNumerarioResponse receber(String a, Long id, String k, ReceberOperacaoNumerarioRequest r) {
        return client.receberOperacao(
                a,
                id,
                k,
                r
        );
    }

    public OperacaoNumerarioResponse conciliar(String a, Long id, String k, ConciliarOperacaoNumerarioRequest r) {
        return client.conciliarOperacao(
                a,
                id,
                k,
                r
        );
    }

    public UnidadeOperacionalResponse carga(String a, String k, CargaInicialTesourariaRequest r) {
        return client.realizarCargaInicial(
                a,
                k,
                r
        );
    }

    public UnidadeOperacionalResponse ajustar(String a, Long id, String k, AjustarDivergenciaRequest r) {
        return client.ajustarDivergencia(
                a,
                id,
                k,
                r
        );
    }
}
