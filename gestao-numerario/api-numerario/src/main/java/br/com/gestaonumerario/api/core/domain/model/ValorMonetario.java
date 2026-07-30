package br.com.gestaonumerario.api.core.domain.model;

import br.com.gestaonumerario.api.core.exception.ValorDeveSerPositivoException;
import br.com.gestaonumerario.api.core.exception.ValorMonetarioObrigatorioException;
import br.com.gestaonumerario.api.core.exception.ValorNaoPodeSerNegativoException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ValorMonetario {

    public static final int ESCALA = 2;
    public static final RoundingMode ARREDONDAMENTO = RoundingMode.HALF_EVEN;

    private ValorMonetario() {
    }

    public static BigDecimal normalizar(BigDecimal valor) {
        if (valor == null) {
            throw new ValorMonetarioObrigatorioException();
        }

        return valor.setScale(
                ESCALA,
                ARREDONDAMENTO
        );
    }

    public static BigDecimal exigirPositivo(BigDecimal valor) {
        BigDecimal valorNormalizado = normalizar(valor);

        if (valorNormalizado.signum() <= 0) {
            throw new ValorDeveSerPositivoException();
        }

        return valorNormalizado;
    }

    public static BigDecimal exigirNaoNegativo(BigDecimal valor) {
        BigDecimal valorNormalizado = normalizar(valor);

        if (valorNormalizado.signum() < 0) {
            throw new ValorNaoPodeSerNegativoException();
        }

        return valorNormalizado;
    }

    public static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(
                ESCALA,
                ARREDONDAMENTO
        );
    }
}
