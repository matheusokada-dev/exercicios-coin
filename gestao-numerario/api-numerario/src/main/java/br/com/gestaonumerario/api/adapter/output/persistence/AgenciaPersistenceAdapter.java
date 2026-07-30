package br.com.gestaonumerario.api.adapter.output.persistence;

import br.com.gestaonumerario.api.adapter.output.mapper.AgenciaPersistenceMapper;
import br.com.gestaonumerario.api.adapter.output.repository.AgenciaJpaRepository;
import br.com.gestaonumerario.api.adapter.output.repository.entity.AgenciaEntity;
import br.com.gestaonumerario.api.core.domain.model.Agencia;
import br.com.gestaonumerario.api.core.domain.model.FiltroAgencia;
import br.com.gestaonumerario.api.core.domain.model.Pagina;
import br.com.gestaonumerario.api.core.exception.AgenciaNaoEncontradaException;
import br.com.gestaonumerario.api.port.output.AgenciaOutputPort;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AgenciaPersistenceAdapter implements AgenciaOutputPort {

    private final AgenciaJpaRepository repository;
    private final AgenciaPersistenceMapper mapper;

    @Override
    public Optional<Agencia> buscarPorId(Long id) {

        return repository.findById(id)
                .map(mapper::toDomain);

    }

    @Override
    public Optional<Agencia> buscarPorIdParaAtualizacao(Long id) {
        return repository.buscarPorIdParaAtualizacao(id)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existePorCodigo(String codigo) {
        return repository.existsByCodigo(codigo);
    }

    @Override
    public List<Agencia> buscarTodos() {
        return repository.findAll()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Pagina<Agencia> buscar(FiltroAgencia filtro) {
        Sort.Direction direcao = filtro.direcao()
                .name()
                .equals("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        var page = repository.buscar(
                filtro.busca(),
                filtro.ativo(),
                filtro.alerta(),
                PageRequest.of(
                        filtro.pagina(),
                        filtro.tamanho(),
                        Sort.by(
                                direcao,
                                filtro.ordenarPor()
                                        .getPropriedadeJpa()
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
    public Agencia salvar(Agencia agencia) {
        if (agencia.getId() == null) {
            return mapper.toDomain(repository.save(mapper.toEntity(agencia)));
        }

        AgenciaEntity entity = repository.findById(agencia.getId())
                .orElseThrow(AgenciaNaoEncontradaException::new);
        entity.atualizarDados(
                agencia.getNome(),
                agencia.getCidade(),
                agencia.getSaldoAtual(),
                agencia.getLimiteMinimo(),
                agencia.isAtivo()
        );
        return mapper.toDomain(repository.save(entity));
    }
}
