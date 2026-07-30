package br.com.gestaonumerario.api.core.usecase.solicitacao;

import br.com.gestaonumerario.api.core.domain.model.DetalheSolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.model.FiltroSolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.model.Pagina;
import br.com.gestaonumerario.api.core.domain.model.SolicitacaoNumerario;
import br.com.gestaonumerario.api.core.domain.model.UnidadeOperacional;
import br.com.gestaonumerario.api.core.domain.model.Usuario;
import br.com.gestaonumerario.api.core.domain.model.command.CriarSolicitacaoNumerarioCommand;
import br.com.gestaonumerario.api.core.domain.model.command.DecidirSolicitacaoNumerarioCommand;
import br.com.gestaonumerario.api.core.exception.AgenciaNaoEncontradaException;
import br.com.gestaonumerario.api.core.exception.CampoObrigatorioException;
import br.com.gestaonumerario.api.core.exception.RegraOperacaoNumerarioException;
import br.com.gestaonumerario.api.core.exception.SolicitacaoAbertaDuplicadaException;
import br.com.gestaonumerario.api.core.exception.SolicitacaoNaoEncontradaException;
import br.com.gestaonumerario.api.core.exception.UsuarioNaoEncontradoException;
import br.com.gestaonumerario.api.port.input.SolicitacaoNumerarioInputPort;
import br.com.gestaonumerario.api.port.output.NumerarioOutputPort;
import br.com.gestaonumerario.api.port.output.RelogioOutputPort;
import br.com.gestaonumerario.api.port.output.TransacaoOutputPort;
import br.com.gestaonumerario.api.port.output.UsuarioOutputPort;

public class SolicitacaoNumerarioUseCase implements SolicitacaoNumerarioInputPort {
    private final NumerarioOutputPort numerarioPort;
    private final UsuarioOutputPort usuarioPort;
    private final RelogioOutputPort relogio;
    private final TransacaoOutputPort transacao;

    public SolicitacaoNumerarioUseCase(
            NumerarioOutputPort numerarioPort,
            UsuarioOutputPort usuarioPort,
            RelogioOutputPort relogio,
            TransacaoOutputPort transacao) {
        this.numerarioPort = numerarioPort;
        this.usuarioPort = usuarioPort;
        this.relogio = relogio;
        this.transacao = transacao;
    }

    @Override
    public SolicitacaoNumerario criar(CriarSolicitacaoNumerarioCommand c) {
        if (c == null)
            throw new CampoObrigatorioException("comando");
        if (c.agenciaId() == null)
            throw new CampoObrigatorioException("agenciaId");
        if (c.solicitanteId() == null)
            throw new CampoObrigatorioException("solicitanteId");
        return transacao.executar(() -> {
            if (numerarioPort.existeSolicitacaoAberta(c.agenciaId()))
                throw new SolicitacaoAbertaDuplicadaException();
            UnidadeOperacional unidade = numerarioPort.buscarUnidadeDaAgencia(c.agenciaId())
                    .orElseThrow(AgenciaNaoEncontradaException::new);
            if (!unidade.isAtivo()) {
                throw new RegraOperacaoNumerarioException(
                        "A agência está inativa e não pode receber novas solicitações."
                );
            }
            Usuario usuario = usuario(c.solicitanteId());
            return numerarioPort.salvarSolicitacao(
                    SolicitacaoNumerario.criar(
                            c.tipo(),
                            c.agenciaId(),
                            unidade,
                            c.valor(),
                            c.motivo(),
                            c.dataDesejada(),
                            usuario,
                            relogio.hoje(),
                            relogio.agora()
                    )
            );
        });
    }

    @Override
    public SolicitacaoNumerario aprovar(DecidirSolicitacaoNumerarioCommand c) {
        return decidir(
                c,
                (s, u) -> s.aprovar(
                        u,
                        c.justificativa(),
                        c.versao(),
                        relogio.agora()
                )
        );
    }

    @Override
    public SolicitacaoNumerario rejeitar(DecidirSolicitacaoNumerarioCommand c) {
        return decidir(
                c,
                (s, u) -> s.rejeitar(
                        u,
                        c.justificativa(),
                        c.versao(),
                        relogio.agora()
                )
        );
    }

    @Override
    public SolicitacaoNumerario cancelar(DecidirSolicitacaoNumerarioCommand c) {
        return decidir(
                c,
                (s, u) -> s.cancelar(
                        u,
                        c.justificativa(),
                        c.versao(),
                        relogio.agora()
                )
        );
    }

    private SolicitacaoNumerario decidir(
            DecidirSolicitacaoNumerarioCommand c,
            java.util.function.BiConsumer<SolicitacaoNumerario, Usuario> acao) {
        if (c == null)
            throw new CampoObrigatorioException("comando");
        if (c.solicitacaoId() == null)
            throw new CampoObrigatorioException("solicitacaoId");
        if (c.usuarioId() == null)
            throw new CampoObrigatorioException("usuarioId");
        return transacao.executar(() -> {
            SolicitacaoNumerario s = numerarioPort.buscarDetalhe(c.solicitacaoId())
                    .map(DetalheSolicitacaoNumerario::solicitacao)
                    .orElseThrow(SolicitacaoNaoEncontradaException::new);
            acao.accept(
                    s,
                    usuario(c.usuarioId())
            );
            return numerarioPort.salvarSolicitacao(s);
        });
    }

    @Override
    public Pagina<SolicitacaoNumerario> consultar(FiltroSolicitacaoNumerario f) {
        if (f == null)
            throw new CampoObrigatorioException("filtro");
        if (f.pagina() < 0) {
            throw new RegraOperacaoNumerarioException("A página não pode ser negativa.");
        }
        if (f.tamanho() < 1 || f.tamanho() > 100) {
            throw new RegraOperacaoNumerarioException("O tamanho da página deve estar entre 1 e 100.");
        }
        return numerarioPort.consultarSolicitacoes(f);
    }

    @Override
    public DetalheSolicitacaoNumerario detalhar(Long id) {
        return numerarioPort.buscarDetalhe(id)
                .orElseThrow(SolicitacaoNaoEncontradaException::new);
    }

    private Usuario usuario(Long id) {
        Usuario usuario = usuarioPort.buscarPorId(id)
                .orElseThrow(UsuarioNaoEncontradoException::new);
        if (!usuario.isAtivo()) {
            throw new RegraOperacaoNumerarioException("O usuário está inativo e não pode executar esta operação.");
        }
        return usuario;
    }
}
