package br.com.gestaonumerario.api.adapter.output.mapper;

import br.com.gestaonumerario.api.adapter.output.repository.entity.SolicitacaoAbastecimentoEntity;
import br.com.gestaonumerario.api.core.domain.model.SolicitacaoNumerario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor
public class SolicitacaoNumerarioPersistenceMapper {
    private final UnidadeOperacionalPersistenceMapper unidadeMapper;
    private final UsuarioPersistenceMapper usuarioMapper;

    public SolicitacaoNumerario toDomain(SolicitacaoAbastecimentoEntity e) {
        if (e==null) return null;
        return SolicitacaoNumerario.reconstituir(e.getId(),e.getTipoOperacao(),e.getAgencia().getId(),
                unidadeMapper.toDomain(e.getOrigem()),unidadeMapper.toDomain(e.getDestino()),
                e.getValor(),e.getMotivo(),e.getDataDesejada(),e.getStatus(),
                usuarioMapper.toDomain(e.getSolicitante()),usuarioMapper.toDomain(e.getDecisor()),
                e.getJustificativaDecisao(),e.getDataCriacao(),e.getDataDecisao(),
                usuarioMapper.toDomain(e.getCanceladoPor()),e.getJustificativaCancelamento(),
                e.getDataCancelamento(),e.getDataAtendimento(),e.getVersao());
    }
}
