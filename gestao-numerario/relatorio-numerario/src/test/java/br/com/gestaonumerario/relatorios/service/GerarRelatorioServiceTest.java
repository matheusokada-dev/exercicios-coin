package br.com.gestaonumerario.relatorios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.gestaonumerario.relatorios.dto.GerarRelatorioRequest;
import br.com.gestaonumerario.relatorios.dto.MetadadosRelatorio;
import br.com.gestaonumerario.relatorios.exception.RelatorioInvalidoException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class GerarRelatorioServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final GerarRelatorioService service = new GerarRelatorioService();

    @Test
    void deveGerarXlsxValidoComContratoDocumentado() throws Exception {
        var request = new GerarRelatorioRequest(
                List.of("Código", "Valor"),
                List.of(
                        List.of(mapper.valueToTree("C001"), mapper.valueToTree(1500.25)),
                        List.of(mapper.valueToTree(""), mapper.valueToTree(0))
                ),
                new MetadadosRelatorio(
                        "Relatório",
                        "Agência 1",
                        "01/07/2026 - 29/07/2026",
                        "Gestor",
                        "livro-caixa"
                ),
                "xlsx"
        );

        var response = service.gerar(request);
        byte[] arquivo = Base64.getDecoder().decode(response.conteudo());

        assertThat(response.nomeArquivo()).isEqualTo("livro-caixa.xlsx");
        assertThat(arquivo).startsWith((byte) 'P', (byte) 'K');
        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(arquivo))) {
            var sheet = workbook.getSheetAt(0);
            assertThat(sheet.getRow(0).getCell(1).getStringCellValue())
                    .isEqualTo("Relatório");
            assertThat(sheet.getRow(2).getCell(1).getStringCellValue())
                    .isEqualTo("Subtítulo: Agência 1");
            assertThat(sheet.getRow(5).getCell(0).getStringCellValue()).isEqualTo("Código");
            assertThat(sheet.getRow(6).getCell(1).getNumericCellValue()).isEqualTo(1500.25);
            assertThat(sheet.getRow(7).getCell(0).getCellType())
                    .isEqualTo(org.apache.poi.ss.usermodel.CellType.BLANK);
            assertThat(workbook.getAllPictures()).hasSize(1);
            assertThat(sheet.getDrawingPatriarch().getShapes()).hasSize(1);
            assertThat(sheet.getPrintSetup().getPaperSize())
                    .isEqualTo(org.apache.poi.ss.usermodel.PrintSetup.A4_PAPERSIZE);
        }
    }

    @Test
    void deveRejeitarLinhaComQuantidadeDeCelulasDiferente() {
        var request = new GerarRelatorioRequest(
                List.of("Código", "Valor"),
                List.of(List.of(mapper.valueToTree("C001"))),
                new MetadadosRelatorio("Relatório", null, null, null, "arquivo"),
                "xlsx"
        );

        assertThatThrownBy(() -> service.gerar(request))
                .isInstanceOf(RelatorioInvalidoException.class)
                .hasMessage("Linha 1 possui 1 célula(s), mas foram declaradas 2 coluna(s)");
    }

    @Test
    void deveRejeitarQuantidadeDeColunasQueExcedeOLimite() {
        var colunas = java.util.stream.IntStream.range(0, 26)
                .mapToObj(indice -> "Coluna " + indice)
                .toList();
        var request = new GerarRelatorioRequest(
                colunas,
                List.of(),
                new MetadadosRelatorio("Relatório", null, null, null, "arquivo"),
                "xlsx"
        );

        assertThatThrownBy(() -> service.gerar(request))
                .isInstanceOf(RelatorioInvalidoException.class)
                .hasMessage("O relatório não pode exceder 25 colunas.");
    }
}
