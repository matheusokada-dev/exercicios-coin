package br.com.gestaonumerario.api.core.usecase.movimentacao;

import br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao;
import br.com.gestaonumerario.api.core.domain.model.Agencia;
import br.com.gestaonumerario.api.core.domain.model.FiltroMovimentacao;
import br.com.gestaonumerario.api.core.domain.model.Movimentacao;
import br.com.gestaonumerario.api.core.domain.model.Usuario;
import br.com.gestaonumerario.api.core.exception.AgenciaNaoEncontradaException;
import br.com.gestaonumerario.api.core.exception.CampoObrigatorioException;
import br.com.gestaonumerario.api.core.exception.IdempotencyKeyDuplicadaException;
import br.com.gestaonumerario.api.core.exception.TipoMovimentacaoNaoPermitidoException;
import br.com.gestaonumerario.api.core.exception.UsuarioNaoEncontradoException;
import br.com.gestaonumerario.api.core.exception.RegraOperacaoNumerarioException;
import br.com.gestaonumerario.api.port.input.MovimentacaoInputPort;
import br.com.gestaonumerario.api.core.domain.model.command.RegistrarMovimentacaoCommand;
import br.com.gestaonumerario.api.port.output.AgenciaOutputPort;
import br.com.gestaonumerario.api.port.output.MovimentacaoOutputPort;
import br.com.gestaonumerario.api.core.domain.model.Pagina;
import br.com.gestaonumerario.api.port.output.RelogioOutputPort;
import br.com.gestaonumerario.api.port.output.TransacaoOutputPort;
import br.com.gestaonumerario.api.port.output.UsuarioOutputPort;
public class MovimentacaoUseCase implements MovimentacaoInputPort {

    private final AgenciaOutputPort agenciaPort;
    private final UsuarioOutputPort usuarioPort;
    private final MovimentacaoOutputPort movimentacaoPort;
    private final RelogioOutputPort relogioPort;
    private final TransacaoOutputPort transacaoPort;

    public MovimentacaoUseCase(AgenciaOutputPort agenciaPort,
                               UsuarioOutputPort usuarioPort,
                               MovimentacaoOutputPort movimentacaoPort,
                               RelogioOutputPort relogioPort,
                               TransacaoOutputPort transacaoPort) {
        this.agenciaPort = agenciaPort;
        this.usuarioPort = usuarioPort;
        this.movimentacaoPort = movimentacaoPort;
        this.relogioPort = relogioPort;
        this.transacaoPort = transacaoPort;
    }

    @Override
    public Movimentacao registrar(RegistrarMovimentacaoCommand command) {
        if (command == null) throw new CampoObrigatorioException("comando");
        if (command.agenciaId() == null) throw new CampoObrigatorioException("agenciaId");
        if (command.usuarioId() == null) throw new CampoObrigatorioException("usuarioId");
        if (command.tipo() == null) throw new CampoObrigatorioException("tipo");
        if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
            throw new CampoObrigatorioException("idempotencyKey");
        }

        if (command.tipo() == TipoMovimentacao.ABASTECIMENTO) {
            throw new TipoMovimentacaoNaoPermitidoException();
        }

        return transacaoPort.executar(() -> {
            Agencia agencia = agenciaPort.buscarPorIdParaAtualizacao(command.agenciaId())
                    .orElseThrow(AgenciaNaoEncontradaException::new);
            Usuario usuario = usuarioPort.buscarPorId(command.usuarioId())
                    .orElseThrow(UsuarioNaoEncontradoException::new);
            agencia.exigirAtiva();
            exigirUsuarioAtivo(usuario);

            String idempotencyKey = command.idempotencyKey().trim();
            if (movimentacaoPort.existePorIdempotencyKey(idempotencyKey)) {
                throw new IdempotencyKeyDuplicadaException();
            }

            boolean entrada = resolverEntrada(command.tipo(), command.entradaAjuste());
            Movimentacao movimentacao = Movimentacao.criar(agencia, null, command.tipo(), entrada,
                    command.valor(), command.descricao(), relogioPort.agora(), usuario, idempotencyKey);

            if (entrada) {
                agencia.abastecer(command.valor());
            } else {
                agencia.retirar(command.valor());
            }

            agenciaPort.salvar(agencia);
            return movimentacaoPort.salvar(movimentacao);
        });
    }

    @Override
    public Pagina<Movimentacao> consultar(FiltroMovimentacao filtro) {
        if (filtro == null) {
            throw new CampoObrigatorioException("filtro");
        }
        return movimentacaoPort.buscar(filtro);
    }

    private static boolean resolverEntrada(TipoMovimentacao tipo, Boolean entradaAjuste) {
        if (tipo.exigeDirecaoInformada()) {
            if (entradaAjuste == null) {
                throw new CampoObrigatorioException("entradaAjuste");
            }
            return entradaAjuste;
        }
        return tipo.getEntradaPadrao();
    }

    private static void exigirUsuarioAtivo(Usuario usuario) {
        if (!usuario.isAtivo()) {
            throw new RegraOperacaoNumerarioException(
                    "O usuário está inativo e não pode registrar movimentações.");
        }
    }
}


