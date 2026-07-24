package br.com.gestaonumerario.api.adapter.output.mapper;

import br.com.gestaonumerario.api.adapter.output.repository.entity.AgenciaEntity;
import br.com.gestaonumerario.api.adapter.output.repository.entity.SolicitacaoAbastecimentoEntity;
import br.com.gestaonumerario.api.adapter.output.repository.entity.UsuarioEntity;
import br.com.gestaonumerario.api.core.domain.model.SolicitacaoAbastecimento;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SolicitacaoAbastecimentoPersistenceMapper {

    private final AgenciaPersistenceMapper agenciaMapper;
    private final UsuarioPersistenceMapper usuarioMapper;

    public SolicitacaoAbastecimento toDomain(SolicitacaoAbastecimentoEntity entity) {
        if (entity == null) {
            return null;
        }

        return SolicitacaoAbastecimento.reconstituir(
                entity.getId(), agenciaMapper.toDomain(entity.getAgencia()), entity.getValor(), entity.getMotivo(),
                entity.getDataDesejada(), entity.getStatus(), usuarioMapper.toDomain(entity.getSolicitante()),
                usuarioMapper.toDomain(entity.getDecisor()), entity.getJustificativaDecisao(),
                entity.getJustificativaEspecial(), entity.getDataCriacao(), entity.getDataDecisao(),
                entity.getDataAtendimento(), entity.getVersao());
    }

    public SolicitacaoAbastecimentoEntity toEntity(
            SolicitacaoAbastecimento domain,
            AgenciaEntity agencia,
            UsuarioEntity solicitante,
            UsuarioEntity decisor
    ) {
        if (domain == null) {
            return null;
        }

        return new SolicitacaoAbastecimentoEntity(
                domain.getId(), agencia, domain.getValor(), domain.getMotivo(), domain.getDataDesejada(),
                domain.getStatus(), solicitante, decisor, domain.getJustificativaDecisao(),
                domain.getJustificativaEspecial(), domain.getDataCriacao(), domain.getDataDecisao(),
                domain.getDataAtendimento(), domain.getVersao());
    }
}

