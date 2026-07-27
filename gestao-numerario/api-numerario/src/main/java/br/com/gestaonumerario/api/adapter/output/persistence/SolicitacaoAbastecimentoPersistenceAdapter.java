package br.com.gestaonumerario.api.adapter.output.persistence;

import br.com.gestaonumerario.api.adapter.output.repository.entity.AgenciaEntity;
import br.com.gestaonumerario.api.adapter.output.repository.entity.UsuarioEntity;
import br.com.gestaonumerario.api.adapter.output.mapper.SolicitacaoAbastecimentoPersistenceMapper;
import br.com.gestaonumerario.api.adapter.output.repository.AgenciaJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.SolicitacaoAbastecimentoJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.UsuarioJpaRepository;
import br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacao;
import br.com.gestaonumerario.api.core.domain.enums.StatusSolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.model.SolicitacaoAbastecimento;
import br.com.gestaonumerario.api.port.output.SolicitacaoAbastecimentoOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Optional;
import java.time.ZoneOffset;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import br.com.gestaonumerario.api.core.domain.model.FiltroSolicitacao;
import br.com.gestaonumerario.api.core.domain.model.Pagina;

@Component
@RequiredArgsConstructor
public class SolicitacaoAbastecimentoPersistenceAdapter implements SolicitacaoAbastecimentoOutputPort {

    private static final EnumSet<StatusSolicitacaoNumerario> STATUS_ABERTOS =
            EnumSet.of(StatusSolicitacaoNumerario.PENDENTE, StatusSolicitacaoNumerario.APROVADA,
                    StatusSolicitacaoNumerario.EM_EXECUCAO, StatusSolicitacaoNumerario.COM_DIVERGENCIA);

    private final SolicitacaoAbastecimentoJpaRepository repository;
    private final AgenciaJpaRepository agenciaRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final SolicitacaoAbastecimentoPersistenceMapper mapper;

    @Override
    public Optional<SolicitacaoAbastecimento> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<SolicitacaoAbastecimento> buscarAprovadaPorAgenciaId(Long agenciaId) {
        return repository.findByAgencia_IdAndStatus(
                agenciaId, StatusSolicitacaoNumerario.APROVADA).map(mapper::toDomain);
    }

    @Override
    public Pagina<SolicitacaoAbastecimento> buscar(FiltroSolicitacao filtro) {
        var page = repository.buscar(filtro.agenciaId(), converter(filtro.status()),
                filtro.dataInicio() == null ? null : filtro.dataInicio().atStartOfDay(ZoneOffset.UTC).toInstant(),
                filtro.dataFim() == null ? null : filtro.dataFim().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant(),
                PageRequest.of(filtro.pagina(), filtro.tamanho(), Sort.by(Sort.Direction.DESC, "dataCriacao")));
        return new Pagina<>(page.getContent().stream().map(mapper::toDomain).toList(), page.getNumber(),
                page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private static StatusSolicitacaoNumerario converter(StatusSolicitacao status) {
        if (status == null) return null;
        return switch (status) {
            case PENDENTE -> StatusSolicitacaoNumerario.PENDENTE;
            case APROVADA -> StatusSolicitacaoNumerario.APROVADA;
            case REJEITADA -> StatusSolicitacaoNumerario.REJEITADA;
            case ATENDIDA -> StatusSolicitacaoNumerario.CONCLUIDA;
        };
    }

    @Override
    public boolean existeSolicitacaoAbertaParaAgencia(Long agenciaId) {
        return repository.existsByAgencia_IdAndStatusIn(agenciaId, STATUS_ABERTOS);
    }

    @Override
    public SolicitacaoAbastecimento salvar(SolicitacaoAbastecimento solicitacao) {
        AgenciaEntity agencia = agenciaRepository.getReferenceById(solicitacao.getAgencia().getId());
        UsuarioEntity solicitante = usuarioRepository.getReferenceById(solicitacao.getSolicitante().getId());
        UsuarioEntity decisor = solicitacao.getDecisor() == null
                ? null
                : usuarioRepository.getReferenceById(solicitacao.getDecisor().getId());

        return mapper.toDomain(repository.save(mapper.toEntity(solicitacao, agencia, solicitante, decisor)));
    }
}


