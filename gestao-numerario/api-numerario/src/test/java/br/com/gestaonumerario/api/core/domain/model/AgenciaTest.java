package br.com.gestaonumerario.api.core.domain.model;

import br.com.gestaonumerario.api.core.exception.RegraOperacaoNumerarioException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AgenciaTest {

    @Test
    void agenciaInativaNaoDeveReceberMovimentacao() {
        Agencia agencia = agenciaInativa();

        assertThrows(
                RegraOperacaoNumerarioException.class,
                () -> agencia.abastecer(new BigDecimal("10.00"))
        );
        assertThrows(
                RegraOperacaoNumerarioException.class,
                () -> agencia.retirar(new BigDecimal("10.00"))
        );
    }

    @Test
    void agenciaInativaNaoDeveSerAlteradaPorFluxoComum() {
        Agencia agencia = agenciaInativa();

        assertThrows(
                RegraOperacaoNumerarioException.class,
                () -> agencia.atualizarDados(
                        "Novo nome",
                        "São Paulo",
                        new BigDecimal("20.00")
                )
        );
    }

    private Agencia agenciaInativa() {
        return new Agencia(
                1L,
                "0001",
                "Agência Centro",
                "São Paulo",
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                false,
                0
        );
    }
}
