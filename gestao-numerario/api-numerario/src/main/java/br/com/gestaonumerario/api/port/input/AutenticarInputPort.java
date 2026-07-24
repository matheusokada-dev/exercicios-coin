package br.com.gestaonumerario.api.port.input;

import br.com.gestaonumerario.api.core.domain.model.TokenAcesso;
import br.com.gestaonumerario.api.core.domain.model.command.AutenticarCommand;

public interface AutenticarInputPort {
    TokenAcesso autenticar(AutenticarCommand command);
}


