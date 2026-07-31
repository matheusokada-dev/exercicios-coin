package br.com.gestaonumerario.api.adapter.output.persistence;

import br.com.gestaonumerario.api.adapter.output.mapper.MovimentacaoPersistenceMapper;
import br.com.gestaonumerario.api.adapter.output.repository.AgenciaJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.MovimentacaoJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.SolicitacaoAbastecimentoJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.UsuarioJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.entity.AgenciaEntity;
import br.com.gestaonumerario.api.adapter.output.repository.entity.SolicitacaoAbastecimentoEntity;
import br.com.gestaonumerario.api.adapter.output.repository.entity.UsuarioEntity;
import br.com.gestaonumerario.api.core.domain.model.FiltroMovimentacao;
import br.com.gestaonumerario.api.core.domain.model.Movimentacao;
import br.com.gestaonumerario.api.core.domain.model.Pagina;
import br.com.gestaonumerario.api.core.domain.model.ResumoMovimentacaoDiaria;
import br.com.gestaonumerario.api.port.output.MovimentacaoOutputPort;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MovimentacaoPersistenceAdapter implements MovimentacaoOutputPort {

    private final MovimentacaoJpaRepository repository;
    private final AgenciaJpaRepository agenciaRepository;
    private final SolicitacaoAbastecimentoJpaRepository solicitacaoRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final MovimentacaoPersistenceMapper mapper;

    @Override
    public boolean existePorIdempotencyKey(String idempotencyKey) {
        return idempotencyKey != null && repository.existsByIdempotencyKey(idempotencyKey);
    }

    @Override
    public Pagina<Movimentacao> buscar(FiltroMovimentacao filtro) {
        var page = repository.buscar(
                filtro.agenciaId(),
                filtro.tipo(),
                filtro.dataInicio() == null
                        ? null
                        : filtro.dataInicio()
                                .atStartOfDay(ZoneOffset.UTC)
                                .toInstant(),
                filtro.dataFim() == null
                        ? null
                        : filtro.dataFim()
                                .plusDays(1)
                                .atStartOfDay(ZoneOffset.UTC)
                                .toInstant(),
                PageRequest.of(
                        filtro.pagina(),
                        filtro.tamanho(),
                        Sort.by(
                                Sort.Direction.DESC,
                                "dataMovimento"
                        )
                )
        );

        return new Pagina<>(
                page.getContent()
                        .stream()
                        .map(mapper::toDomain)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    public ResumoMovimentacaoDiaria resumirDiaPorAgencia(Long agenciaId, java.time.LocalDate dataReferencia) {
        var inicio = dataReferencia.atStartOfDay(ZoneOffset.UTC)
                .toInstant();
        var fimExclusivo = dataReferencia.plusDays(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        return new ResumoMovimentacaoDiaria(
                repository.somarValorPorAgenciaEDirecaoNoPeriodo(
                        agenciaId,
                        true,
                        inicio,
                        fimExclusivo
                ),
                repository.somarValorPorAgenciaEDirecaoNoPeriodo(
                        agenciaId,
                        false,
                        inicio,
                        fimExclusivo
                )
        );
    }

    @Override
    public Movimentacao salvar(Movimentacao movimentacao) {
        AgenciaEntity agencia = agenciaRepository.getReferenceById(
                movimentacao.getAgencia()
                        .getId()
        );
        SolicitacaoAbastecimentoEntity solicitacao = movimentacao.getSolicitacao() == null
                ? null
                : solicitacaoRepository.getReferenceById(
                        movimentacao.getSolicitacao()
                                .getId()
                );
        UsuarioEntity usuario = usuarioRepository.getReferenceById(
                movimentacao.getUsuario()
                        .getId()
        );

        return mapper.toDomain(
                repository.save(
                        mapper.toEntity(
                                movimentacao,
                                agencia,
                                solicitacao,
                                usuario
                        )
                )
        );
    }
}
