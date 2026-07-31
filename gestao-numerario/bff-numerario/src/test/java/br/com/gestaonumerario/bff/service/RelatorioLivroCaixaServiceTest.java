package br.com.gestaonumerario.bff.service;

import br.com.gestaonumerario.bff.client.ApiNumerarioClient;
import br.com.gestaonumerario.bff.client.RelatoriosClient;
import br.com.gestaonumerario.bff.dto.AgenciaResponse;
import br.com.gestaonumerario.bff.dto.DetalheAgenciaResponse;
import br.com.gestaonumerario.bff.dto.GerarLivroCaixaRequest;
import br.com.gestaonumerario.bff.dto.GerarRelatorioRequest;
import br.com.gestaonumerario.bff.dto.GerarRelatorioResponse;
import br.com.gestaonumerario.bff.dto.MovimentacaoResponse;
import br.com.gestaonumerario.bff.dto.PaginaResponse;
import br.com.gestaonumerario.bff.dto.SessaoResponse;
import br.com.gestaonumerario.bff.exception.PeriodoRelatorioInvalidoException;
import br.com.gestaonumerario.bff.exception.RelatorioSemDadosException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RelatorioLivroCaixaServiceTest {

    private ApiNumerarioClient apiNumerarioClient;
    private RelatoriosClient relatoriosClient;
    private RelatorioLivroCaixaService service;

    @BeforeEach
    void configurar() {
        apiNumerarioClient = mock(ApiNumerarioClient.class);
        relatoriosClient = mock(RelatoriosClient.class);
        service = new RelatorioLivroCaixaService(apiNumerarioClient, relatoriosClient);
    }

    @Test
    void deveCentralizarDadosPaginadosMetadadosETotais() {
        LocalDate inicio = LocalDate.of(2026, 7, 1);
        LocalDate fim = LocalDate.of(2026, 7, 29);
        GerarLivroCaixaRequest entrada = new GerarLivroCaixaRequest(7L, inicio, fim);
        AgenciaResponse agencia = new AgenciaResponse(
                7L,
                "0007",
                "Agência Centro",
                "São Paulo",
                new BigDecimal("1300.00"),
                new BigDecimal("100.00"),
                true,
                false,
                BigDecimal.ZERO,
                1
        );
        when(apiNumerarioClient.detalharAgencia("Bearer token", 7L))
                .thenReturn(new DetalheAgenciaResponse(
                        agencia,
                        fim,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                ));
        when(apiNumerarioClient.consultarSessao("Bearer token"))
                .thenReturn(new SessaoResponse(9L, "Maria Gestora", "GESTOR", Instant.parse("2026-07-29T20:00:00Z")));

        MovimentacaoResponse saida = movimento(
                2L,
                false,
                "200.00",
                "1500.00",
                "1300.00",
                "2026-07-02T13:00:00Z"
        );
        MovimentacaoResponse entradaMovimento = movimento(
                1L,
                true,
                "500.00",
                "1000.00",
                "1500.00",
                "2026-07-01T12:00:00Z"
        );
        when(apiNumerarioClient.listarMovimentacoes(
                "Bearer token",
                7L,
                null,
                inicio,
                fim,
                0,
                100
        )).thenReturn(new PaginaResponse<>(List.of(saida), 0, 100, 2, 2));
        when(apiNumerarioClient.listarMovimentacoes(
                "Bearer token",
                7L,
                null,
                inicio,
                fim,
                1,
                100
        )).thenReturn(new PaginaResponse<>(List.of(entradaMovimento), 1, 100, 2, 2));
        GerarRelatorioResponse resposta = new GerarRelatorioResponse(
                "UEsDBA==",
                "livro-caixa-0007.xlsx",
                "xlsx",
                "2026-07-29T14:00:00"
        );
        when(relatoriosClient.gerar(any())).thenReturn(resposta);

        assertThat(service.gerar("Bearer token", entrada)).isSameAs(resposta);

        ArgumentCaptor<GerarRelatorioRequest> captor = ArgumentCaptor.forClass(GerarRelatorioRequest.class);
        verify(relatoriosClient).gerar(captor.capture());
        GerarRelatorioRequest request = captor.getValue();
        assertThat(request.colunas()).hasSize(10);
        assertThat(request.linhas()).hasSize(3);
        assertThat(request.linhas().getFirst())
                .containsExactly(
                        "01/07/2026 09:00:00",
                        "DEPOSITO",
                        "Entrada",
                        "Movimento 1",
                        101L,
                        new BigDecimal("1000.00"),
                        new BigDecimal("500.00"),
                        "",
                        new BigDecimal("1500.00"),
                        9L
                );
        assertThat(request.linhas().getLast())
                .containsExactly("", "", "", "TOTAIS", "", "", new BigDecimal("500.00"),
                        new BigDecimal("200.00"), "", "");
        assertThat(request.metadados().titulo()).isEqualTo("Relatório Livro Caixa");
        assertThat(request.metadados().subtitulo()).isEqualTo("Agência: 0007 - Agência Centro");
        assertThat(request.metadados().periodo()).isEqualTo("01/07/2026 - 29/07/2026");
        assertThat(request.metadados().usuario()).isEqualTo("Maria Gestora");
        assertThat(request.metadados().nomeArquivo()).isEqualTo("livro-caixa-0007-2026-07-01-2026-07-29");
        assertThat(request.formato()).isEqualTo("xlsx");
    }

    @Test
    void deveRejeitarPeriodoInvertidoAntesDeConsultarServicos() {
        GerarLivroCaixaRequest request = new GerarLivroCaixaRequest(
                1L,
                LocalDate.of(2026, 7, 2),
                LocalDate.of(2026, 7, 1)
        );

        assertThatThrownBy(() -> service.gerar("Bearer token", request))
                .isInstanceOf(PeriodoRelatorioInvalidoException.class);
        verifyNoInteractions(apiNumerarioClient, relatoriosClient);
    }

    @Test
    void deveRejeitarRelatorioSemMovimentacoes() {
        LocalDate data = LocalDate.of(2026, 7, 1);
        GerarLivroCaixaRequest request = new GerarLivroCaixaRequest(1L, data, data);
        AgenciaResponse agencia = new AgenciaResponse(
                1L,
                "0001",
                "Agência",
                "Cidade",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                true,
                false,
                BigDecimal.ZERO,
                0
        );
        when(apiNumerarioClient.detalharAgencia("Bearer token", 1L))
                .thenReturn(new DetalheAgenciaResponse(
                        agencia,
                        data,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                ));
        when(apiNumerarioClient.consultarSessao("Bearer token"))
                .thenReturn(new SessaoResponse(1L, "Gestor", "GESTOR", null));
        when(apiNumerarioClient.listarMovimentacoes(
                "Bearer token",
                1L,
                null,
                data,
                data,
                0,
                100
        )).thenReturn(new PaginaResponse<>(List.of(), 0, 100, 0, 0));

        assertThatThrownBy(() -> service.gerar("Bearer token", request))
                .isInstanceOf(RelatorioSemDadosException.class);
        verifyNoInteractions(relatoriosClient);
    }

    private MovimentacaoResponse movimento(
            Long id,
            boolean entrada,
            String valor,
            String saldoAnterior,
            String saldoPosterior,
            String instante) {
        return new MovimentacaoResponse(
                id,
                7L,
                100L + id,
                entrada ? "DEPOSITO" : "SAQUE",
                entrada,
                new BigDecimal(valor),
                new BigDecimal(saldoAnterior),
                new BigDecimal(saldoPosterior),
                "Movimento " + id,
                Instant.parse(instante),
                9L
        );
    }
}
