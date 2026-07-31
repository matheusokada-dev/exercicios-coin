package br.com.gestaonumerario.relatorios.service;

import br.com.gestaonumerario.relatorios.dto.GerarRelatorioRequest;
import br.com.gestaonumerario.relatorios.dto.GerarRelatorioResponse;
import br.com.gestaonumerario.relatorios.exception.RelatorioInvalidoException;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class GerarRelatorioService {

    private static final String XLSX = "xlsx";
    private static final String LOGO_BRADESCO = "/logo-bradesco.png";
    private static final short VERMELHO_BRADESCO = IndexedColors.RED.getIndex();
    private static final int LINHA_CABECALHO = 5;
    private static final int MAXIMO_COLUNAS = 25;
    private static final int MAXIMO_LINHAS = 25_000;

    public GerarRelatorioResponse gerar(GerarRelatorioRequest request) {
        validar(request);
        String nomeArquivo = normalizarNome(request.metadados().nomeArquivo());

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            criarPlanilha(workbook, request);
            workbook.write(output);
            return new GerarRelatorioResponse(
                    Base64.getEncoder().encodeToString(output.toByteArray()),
                    nomeArquivo,
                    XLSX,
                    LocalDateTime.now()
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível gerar o arquivo Excel.", exception);
        }
    }

    private void validar(GerarRelatorioRequest request) {
        if (request == null) {
            throw new RelatorioInvalidoException("Os dados do relatório são obrigatórios.");
        }
        if (request.colunas() == null || request.colunas().isEmpty()) {
            throw new RelatorioInvalidoException("Ao menos uma coluna deve ser informada.");
        }
        if (request.linhas() == null || request.metadados() == null
                || request.metadados().nomeArquivo() == null
                || request.metadados().nomeArquivo().isBlank()) {
            throw new RelatorioInvalidoException("Linhas, metadados e nome do arquivo são obrigatórios.");
        }
        if (!XLSX.equalsIgnoreCase(request.formato())) {
            throw new RelatorioInvalidoException("Formato não suportado. Utilize xlsx.");
        }
        if (request.colunas().size() > MAXIMO_COLUNAS) {
            throw new RelatorioInvalidoException(
                    "O relatório não pode exceder " + MAXIMO_COLUNAS + " colunas."
            );
        }
        if (request.linhas().size() > MAXIMO_LINHAS) {
            throw new RelatorioInvalidoException(
                    "O relatório não pode exceder " + MAXIMO_LINHAS + " linhas."
            );
        }
        for (int indice = 0; indice < request.linhas().size(); indice++) {
            var linha = request.linhas().get(indice);
            if (linha == null) {
                throw new RelatorioInvalidoException("Linha " + (indice + 1) + " não pode ser nula.");
            }
            int celulas = linha.size();
            if (celulas != request.colunas().size()) {
                throw new RelatorioInvalidoException(
                        "Linha " + (indice + 1) + " possui " + celulas
                                + " célula(s), mas foram declaradas "
                                + request.colunas().size() + " coluna(s)"
                );
            }
        }
    }

    private void criarPlanilha(XSSFWorkbook workbook, GerarRelatorioRequest request) throws IOException {
        Sheet sheet = workbook.createSheet("Relatório");
        int ultimaColuna = request.colunas().size() - 1;

        criarTitulo(sheet, workbook, request, ultimaColuna);
        CellStyle cabecalho = estiloCabecalho(workbook);
        Row header = sheet.createRow(LINHA_CABECALHO);
        for (int coluna = 0; coluna < request.colunas().size(); coluna++) {
            Cell cell = header.createCell(coluna);
            cell.setCellValue(request.colunas().get(coluna));
            cell.setCellStyle(cabecalho);
        }

        CellStyle decimal = estiloNumero(workbook, "#,##0.00");
        CellStyle inteiro = estiloNumero(workbook, "#,##0");
        CellStyle texto = estiloDados(workbook);
        for (int linha = 0; linha < request.linhas().size(); linha++) {
            Row row = sheet.createRow(LINHA_CABECALHO + 1 + linha);
            for (int coluna = 0; coluna < request.colunas().size(); coluna++) {
                preencherCelula(
                        row.createCell(coluna),
                        request.linhas().get(linha).get(coluna),
                        texto,
                        inteiro,
                        decimal
                );
            }
        }

        sheet.setAutoFilter(new CellRangeAddress(
                LINHA_CABECALHO,
                LINHA_CABECALHO + request.linhas().size(),
                0,
                ultimaColuna
        ));
        sheet.createFreezePane(0, LINHA_CABECALHO + 1);
        ajustarLarguras(sheet, request.colunas().size());
        configurarImpressao(sheet);
    }

    private void criarTitulo(
            Sheet sheet,
            XSSFWorkbook workbook,
            GerarRelatorioRequest request,
            int ultimaColuna) throws IOException {
        int colunaApresentacao = Math.min(3, ultimaColuna);
        Row primeiraLinha = sheet.createRow(0);

        CellStyle marca = workbook.createCellStyle();
        Font marcaFonte = workbook.createFont();
        marcaFonte.setBold(true);
        marcaFonte.setFontHeightInPoints((short) 18);
        marcaFonte.setColor(VERMELHO_BRADESCO);
        marca.setFont(marcaFonte);
        Cell marcaCell = primeiraLinha.createCell(0);
        marcaCell.setBlank();
        marcaCell.setCellStyle(marca);
        inserirLogo(sheet, workbook);

        CellStyle titulo = workbook.createCellStyle();
        Font tituloFonte = workbook.createFont();
        tituloFonte.setBold(true);
        tituloFonte.setFontHeightInPoints((short) 16);
        titulo.setFont(tituloFonte);
        titulo.setAlignment(HorizontalAlignment.CENTER);
        Cell tituloCell = primeiraLinha.createCell(colunaApresentacao);
        tituloCell.setCellValue(request.metadados().titulo());
        tituloCell.setCellStyle(titulo);
        primeiraLinha.setHeightInPoints(22.05f);

        CellStyle metadado = estiloMetadado(workbook);
        criarLinhaMetadado(
                sheet,
                2,
                "Subtítulo: ",
                request.metadados().subtitulo(),
                colunaApresentacao,
                metadado
        );
        criarLinhaMetadado(
                sheet,
                3,
                "Período: ",
                request.metadados().periodo(),
                colunaApresentacao,
                metadado
        );
        criarLinhaMetadado(
                sheet,
                4,
                "Gerado por: ",
                request.metadados().usuario(),
                colunaApresentacao,
                metadado
        );
    }

    private void criarLinhaMetadado(
            Sheet sheet,
            int linha,
            String rotulo,
            String valor,
            int coluna,
            CellStyle estilo) {
        Row row = sheet.createRow(linha);
        Cell cell = row.createCell(coluna);
        cell.setCellValue(rotulo + (valor == null ? "" : valor));
        cell.setCellStyle(estilo);
    }

    private void inserirLogo(Sheet sheet, XSSFWorkbook workbook) throws IOException {
        byte[] imagem;
        try (InputStream input = GerarRelatorioService.class.getResourceAsStream(LOGO_BRADESCO)) {
            if (input == null) {
                throw new IOException("Logo do Bradesco não encontrada nos recursos da aplicação.");
            }
            imagem = input.readAllBytes();
        }

        int indiceImagem = workbook.addPicture(imagem, Workbook.PICTURE_TYPE_PNG);
        XSSFClientAnchor anchor = new XSSFClientAnchor(
                Units.pixelToEMU(8),
                Units.pixelToEMU(10),
                Units.pixelToEMU(100),
                Units.pixelToEMU(13),
                0,
                0,
                0,
                4
        );
        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_DONT_RESIZE);
        sheet.createDrawingPatriarch().createPicture(anchor, indiceImagem);
    }

    private CellStyle estiloMetadado(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        return style;
    }

    private void preencherCelula(
            Cell cell,
            JsonNode valor,
            CellStyle texto,
            CellStyle inteiro,
            CellStyle decimal) {
        if (valor == null || valor.isNull()) {
            cell.setBlank();
            cell.setCellStyle(texto);
        } else if (valor.isTextual() && valor.asText().isBlank()) {
            cell.setBlank();
            cell.setCellStyle(texto);
        } else if (valor.isIntegralNumber()) {
            cell.setCellValue(valor.longValue());
            cell.setCellStyle(inteiro);
        } else if (valor.isFloatingPointNumber() || valor.isBigDecimal()) {
            cell.setCellValue(valor.decimalValue().doubleValue());
            cell.setCellStyle(decimal);
        } else if (valor.isBoolean()) {
            cell.setCellValue(valor.booleanValue());
            cell.setCellStyle(texto);
        } else {
            cell.setCellValue(valor.asText());
            cell.setCellStyle(texto);
        }
    }

    private CellStyle estiloCabecalho(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        aplicarBordas(style);
        return style;
    }

    private CellStyle estiloDados(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        aplicarBordas(style);
        return style;
    }

    private CellStyle estiloNumero(Workbook workbook, String formato) {
        CellStyle style = estiloDados(workbook);
        style.setDataFormat(workbook.createDataFormat().getFormat(formato));
        return style;
    }

    private void aplicarBordas(CellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
    }

    private void ajustarLarguras(Sheet sheet, int quantidadeColunas) {
        for (int coluna = 0; coluna < quantidadeColunas; coluna++) {
            sheet.autoSizeColumn(coluna);
            int largura = Math.min(Math.max(sheet.getColumnWidth(coluna) + 768, 3_000), 15_000);
            sheet.setColumnWidth(coluna, largura);
        }
    }

    private void configurarImpressao(Sheet sheet) {
        PrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setPaperSize(PrintSetup.A4_PAPERSIZE);
        printSetup.setLandscape(false);
        printSetup.setFitWidth((short) 1);
        printSetup.setFitHeight((short) 1);
        sheet.setAutobreaks(true);
        sheet.setFitToPage(true);
    }

    private String normalizarNome(String nome) {
        String limpo = nome.trim();
        return limpo.toLowerCase().endsWith(".xlsx") ? limpo : limpo + ".xlsx";
    }
}
