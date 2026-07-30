package br.com.gestaonumerario.api.port.input;

import br.com.gestaonumerario.api.core.domain.model.SessaoAutenticacao;
import br.com.gestaonumerario.api.core.domain.model.command.AutenticarCommand;

public interface AutenticarInputPort {
    SessaoAutenticacao autenticar(AutenticarCommand command);
}
