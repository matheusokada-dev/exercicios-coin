package br.com.gestaonumerario.api.adapter.output.mapper;

import br.com.gestaonumerario.api.adapter.output.repository.entity.OperacaoNumerarioEntity;
import br.com.gestaonumerario.api.core.domain.model.OperacaoNumerario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component @RequiredArgsConstructor
public class OperacaoNumerarioPersistenceMapper {
    private final SolicitacaoNumerarioPersistenceMapper solicitacaoMapper;
    private final UnidadeOperacionalPersistenceMapper unidadeMapper;
    private final UsuarioPersistenceMapper usuarioMapper;

    public OperacaoNumerario toDomain(OperacaoNumerarioEntity e) {
        if(e==null) return null;
        return OperacaoNumerario.reconstituir(e.getId(),solicitacaoMapper.toDomain(e.getSolicitacao()),
                unidadeMapper.toDomain(e.getOrigem()),unidadeMapper.toDomain(e.getDestino()),e.getStatus(),
                e.getValorProgramado(),e.getValorExpedido(),e.getValorRecebido(),e.getValorDivergencia(),
                usuarioMapper.toDomain(e.getProgramadoPor()),usuarioMapper.toDomain(e.getExpedidoPor()),
                usuarioMapper.toDomain(e.getRecebidoPor()),usuarioMapper.toDomain(e.getConciliadoPor()),
                e.getDataProgramacao(),e.getDataExpedicao(),e.getDataRecebimento(),e.getDataConciliacao(),
                e.getJustificativaDivergencia(),e.getDescricaoOcorrencia(),e.getIdempotencyKey(),e.getVersao());
    }
}
