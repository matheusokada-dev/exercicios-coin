package br.com.gestaonumerario.api.adapter.output.mapper;

import br.com.gestaonumerario.api.adapter.output.repository.entity.AgenciaEntity;
import br.com.gestaonumerario.api.adapter.output.repository.entity.MovimentacaoEntity;
import br.com.gestaonumerario.api.adapter.output.repository.entity.SolicitacaoAbastecimentoEntity;
import br.com.gestaonumerario.api.adapter.output.repository.entity.UsuarioEntity;
import br.com.gestaonumerario.api.core.domain.model.Movimentacao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MovimentacaoPersistenceMapper {

    private final AgenciaPersistenceMapper agenciaMapper;
    private final SolicitacaoAbastecimentoPersistenceMapper solicitacaoMapper;
    private final UsuarioPersistenceMapper usuarioMapper;

    public Movimentacao toDomain(MovimentacaoEntity entity) {
        if (entity == null) {
            return null;
        }

        return Movimentacao.reconstituir(
                entity.getId(), agenciaMapper.toDomain(entity.getAgencia()),
                solicitacaoMapper.toDomain(entity.getSolicitacao()), entity.getTipo(), entity.isEntrada(),
                entity.getValor(),
                entity.getSaldoAnterior(), entity.getSaldoPosterior(), entity.getDescricao(),
                entity.getDataMovimento(), usuarioMapper.toDomain(entity.getUsuario()), entity.getIdempotencyKey());
    }

    public MovimentacaoEntity toEntity(
            Movimentacao domain,
            AgenciaEntity agencia,
            SolicitacaoAbastecimentoEntity solicitacao,
            UsuarioEntity usuario
    ) {
        if (domain == null) {
            return null;
        }

        return new MovimentacaoEntity(
                domain.getId(), agencia, solicitacao, domain.getTipo(), domain.isEntrada(), domain.getValor(),
                domain.getSaldoAnterior(), domain.getSaldoPosterior(), domain.getDescricao(),
                domain.getDataMovimento(), usuario, domain.getIdempotencyKey());
    }
}

