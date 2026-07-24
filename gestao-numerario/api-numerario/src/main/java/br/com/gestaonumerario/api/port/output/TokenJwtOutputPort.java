package br.com.gestaonumerario.api.port.output;

import br.com.gestaonumerario.api.core.domain.model.TokenAcesso;
import br.com.gestaonumerario.api.core.domain.model.Usuario;

public interface TokenJwtOutputPort {
    TokenAcesso gerar(Usuario usuario);
}

