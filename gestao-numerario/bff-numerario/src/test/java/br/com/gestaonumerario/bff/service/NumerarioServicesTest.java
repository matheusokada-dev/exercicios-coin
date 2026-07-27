package br.com.gestaonumerario.bff.service;

import br.com.gestaonumerario.bff.client.ApiNumerarioClient;
import br.com.gestaonumerario.bff.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NumerarioServicesTest {
    private ApiNumerarioClient client;
    private ComandoNumerarioService comandos;
    private ConsultaNumerarioService consultas;

    @BeforeEach
    void configurar() {
        client = mock(ApiNumerarioClient.class);
        comandos = new ComandoNumerarioService(client);
        consultas = new ConsultaNumerarioService(client);
    }

    @Test
    void deveDelegarTodasAsConsultas() {
        LocalDate inicio = LocalDate.of(2026, 1, 1);
        LocalDate fim = LocalDate.of(2026, 1, 31);

        consultas.solicitacoes("Bearer token", 1L, "SUPRIMENTO", "PENDENTE",
                2L, 3L, inicio, fim, 0, 20);
        consultas.detalhe("Bearer token", 4L);
        consultas.historico("Bearer token", 4L);
        consultas.unidades("Bearer token", "AGENCIA");
        consultas.operacoes("Bearer token", "PROGRAMADA", 2L, 3L,
                inicio, fim, 1, 10);

        verify(client).listarSolicitacoesNumerario("Bearer token", 1L, "SUPRIMENTO",
                "PENDENTE", 2L, 3L, inicio, fim, 0, 20);
        verify(client).detalharSolicitacaoNumerario("Bearer token", 4L);
        verify(client).consultarHistoricoSolicitacao("Bearer token", 4L);
        verify(client).listarUnidadesOperacionais("Bearer token", "AGENCIA");
        verify(client).listarOperacoesNumerario("Bearer token", "PROGRAMADA",
                2L, 3L, inicio, fim, 1, 10);
    }

    @Test
    void deveDelegarTodosOsComandos() {
        CriarSolicitacaoNumerarioRequest criar = mock(CriarSolicitacaoNumerarioRequest.class);
        DecidirSolicitacaoNumerarioRequest decidir = mock(DecidirSolicitacaoNumerarioRequest.class);
        ProgramarOperacaoNumerarioRequest programar = mock(ProgramarOperacaoNumerarioRequest.class);
        VersaoOperacaoNumerarioRequest versao = mock(VersaoOperacaoNumerarioRequest.class);
        ExecutarOperacaoNumerarioRequest executar = mock(ExecutarOperacaoNumerarioRequest.class);
        OcorrenciaOperacaoNumerarioRequest ocorrencia = mock(OcorrenciaOperacaoNumerarioRequest.class);
        ReceberOperacaoNumerarioRequest receber = mock(ReceberOperacaoNumerarioRequest.class);
        ConciliarOperacaoNumerarioRequest conciliar = mock(ConciliarOperacaoNumerarioRequest.class);
        CargaInicialTesourariaRequest carga = mock(CargaInicialTesourariaRequest.class);
        AjustarDivergenciaRequest ajuste = mock(AjustarDivergenciaRequest.class);

        comandos.criar("Bearer token", criar);
        comandos.decidir("Bearer token", 1L, "aprovar", decidir);
        comandos.programar("Bearer token", 1L, "p-1", programar);
        comandos.separar("Bearer token", 1L, versao);
        comandos.expedir("Bearer token", 1L, "e-1", executar);
        comandos.ocorrencia("Bearer token", 1L, ocorrencia);
        comandos.receber("Bearer token", 1L, "r-1", receber);
        comandos.conciliar("Bearer token", 1L, "c-1", conciliar);
        comandos.carga("Bearer token", "t-1", carga);
        comandos.ajustar("Bearer token", 1L, "a-1", ajuste);

        verify(client).criarSolicitacaoNumerario("Bearer token", criar);
        verify(client).decidirSolicitacaoNumerario("Bearer token", 1L, "aprovar", decidir);
        verify(client).programarOperacao("Bearer token", 1L, "p-1", programar);
        verify(client).iniciarSeparacao("Bearer token", 1L, versao);
        verify(client).expedirOperacao("Bearer token", 1L, "e-1", executar);
        verify(client).registrarOcorrencia("Bearer token", 1L, ocorrencia);
        verify(client).receberOperacao("Bearer token", 1L, "r-1", receber);
        verify(client).conciliarOperacao("Bearer token", 1L, "c-1", conciliar);
        verify(client).realizarCargaInicial("Bearer token", "t-1", carga);
        verify(client).ajustarDivergencia("Bearer token", 1L, "a-1", ajuste);
    }
}
