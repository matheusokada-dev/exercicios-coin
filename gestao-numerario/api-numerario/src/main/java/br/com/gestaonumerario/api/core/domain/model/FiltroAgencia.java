package br.com.gestaonumerario.api.core.domain.model;

import br.com.gestaonumerario.api.core.domain.enums.CampoOrdenacaoAgencia;
import br.com.gestaonumerario.api.core.domain.enums.DirecaoOrdenacao;

public record FiltroAgencia(
        String busca,
        Boolean ativo,
        Boolean alerta,
        CampoOrdenacaoAgencia ordenarPor,
        DirecaoOrdenacao direcao,
        int pagina,
        int tamanho
) {
}
