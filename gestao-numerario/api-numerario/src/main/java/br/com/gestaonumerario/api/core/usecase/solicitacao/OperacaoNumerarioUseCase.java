package br.com.gestaonumerario.api.core.usecase.solicitacao;

import br.com.gestaonumerario.api.core.domain.enums.TipoMovimentacao;
import br.com.gestaonumerario.api.core.domain.model.DetalheSolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.model.OperacaoNumerario;
import br.com.gestaonumerario.api.core.domain.model.Usuario;
import br.com.gestaonumerario.api.core.domain.model.command.ConciliarOperacaoNumerarioCommand;
import br.com.gestaonumerario.api.core.domain.model.command.ExecutarOperacaoNumerarioCommand;
import br.com.gestaonumerario.api.core.domain.model.command.ProgramarOperacaoNumerarioCommand;
import br.com.gestaonumerario.api.core.domain.model.command.ReceberOperacaoNumerarioCommand;
import br.com.gestaonumerario.api.core.domain.model.command.RegistrarOcorrenciaOperacaoCommand;
import br.com.gestaonumerario.api.core.exception.CampoObrigatorioException;
import br.com.gestaonumerario.api.core.exception.IdempotencyKeyDuplicadaException;
import br.com.gestaonumerario.api.core.exception.RegraOperacaoNumerarioException;
import br.com.gestaonumerario.api.core.exception.SolicitacaoNaoEncontradaException;
import br.com.gestaonumerario.api.core.exception.UsuarioNaoEncontradoException;
import br.com.gestaonumerario.api.port.input.OperacaoNumerarioInputPort;
import br.com.gestaonumerario.api.port.output.NumerarioOutputPort;
import br.com.gestaonumerario.api.port.output.RelogioOutputPort;
import br.com.gestaonumerario.api.port.output.TransacaoOutputPort;
import br.com.gestaonumerario.api.port.output.UsuarioOutputPort;

public class OperacaoNumerarioUseCase implements OperacaoNumerarioInputPort {
    private final NumerarioOutputPort port;
    private final UsuarioOutputPort usuarios;
    private final RelogioOutputPort relogio;
    private final TransacaoOutputPort transacao;

    public OperacaoNumerarioUseCase(
            NumerarioOutputPort port,
            UsuarioOutputPort usuarios,
            RelogioOutputPort relogio,
            TransacaoOutputPort transacao) {
        this.port = port;
        this.usuarios = usuarios;
        this.relogio = relogio;
        this.transacao = transacao;
    }

    @Override
    public OperacaoNumerario programar(ProgramarOperacaoNumerarioCommand c) {
        validar(
                c,
                c == null ? null : c.solicitacaoId(),
                c == null ? null : c.unidadeFaltanteId(),
                c == null ? null : c.usuarioId()
        );
        validarChave(c.idempotencyKey());
        return transacao.executar(() -> {
            rejeitarChaveRepetida(c.idempotencyKey());
            var detalhe = detalhe(c.solicitacaoId());
            var unidade = port.buscarUnidade(c.unidadeFaltanteId())
                    .orElseThrow(RegraOperacaoNumerarioException::new);
            var operacao = detalhe.solicitacao()
                    .programar(
                            unidade,
                            usuario(c.usuarioId()),
                            c.idempotencyKey(),
                            c.versaoSolicitacao(),
                            relogio.agora()
                    );
            var salva = port.salvarProgramacao(operacao);
            port.registrarIdempotencia(
                    c.idempotencyKey(),
                    "PROGRAMAR",
                    salva.getId(),
                    c.usuarioId(),
                    relogio.agora()
            );
            return salva;
        });
    }

    @Override
    public OperacaoNumerario iniciarSeparacao(ExecutarOperacaoNumerarioCommand c) {
        validar(
                c,
                c == null ? null : c.solicitacaoId(),
                c == null ? null : c.usuarioId()
        );
        return transacao.executar(() -> {
            var o = operacao(c.solicitacaoId());
            o.iniciarSeparacao(
                    usuario(c.usuarioId()),
                    c.versaoOperacao(),
                    relogio.agora()
            );
            return port.salvarOperacao(o);
        });
    }

    @Override
    public OperacaoNumerario expedir(ExecutarOperacaoNumerarioCommand c) {
        validarExecucao(c);
        return transacao.executar(() -> {
            rejeitarChaveRepetida(c.idempotencyKey());
            var o = operacao(c.solicitacaoId());
            port.buscarUnidadeParaAtualizacao(
                    o.getOrigem()
                            .getId()
            )
                    .orElseThrow(RegraOperacaoNumerarioException::new);
            var agora = relogio.agora();
            o.expedir(
                    usuario(c.usuarioId()),
                    c.versaoOperacao(),
                    c.versaoUnidade(),
                    c.idempotencyKey(),
                    agora
            );
            var salva = port.salvarOperacaoFinanceira(
                    o,
                    TipoMovimentacao.SAIDA_PARA_TRANSITO,
                    c.usuarioId(),
                    c.idempotencyKey(),
                    "Expedição da solicitação " + c.solicitacaoId(),
                    agora
            );
            port.registrarIdempotencia(
                    c.idempotencyKey(),
                    "EXPEDIR",
                    salva.getId(),
                    c.usuarioId(),
                    agora
            );
            return salva;
        });
    }

    @Override
    public OperacaoNumerario registrarOcorrencia(RegistrarOcorrenciaOperacaoCommand c) {
        validar(
                c,
                c == null ? null : c.solicitacaoId(),
                c == null ? null : c.usuarioId()
        );
        return transacao.executar(() -> {
            var o = operacao(c.solicitacaoId());
            o.registrarOcorrencia(
                    c.descricao(),
                    usuario(c.usuarioId()),
                    c.versaoOperacao(),
                    relogio.agora()
            );
            return port.salvarOperacao(o);
        });
    }

    @Override
    public OperacaoNumerario receber(ReceberOperacaoNumerarioCommand c) {
        validar(
                c,
                c == null ? null : c.solicitacaoId(),
                c == null ? null : c.usuarioId()
        );
        validarChave(c.idempotencyKey());
        return transacao.executar(() -> {
            rejeitarChaveRepetida(c.idempotencyKey());
            var o = operacao(c.solicitacaoId());
            port.buscarUnidadeParaAtualizacao(
                    o.getDestino()
                            .getId()
            )
                    .orElseThrow(RegraOperacaoNumerarioException::new);
            var agora = relogio.agora();
            o.receber(
                    c.valorRecebido(),
                    c.justificativaDivergencia(),
                    usuario(c.usuarioId()),
                    c.versaoOperacao(),
                    c.versaoUnidade(),
                    c.idempotencyKey(),
                    agora
            );
            var salva = port.salvarOperacaoFinanceira(
                    o,
                    TipoMovimentacao.ENTRADA_DE_TRANSITO,
                    c.usuarioId(),
                    c.idempotencyKey(),
                    "Recebimento da solicitação " + c.solicitacaoId(),
                    agora
            );
            port.registrarIdempotencia(
                    c.idempotencyKey(),
                    "RECEBER",
                    salva.getId(),
                    c.usuarioId(),
                    agora
            );
            return salva;
        });
    }

    @Override
    public OperacaoNumerario conciliar(ConciliarOperacaoNumerarioCommand c) {
        validar(
                c,
                c == null ? null : c.solicitacaoId(),
                c == null ? null : c.usuarioId()
        );
        validarChave(c.idempotencyKey());
        return transacao.executar(() -> {
            rejeitarChaveRepetida(c.idempotencyKey());
            var o = operacao(c.solicitacaoId());
            var agora = relogio.agora();
            o.conciliar(
                    c.justificativa(),
                    usuario(c.usuarioId()),
                    c.versaoOperacao(),
                    c.idempotencyKey(),
                    agora
            );
            var salva = port.salvarOperacao(o);
            port.registrarIdempotencia(
                    c.idempotencyKey(),
                    "CONCILIAR",
                    salva.getId(),
                    c.usuarioId(),
                    agora
            );
            return salva;
        });
    }

    private void validarExecucao(ExecutarOperacaoNumerarioCommand c) {
        validar(
                c,
                c == null ? null : c.solicitacaoId(),
                c == null ? null : c.usuarioId()
        );
        validarChave(c.idempotencyKey());
    }

    private DetalheSolicitacaoNumerario detalhe(Long id) {
        return port.buscarDetalhe(id)
                .orElseThrow(SolicitacaoNaoEncontradaException::new);
    }

    private OperacaoNumerario operacao(Long id) {
        var o = detalhe(id).operacao();
        if (o == null) {
            throw new RegraOperacaoNumerarioException(
                    "A solicitação ainda não possui uma operação de numerário programada."
            );
        }
        return o;
    }

    private Usuario usuario(Long id) {
        var usuario = usuarios.buscarPorId(id)
                .orElseThrow(UsuarioNaoEncontradoException::new);
        if (!usuario.isAtivo()) {
            throw new RegraOperacaoNumerarioException("O usuário está inativo e não pode executar esta operação.");
        }
        return usuario;
    }

    private void rejeitarChaveRepetida(String key) {
        if (port.existeIdempotencyKey(key))
            throw new IdempotencyKeyDuplicadaException();
    }

    private static void validarChave(String key) {
        if (key == null || key.isBlank()) {
            throw new CampoObrigatorioException("Idempotency-Key");
        }
    }

    private static void validar(Object c, Long... ids) {
        if (c == null)
            throw new CampoObrigatorioException("comando");
        for (Long id : ids) {
            if (id == null)
                throw new CampoObrigatorioException("identificador");
        }
    }
}
