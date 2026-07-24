package br.com.gestaonumerario.api.core.usecase.autenticacao;

import br.com.gestaonumerario.api.core.exception.CredenciaisInvalidasException;
import br.com.gestaonumerario.api.port.input.AutenticarInputPort;
import br.com.gestaonumerario.api.core.domain.model.command.AutenticarCommand;
import br.com.gestaonumerario.api.port.output.CodificadorSenhaOutputPort;
import br.com.gestaonumerario.api.port.output.RelogioOutputPort;
import br.com.gestaonumerario.api.port.output.TokenJwtOutputPort;
import br.com.gestaonumerario.api.port.output.UsuarioOutputPort;

import java.time.Duration;

public class AutenticacaoUseCase implements AutenticarInputPort {
    private final UsuarioOutputPort usuarioPort;
    private final CodificadorSenhaOutputPort codificadorSenhaPort;
    private final TokenJwtOutputPort tokenJwtPort;
    private final RelogioOutputPort relogioPort;
    private final int limiteTentativas;
    private final Duration duracaoBloqueio;

    public AutenticacaoUseCase(UsuarioOutputPort usuarioPort,
                               CodificadorSenhaOutputPort codificadorSenhaPort,
                               TokenJwtOutputPort tokenJwtPort,
                               RelogioOutputPort relogioPort,
                               int limiteTentativas,
                               Duration duracaoBloqueio) {
        this.usuarioPort = usuarioPort;
        this.codificadorSenhaPort = codificadorSenhaPort;
        this.tokenJwtPort = tokenJwtPort;
        this.relogioPort = relogioPort;
        this.limiteTentativas = limiteTentativas;
        this.duracaoBloqueio = duracaoBloqueio;
    }

    @Override
    public br.com.gestaonumerario.api.core.domain.model.TokenAcesso autenticar(AutenticarCommand command) {
        if (command == null || command.login() == null || command.senha() == null) {
            throw new CredenciaisInvalidasException();
        }
        var usuario = usuarioPort.buscarPorLogin(command.login().trim())
                .orElseThrow(CredenciaisInvalidasException::new);

        var agora = relogioPort.agora();
        if (!usuario.isAtivo()) {
            throw new CredenciaisInvalidasException();
        }

        if (usuario.estaBloqueado(agora)) {
            throw new CredenciaisInvalidasException(0, usuario.getBloqueadoAte());
        }

        if (!codificadorSenhaPort.confere(command.senha(), usuario.getSenhaHash())) {
            usuario.registrarFalhaLogin(agora, limiteTentativas, duracaoBloqueio);
            usuarioPort.salvar(usuario);
            throw new CredenciaisInvalidasException(
                    usuario.tentativasLoginRestantes(limiteTentativas),
                    usuario.getBloqueadoAte()
            );
        }

        if (usuario.possuiFalhasLogin()) {
            usuario.limparTentativasLogin();
            usuarioPort.salvar(usuario);
        }

        return tokenJwtPort.gerar(usuario);
    }
}



