package br.com.gestaonumerario.api.adapter.output.mapper;

import br.com.gestaonumerario.api.adapter.output.repository.entity.UnidadeOperacionalEntity;
import br.com.gestaonumerario.api.core.domain.model.UnidadeOperacional;
import org.springframework.stereotype.Component;

@Component
public class UnidadeOperacionalPersistenceMapper {
    public UnidadeOperacional toDomain(UnidadeOperacionalEntity e) {
        if (e==null) return null;
        return new UnidadeOperacional(e.getId(),e.getTipo(),e.getCodigo(),e.getNome(),
                e.isControlaSaldo(),e.getSaldoAtual(),e.isAtivo(),e.getVersao(),
                e.getCriadoEm(),e.getAtualizadoEm());
    }
    public UnidadeOperacionalEntity toEntity(UnidadeOperacional d) {
        if (d==null) return null;
        return new UnidadeOperacionalEntity(d.getId(),d.getTipo(),d.getCodigo(),d.getNome(),
                d.isControlaSaldo(),d.getSaldoAtual(),d.isAtivo(),d.getVersao(),
                d.getCriadoEm(),d.getAtualizadoEm());
    }
}
