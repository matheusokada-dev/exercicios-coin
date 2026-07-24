package br.com.gestaonumerario.api.port.output;

import br.com.gestaonumerario.api.core.domain.model.Agencia;
import br.com.gestaonumerario.api.core.domain.model.FiltroAgencia;

import java.util.List;
import java.util.Optional;
import br.com.gestaonumerario.api.core.domain.model.Pagina;

public interface AgenciaOutputPort {

    Optional<Agencia> buscarPorId(Long id);

    Optional<Agencia> buscarPorIdParaAtualizacao(Long id);

    boolean existePorCodigo(String codigo);

    List<Agencia> buscarTodos();

    Pagina<Agencia> buscar(FiltroAgencia filtro);

    Agencia salvar(Agencia agencia);
}


