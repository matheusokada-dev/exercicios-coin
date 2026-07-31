package br.com.gestaonumerario.bff.service;

import br.com.gestaonumerario.bff.client.ApiNumerarioClient;
import br.com.gestaonumerario.bff.client.RelatoriosClient;
import br.com.gestaonumerario.bff.dto.AgenciaResponse;
import br.com.gestaonumerario.bff.dto.GerarLivroCaixaRequest;
import br.com.gestaonumerario.bff.dto.GerarRelatorioRequest;
import br.com.gestaonumerario.bff.dto.GerarRelatorioResponse;
import br.com.gestaonumerario.bff.dto.MetadadosRelatorio;
import br.com.gestaonumerario.bff.dto.MovimentacaoResponse;
import br.com.gestaonumerario.bff.exception.PeriodoRelatorioInvalidoException;
import br.com.gestaonumerario.bff.exception.RelatorioSemDadosException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RelatorioLivroCaixaService {

    private static final int TAMANHO_PAGINA = 100;
    private static final ZoneId FUSO_RELATORIO = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm:ss")
            .withZone(FUSO_RELATORIO);
    private static final List<String> COLUNAS = List.of(
            "Data e hora",
            "Tipo",
            "Direção",
            "Descrição",
            "Solicitação",
            "Saldo anterior",
            "Entrada",
            "Saída",
            "Saldo posterior",
            "Usuário"
    );

    private final ApiNumerarioClient apiNumerarioClient;
    private final RelatoriosClient relatoriosClient;

    public GerarRelatorioResponse gerar(String authorization, GerarLivroCaixaRequest request) {
        if (request.dataFim().isBefore(request.dataInicio())) {
            throw new PeriodoRelatorioInvalidoException();
        }

        AgenciaResponse agencia = apiNumerarioClient
                .detalharAgencia(authorization, request.agenciaId())
                .agencia();
        var sessao = apiNumerarioClient.consultarSessao(authorization);
        List<MovimentacaoResponse> movimentacoes = carregarMovimentacoes(authorization, request);
        if (movimentacoes.isEmpty()) {
            throw new RelatorioSemDadosException();
        }

        List<List<Object>> linhas = new ArrayList<>(movimentacoes.size() + 1);
        BigDecimal totalEntradas = BigDecimal.ZERO;
        BigDecimal totalSaidas = BigDecimal.ZERO;
        for (MovimentacaoResponse movimentacao : movimentacoes) {
            BigDecimal entrada = movimentacao.entrada() ? movimentacao.valor() : null;
            BigDecimal saida = movimentacao.entrada() ? null : movimentacao.valor();
            totalEntradas = totalEntradas.add(entrada == null ? BigDecimal.ZERO : entrada);
            totalSaidas = totalSaidas.add(saida == null ? BigDecimal.ZERO : saida);
            linhas.add(List.of(
                    DATA_HORA.format(movimentacao.dataMovimento()),
                    movimentacao.tipo(),
                    movimentacao.entrada() ? "Entrada" : "Saída",
                    movimentacao.descricao() == null ? "" : movimentacao.descricao(),
                    movimentacao.solicitacaoId() == null ? "" : movimentacao.solicitacaoId(),
                    movimentacao.saldoAnterior(),
                    entrada == null ? "" : entrada,
                    saida == null ? "" : saida,
                    movimentacao.saldoPosterior(),
                    movimentacao.usuarioId()
            ));
        }
        linhas.add(List.of("", "", "", "TOTAIS", "", "", totalEntradas, totalSaidas, "", ""));

        String periodo = DATA.format(request.dataInicio()) + " - " + DATA.format(request.dataFim());
        String nomeArquivo = "livro-caixa-" + agencia.codigo() + "-"
                + request.dataInicio() + "-" + request.dataFim();
        return relatoriosClient.gerar(new GerarRelatorioRequest(
                COLUNAS,
                linhas,
                new MetadadosRelatorio(
                        "Relatório Livro Caixa",
                        "Agência: " + agencia.codigo() + " - " + agencia.nome(),
                        periodo,
                        sessao.nome(),
                        nomeArquivo
                ),
                "xlsx"
        ));
    }

    private List<MovimentacaoResponse> carregarMovimentacoes(
            String authorization,
            GerarLivroCaixaRequest request) {
        var primeira = apiNumerarioClient.listarMovimentacoes(
                authorization,
                request.agenciaId(),
                null,
                request.dataInicio(),
                request.dataFim(),
                0,
                TAMANHO_PAGINA
        );
        List<MovimentacaoResponse> movimentacoes = new ArrayList<>(primeira.itens());
        for (int pagina = 1; pagina < primeira.totalPaginas(); pagina++) {
            movimentacoes.addAll(apiNumerarioClient.listarMovimentacoes(
                    authorization,
                    request.agenciaId(),
                    null,
                    request.dataInicio(),
                    request.dataFim(),
                    pagina,
                    TAMANHO_PAGINA
            ).itens());
        }
        movimentacoes.sort(Comparator.comparing(MovimentacaoResponse::dataMovimento));
        return movimentacoes;
    }
}
