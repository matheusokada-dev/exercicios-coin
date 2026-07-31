package br.com.gestaonumerario.api.adapter.output.mapper;

import br.com.gestaonumerario.api.adapter.output.repository.entity.OperacaoNumerarioEntity;
import br.com.gestaonumerario.api.core.domain.model.OperacaoNumerario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OperacaoNumerarioPersistenceMapper {
    private final SolicitacaoNumerarioPersistenceMapper solicitacaoMapper;
    private final UnidadeOperacionalVirtualMapper unidadeMapper;
    private final UsuarioPersistenceMapper usuarioMapper;

    public OperacaoNumerario toDomain(OperacaoNumerarioEntity e) {
        if (e == null)
            return null;
        var solicitacao = solicitacaoMapper.toDomain(e.getSolicitacao());
        return OperacaoNumerario.reconstituir(
                e.getId(),
                solicitacao,
                unidadeMapper.agencia(e.getOrigem()),
                unidadeMapper.agencia(e.getDestino()),
                e.getStatus(),
                e.getValorProgramado(),
                e.getValorExpedido(),
                e.getValorRecebido(),
                e.getValorDivergencia(),
                usuarioMapper.toDomain(e.getProgramadoPor()),
                usuarioMapper.toDomain(e.getExpedidoPor()),
                usuarioMapper.toDomain(e.getRecebidoPor()),
                usuarioMapper.toDomain(e.getConciliadoPor()),
                e.getDataProgramacao(),
                e.getDataExpedicao(),
                e.getDataRecebimento(),
                e.getDataConciliacao(),
                e.getJustificativaDivergencia(),
                e.getDescricaoOcorrencia(),
                e.getIdempotencyKey(),
                e.getVersao()
        );
    }
}
