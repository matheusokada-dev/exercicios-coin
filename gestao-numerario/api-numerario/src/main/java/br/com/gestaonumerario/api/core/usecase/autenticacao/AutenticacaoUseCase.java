package br.com.gestaonumerario.api.core.usecase.autenticacao;

import br.com.gestaonumerario.api.core.exception.CredenciaisInvalidasException;
import br.com.gestaonumerario.api.port.input.AutenticarInputPort;
import br.com.gestaonumerario.api.core.domain.model.command.AutenticarCommand;
import br.com.gestaonumerario.api.port.output.CodificadorSenhaOutputPort;
import br.com.gestaonumerario.api.port.output.RelogioOutputPort;
import br.com.gestaonumerario.api.port.output.TokenJwtOutputPort;
import br.com.gestaonumerario.api.port.output.UsuarioOutputPort;
import br.com.gestaonumerario.api.port.output.RefreshTokenOutputPort;
import br.com.gestaonumerario.api.core.domain.model.SessaoAutenticacao;

import java.time.Duration;

public class AutenticacaoUseCase implements AutenticarInputPort {
    private final UsuarioOutputPort usuarioPort;
    private final CodificadorSenhaOutputPort codificadorSenhaPort;
    private final TokenJwtOutputPort tokenJwtPort;
    private final RelogioOutputPort relogioPort;
    private final int limiteTentativas;
    private final Duration duracaoBloqueio;
    private final Duration duracaoRefreshToken;
    private final RefreshTokenOutputPort refreshTokenPort;

    public AutenticacaoUseCase(UsuarioOutputPort usuarioPort,
                               CodificadorSenhaOutputPort codificadorSenhaPort,
                               TokenJwtOutputPort tokenJwtPort,
                               RelogioOutputPort relogioPort,
                               int limiteTentativas,
                               Duration duracaoBloqueio,
                               Duration duracaoRefreshToken,
                               RefreshTokenOutputPort refreshTokenPort) {
        this.usuarioPort = usuarioPort;
        this.codificadorSenhaPort = codificadorSenhaPort;
        this.tokenJwtPort = tokenJwtPort;
        this.relogioPort = relogioPort;
        this.limiteTentativas = limiteTentativas;
        this.duracaoBloqueio = duracaoBloqueio;
        this.duracaoRefreshToken = duracaoRefreshToken;
        this.refreshTokenPort = refreshTokenPort;
    }

    @Override
    public SessaoAutenticacao autenticar(AutenticarCommand command) {
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
            if (usuario.estaBloqueado(agora)) {
                refreshTokenPort.revogarTodos(usuario.getId());
            }
            throw new CredenciaisInvalidasException(
                    usuario.tentativasLoginRestantes(limiteTentativas),
                    usuario.getBloqueadoAte()
            );
        }

        if (usuario.possuiFalhasLogin()) {
            usuario.limparTentativasLogin();
            usuarioPort.salvar(usuario);
        }

        refreshTokenPort.revogarTodos(usuario.getId());
        var acesso = tokenJwtPort.gerar(usuario);
        var refresh = refreshTokenPort.emitir(usuario.getId(), agora, duracaoRefreshToken);
        return sessao(usuario, acesso, refresh.valor(), refresh.expiraEm());
    }

    @Override
    public SessaoAutenticacao renovar(String refreshToken) {
        var agora = relogioPort.agora();
        var refresh = refreshTokenPort.rotacionar(refreshToken, agora, duracaoRefreshToken);
        var usuario = usuarioPort.buscarPorId(refresh.usuarioId())
                .filter(br.com.gestaonumerario.api.core.domain.model.Usuario::isAtivo)
                .orElseThrow(CredenciaisInvalidasException::new);
        var acesso = tokenJwtPort.gerar(usuario);
        return sessao(usuario, acesso, refresh.valor(), refresh.expiraEm());
    }

    @Override
    public void encerrar(String refreshToken) {
        refreshTokenPort.revogar(refreshToken);
    }

    private SessaoAutenticacao sessao(
            br.com.gestaonumerario.api.core.domain.model.Usuario usuario,
            br.com.gestaonumerario.api.core.domain.model.TokenAcesso acesso,
            String refreshToken,
            java.time.Instant refreshExpiraEm) {
        return new SessaoAutenticacao(
                acesso.valor(), acesso.expiraEm(), refreshToken, refreshExpiraEm,
                usuario.getId(), usuario.getNome(), usuario.getPerfil().name()
        );
    }
}

