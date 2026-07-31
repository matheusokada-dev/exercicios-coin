package br.com.gestaonumerario.bff.controller;

import br.com.gestaonumerario.bff.dto.GerarLivroCaixaRequest;
import br.com.gestaonumerario.bff.dto.GerarRelatorioResponse;
import br.com.gestaonumerario.bff.service.RelatorioLivroCaixaService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RelatorioControllerTest {

    @Test
    void deveDelegarGeracaoAoCasoDeUso() {
        RelatorioLivroCaixaService service = mock(RelatorioLivroCaixaService.class);
        RelatorioController controller = new RelatorioController(service);
        GerarLivroCaixaRequest request = new GerarLivroCaixaRequest(
                1L,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 29)
        );
        GerarRelatorioResponse response = new GerarRelatorioResponse(
                "base64",
                "livro.xlsx",
                "xlsx",
                "2026-07-29T14:00:00"
        );
        when(service.gerar("Bearer token", request)).thenReturn(response);

        assertThat(controller.gerarLivroCaixa("Bearer token", request)).isSameAs(response);
        verify(service).gerar("Bearer token", request);
    }
}
