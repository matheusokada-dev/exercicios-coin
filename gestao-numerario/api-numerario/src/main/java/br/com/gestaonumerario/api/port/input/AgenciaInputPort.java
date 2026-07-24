package br.com.gestaonumerario.api.port.input;

import br.com.gestaonumerario.api.core.domain.model.Agencia;
import br.com.gestaonumerario.api.core.domain.model.DetalheAgencia;
import br.com.gestaonumerario.api.core.domain.model.FiltroAgencia;
import br.com.gestaonumerario.api.core.domain.model.Pagina;
import br.com.gestaonumerario.api.core.domain.model.command.AtualizarAgenciaCommand;
import br.com.gestaonumerario.api.core.domain.model.command.CriarAgenciaCommand;

public interface AgenciaInputPort {
    Agencia criar(CriarAgenciaCommand command);
    Agencia atualizar(AtualizarAgenciaCommand command);
    Agencia buscarPorId(Long id);
    Pagina<Agencia> listar(FiltroAgencia filtro);
    DetalheAgencia detalhar(Long agenciaId);
    void desativar(Long agenciaId);
}


