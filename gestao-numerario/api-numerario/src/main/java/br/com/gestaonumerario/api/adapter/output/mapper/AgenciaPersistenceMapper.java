package br.com.gestaonumerario.api.adapter.output.mapper;

import br.com.gestaonumerario.api.adapter.output.repository.entity.AgenciaEntity;
import br.com.gestaonumerario.api.core.domain.model.Agencia;
import org.springframework.stereotype.Component;

@Component
public class AgenciaPersistenceMapper {

    public Agencia toDomain(AgenciaEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Agencia(entity.getId(), entity.getCodigo(), entity.getNome(), entity.getCidade(),
                entity.getSaldoAtual(), entity.getLimiteMinimo(), entity.isAtivo(), entity.getVersao());
    }

    public AgenciaEntity toEntity(Agencia domain) {
        if (domain == null) {
            return null;
        }

        return new AgenciaEntity(domain.getId(), domain.getCodigo(), domain.getNome(), domain.getCidade(),
                domain.getSaldoAtual(), domain.getLimiteMinimo(), domain.isAtivo(), domain.getVersao());
    }
}

