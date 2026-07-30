package br.com.gestaonumerario.api.core.usecase.solicitacao;

import br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao;
import br.com.gestaonumerario.api.core.domain.model.Agencia;
import br.com.gestaonumerario.api.core.domain.model.FiltroSolicitacao;
import br.com.gestaonumerario.api.core.domain.model.Movimentacao;
import br.com.gestaonumerario.api.core.domain.model.Pagina;
import br.com.gestaonumerario.api.core.domain.model.SolicitacaoAbastecimento;
import br.com.gestaonumerario.api.core.domain.model.Usuario;
import br.com.gestaonumerario.api.core.domain.model.command.AprovarSolicitacaoCommand;
import br.com.gestaonumerario.api.core.domain.model.command.AtenderSolicitacaoCommand;
import br.com.gestaonumerario.api.core.domain.model.command.RejeitarSolicitacaoCommand;
import br.com.gestaonumerario.api.core.domain.model.command.SolicitarAbastecimentoCommand;
import br.com.gestaonumerario.api.core.exception.AgenciaNaoEncontradaException;
import br.com.gestaonumerario.api.core.exception.CampoObrigatorioException;
import br.com.gestaonumerario.api.core.exception.IdempotencyKeyDuplicadaException;
import br.com.gestaonumerario.api.core.exception.RegraOperacaoNumerarioException;
import br.com.gestaonumerario.api.core.exception.SolicitacaoAbertaDuplicadaException;
import br.com.gestaonumerario.api.core.exception.SolicitacaoNaoEncontradaException;
import br.com.gestaonumerario.api.core.exception.UsuarioNaoEncontradoException;
import br.com.gestaonumerario.api.port.input.SolicitacaoInputPort;
import br.com.gestaonumerario.api.port.output.AgenciaOutputPort;
import br.com.gestaonumerario.api.port.output.MovimentacaoOutputPort;
import br.com.gestaonumerario.api.port.output.RelogioOutputPort;
import br.com.gestaonumerario.api.port.output.SolicitacaoAbastecimentoOutputPort;
import br.com.gestaonumerario.api.port.output.TransacaoOutputPort;
import br.com.gestaonumerario.api.port.output.UsuarioOutputPort;
import java.time.Instant;

public class SolicitacaoUseCase implements SolicitacaoInputPort {

    private final AgenciaOutputPort agenciaPort;
    private final UsuarioOutputPort usuarioPort;
    private final SolicitacaoAbastecimentoOutputPort solicitacaoPort;
    private final MovimentacaoOutputPort movimentacaoPort;
    private final RelogioOutputPort relogioPort;
    private final TransacaoOutputPort transacaoPort;

    public SolicitacaoUseCase(
            AgenciaOutputPort agenciaPort,
            UsuarioOutputPort usuarioPort,
            SolicitacaoAbastecimentoOutputPort solicitacaoPort,
            MovimentacaoOutputPort movimentacaoPort,
            RelogioOutputPort relogioPort,
            TransacaoOutputPort transacaoPort) {
        this.agenciaPort = agenciaPort;
        this.usuarioPort = usuarioPort;
        this.solicitacaoPort = solicitacaoPort;
        this.movimentacaoPort = movimentacaoPort;
        this.relogioPort = relogioPort;
        this.transacaoPort = transacaoPort;
    }

    @Override
    public SolicitacaoAbastecimento solicitar(SolicitarAbastecimentoCommand command) {
        validar(
                command,
                command == null ? null : command.agenciaId(),
                command == null ? null : command.solicitanteId()
        );

        return transacaoPort.executar(() -> {
            Agencia agencia = buscarAgencia(command.agenciaId());
            Usuario solicitante = buscarUsuario(command.solicitanteId());
            agencia.exigirAtiva();

            if (solicitacaoPort.existeSolicitacaoAbertaParaAgencia(command.agenciaId())) {
                throw new SolicitacaoAbertaDuplicadaException();
            }

            SolicitacaoAbastecimento solicitacao = SolicitacaoAbastecimento.criar(
                    agencia,
                    command.valor(),
                    command.motivo(),
                    command.dataDesejada(),
                    solicitante,
                    relogioPort.hoje(),
                    relogioPort.agora()
            );

            return solicitacaoPort.salvar(solicitacao);
        });
    }

    @Override
    public SolicitacaoAbastecimento aprovar(AprovarSolicitacaoCommand command) {
        validar(
                command,
                command == null ? null : command.solicitacaoId(),
                command == null ? null : command.decisorId()
        );

        return transacaoPort.executar(() -> {
            SolicitacaoAbastecimento solicitacao = buscarSolicitacao(command.solicitacaoId());

            Usuario decisor = buscarUsuario(command.decisorId());

            solicitacao.aprovar(
                    decisor,
                    command.justificativaDecisao(),
                    command.justificativaEspecial(),
                    relogioPort.agora()
            );

            return solicitacaoPort.salvar(solicitacao);
        });
    }

    @Override
    public SolicitacaoAbastecimento rejeitar(RejeitarSolicitacaoCommand command) {
        validar(
                command,
                command == null ? null : command.solicitacaoId(),
                command == null ? null : command.decisorId()
        );

        return transacaoPort.executar(() -> {
            SolicitacaoAbastecimento solicitacao = buscarSolicitacao(command.solicitacaoId());

            Usuario decisor = buscarUsuario(command.decisorId());

            solicitacao.rejeitar(
                    decisor,
                    command.justificativaDecisao(),
                    relogioPort.agora()
            );

            return solicitacaoPort.salvar(solicitacao);
        });
    }

    @Override
    public SolicitacaoAbastecimento atender(AtenderSolicitacaoCommand command) {
        validar(
                command,
                command == null ? null : command.solicitacaoId(),
                command == null ? null : command.usuarioId()
        );

        return transacaoPort.executar(() -> {
            SolicitacaoAbastecimento solicitacao = buscarSolicitacao(command.solicitacaoId());

            Usuario usuario = buscarUsuario(command.usuarioId());

            Agencia agencia = agenciaPort.buscarPorIdParaAtualizacao(
                    solicitacao.getAgencia()
                            .getId()
            )
                    .orElseThrow(AgenciaNaoEncontradaException::new);

            String idempotencyKey = textoObrigatorio(command.idempotencyKey());

            if (movimentacaoPort.existePorIdempotencyKey(idempotencyKey)) {
                throw new IdempotencyKeyDuplicadaException();
            }

            Instant agora = relogioPort.agora();

            Movimentacao movimentacao = Movimentacao.criar(
                    agencia,
                    solicitacao,
                    TipoMovimentacao.ABASTECIMENTO,
                    solicitacao.getValor(),
                    "Atendimento da solicitação " + solicitacao.getId(),
                    agora,
                    usuario,
                    idempotencyKey
            );

            agencia.abastecer(solicitacao.getValor());
            solicitacao.atender(agora);

            agenciaPort.salvar(agencia);
            solicitacaoPort.salvar(solicitacao);
            movimentacaoPort.salvar(movimentacao);

            return solicitacao;
        });
    }

    @Override
    public Pagina<SolicitacaoAbastecimento> consultar(FiltroSolicitacao filtro) {
        if (filtro == null || filtro.pagina() < 0 || filtro.tamanho() < 1) {
            throw new CampoObrigatorioException();
        }
        return solicitacaoPort.buscar(filtro);
    }

    private Agencia buscarAgencia(Long id) {
        return agenciaPort.buscarPorId(id)
                .orElseThrow(AgenciaNaoEncontradaException::new);
    }

    private Usuario buscarUsuario(Long id) {
        Usuario usuario = usuarioPort.buscarPorId(id)
                .orElseThrow(UsuarioNaoEncontradoException::new);
        if (!usuario.isAtivo()) {
            throw new RegraOperacaoNumerarioException();
        }
        return usuario;
    }

    private SolicitacaoAbastecimento buscarSolicitacao(Long id) {
        return solicitacaoPort.buscarPorId(id)
                .orElseThrow(SolicitacaoNaoEncontradaException::new);
    }

    private static void validar(Object command, Long... ids) {
        if (command == null) {
            throw new CampoObrigatorioException();
        }

        for (Long id : ids) {
            if (id == null) {
                throw new CampoObrigatorioException();
            }
        }
    }

    private static String textoObrigatorio(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new CampoObrigatorioException();
        }
        return valor.trim();
    }
}
