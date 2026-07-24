package br.com.gestaonumerario.api.core.usecase.usuario;

import br.com.gestaonumerario.api.core.domain.model.Usuario;
import br.com.gestaonumerario.api.core.exception.CampoObrigatorioException;
import br.com.gestaonumerario.api.core.exception.LoginDuplicadoException;
import br.com.gestaonumerario.api.core.exception.UsuarioNaoEncontradoException;
import br.com.gestaonumerario.api.port.input.UsuarioInputPort;
import br.com.gestaonumerario.api.core.domain.model.command.CriarUsuarioCommand;
import br.com.gestaonumerario.api.port.output.CodificadorSenhaOutputPort;
import br.com.gestaonumerario.api.port.output.RelogioOutputPort;
import br.com.gestaonumerario.api.port.output.TransacaoOutputPort;
import br.com.gestaonumerario.api.port.output.UsuarioOutputPort;
public class UsuarioUseCase implements UsuarioInputPort {

    private final UsuarioOutputPort usuarioPort;
    private final CodificadorSenhaOutputPort codificadorSenhaPort;
    private final RelogioOutputPort relogioPort;
    private final TransacaoOutputPort transacaoPort;

    public UsuarioUseCase(UsuarioOutputPort usuarioPort,
                          CodificadorSenhaOutputPort codificadorSenhaPort,
                          RelogioOutputPort relogioPort,
                          TransacaoOutputPort transacaoPort) {
        this.usuarioPort = usuarioPort;
        this.codificadorSenhaPort = codificadorSenhaPort;
        this.relogioPort = relogioPort;
        this.transacaoPort = transacaoPort;
    }

    @Override
    public Usuario criar(CriarUsuarioCommand command) {
        if (command == null) {
            throw new CampoObrigatorioException();
        }

        return transacaoPort.executar(() -> {
            String login = textoObrigatorio(command.login());
            String senha = textoObrigatorio(command.senha());

            if (usuarioPort.existePorLogin(login)) {
                throw new LoginDuplicadoException();
            }

            Usuario usuario = new Usuario(null, command.nome(), login,
                    codificadorSenhaPort.codificar(senha), command.perfil(), true, relogioPort.agora(), 0, null);
            return usuarioPort.salvar(usuario);
        });
    }

    @Override
    public Usuario buscarPorId(Long id) {
        if (id == null) {
            throw new CampoObrigatorioException();
        }
        return usuarioPort.buscarPorId(id).orElseThrow(UsuarioNaoEncontradoException::new);
    }

    private static String textoObrigatorio(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new CampoObrigatorioException();
        }
        return valor.trim();
    }
}




