package br.com.gestaonumerario.api.adapter.output.mapper;

import br.com.gestaonumerario.api.adapter.output.repository.entity.AgenciaEntity;
import br.com.gestaonumerario.api.core.domain.enums.TipoUnidadeOperacional;
import br.com.gestaonumerario.api.core.domain.model.UnidadeOperacional;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * Adaptador de compatibilidade da API. Agencias possuem saldo proprio e formam
 * as rotas persistidas. A tesouraria virtual existe somente para o endpoint
 * financeiro legado de carga inicial e nao participa das transferencias.
 */
@Component
public class UnidadeOperacionalVirtualMapper {

    public static final long TESOURARIA_ID = -1L;
    private static final Instant EPOCA = Instant.parse("2026-01-01T00:00:00Z");

    public UnidadeOperacional agencia(AgenciaEntity agencia) {
        if (agencia == null) {
            return null;
        }
        return new UnidadeOperacional(
                agencia.getId(),
                TipoUnidadeOperacional.AGENCIA,
                agencia.getCodigo(),
                agencia.getNome(),
                true,
                agencia.getSaldoAtual(),
                agencia.isAtivo(),
                agencia.getVersao(),
                EPOCA,
                EPOCA
        );
    }

    public UnidadeOperacional tesouraria() {
        return new UnidadeOperacional(
                TESOURARIA_ID,
                TipoUnidadeOperacional.TESOURARIA,
                "TES-001",
                "Tesouraria Central",
                true,
                new BigDecimal("999999999999999.99"),
                true,
                0,
                EPOCA,
                EPOCA
        );
    }
}
