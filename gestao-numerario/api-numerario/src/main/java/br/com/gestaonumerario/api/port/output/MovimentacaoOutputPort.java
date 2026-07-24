package br.com.gestaonumerario.api.port.output;

import br.com.gestaonumerario.api.core.domain.model.Movimentacao;
import br.com.gestaonumerario.api.core.domain.model.Pagina;
import br.com.gestaonumerario.api.core.domain.model.FiltroMovimentacao;
import br.com.gestaonumerario.api.core.domain.model.ResumoMovimentacaoDiaria;

import java.time.LocalDate;

public interface MovimentacaoOutputPort {

    boolean existePorIdempotencyKey(String idempotencyKey);

    Pagina<Movimentacao> buscar(FiltroMovimentacao filtro);

    ResumoMovimentacaoDiaria resumirDiaPorAgencia(Long agenciaId, LocalDate dataReferencia);

    Movimentacao salvar(Movimentacao movimentacao);
}

