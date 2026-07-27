package br.com.gestaonumerario.api.core.usecase.solicitacao;

import br.com.gestaonumerario.api.core.domain.enums.*;
import br.com.gestaonumerario.api.core.domain.model.*;
import br.com.gestaonumerario.api.core.domain.model.command.*;
import br.com.gestaonumerario.api.core.exception.*;
import br.com.gestaonumerario.api.port.input.FinanceiroNumerarioInputPort;
import br.com.gestaonumerario.api.port.output.*;

public class FinanceiroNumerarioUseCase implements FinanceiroNumerarioInputPort {
    static final String CARGA_INICIAL = "CARGA_INICIAL_TESOURARIA";
    private final NumerarioOutputPort port;
    private final UsuarioOutputPort usuarios;
    private final RelogioOutputPort relogio;
    private final TransacaoOutputPort transacao;

    public FinanceiroNumerarioUseCase(NumerarioOutputPort port,UsuarioOutputPort usuarios,
            RelogioOutputPort relogio,TransacaoOutputPort transacao) {
        this.port=port;this.usuarios=usuarios;this.relogio=relogio;this.transacao=transacao;
    }

    @Override
    public UnidadeOperacional realizarCargaInicial(CargaInicialTesourariaCommand c) {
        validarComando(c,c==null?null:c.usuarioId(),c==null?null:c.idempotencyKey(),
                c==null?null:c.justificativa());
        return transacao.executar(() -> {
            rejeitarChave(c.idempotencyKey());
            gestor(c.usuarioId());
            if (port.existeComandoDoTipo(CARGA_INICIAL)) {
                throw new RegraOperacaoNumerarioException(
                        "A carga inicial da Tesouraria já foi realizada.");
            }
            var tesourarias=port.consultarUnidadesAtivas(TipoUnidadeOperacional.TESOURARIA);
            if (tesourarias.size() != 1) {
                throw new RegraOperacaoNumerarioException(
                        "A carga inicial exige exatamente uma Tesouraria ativa.");
            }
            var bloqueada=port.buscarUnidadeParaAtualizacao(tesourarias.getFirst().getId())
                    .orElseThrow(RegraOperacaoNumerarioException::new);
            var agora=relogio.agora();
            bloqueada.creditar(c.valor(),c.versaoUnidade(),agora);
            var salva=port.salvarAjusteFinanceiro(bloqueada,null,TipoMovimentacao.AJUSTE,
                    true,c.valor(),c.usuarioId(),c.idempotencyKey(),
                    "Carga inicial da Tesouraria: "+c.justificativa().trim(),agora);
            port.registrarIdempotencia(c.idempotencyKey(),CARGA_INICIAL,null,c.usuarioId(),agora);
            return salva;
        });
    }

    @Override
    public UnidadeOperacional ajustarDivergencia(AjustarDivergenciaCommand c) {
        validarComando(c,c==null?null:c.solicitacaoId(),c==null?null:c.unidadeId(),
                c==null?null:c.usuarioId(),c==null?null:c.idempotencyKey(),
                c==null?null:c.justificativa());
        return transacao.executar(() -> {
            rejeitarChave(c.idempotencyKey());
            gestor(c.usuarioId());
            var detalhe=port.buscarDetalhe(c.solicitacaoId())
                    .orElseThrow(SolicitacaoNaoEncontradaException::new);
            var operacao=detalhe.operacao();
            if(operacao==null || operacao.getValorDivergencia()==null
                    || operacao.getValorDivergencia().signum()<=0
                    || (operacao.getStatus()!=StatusOperacaoNumerario.COM_DIVERGENCIA
                    && operacao.getStatus()!=StatusOperacaoNumerario.CONCILIADA)) {
                throw new RegraOperacaoNumerarioException(
                        "A solicitação não possui uma divergência disponível para ajuste.");
            }
            boolean pertenceRota=operacao.getOrigem().getId().equals(c.unidadeId())
                    || operacao.getDestino().getId().equals(c.unidadeId());
            if(!pertenceRota || c.valor()==null
                    || c.valor().compareTo(operacao.getValorDivergencia())>0) {
                throw new RegraOperacaoNumerarioException(
                        !pertenceRota
                                ? "A unidade informada não pertence à rota da operação."
                                : "O valor do ajuste não pode exceder a divergência registrada.");
            }
            var unidade=port.buscarUnidadeParaAtualizacao(c.unidadeId())
                    .orElseThrow(RegraOperacaoNumerarioException::new);
            var agora=relogio.agora();
            unidade.ajustar(c.valor(),c.entrada(),c.versaoUnidade(),agora);
            var salva=port.salvarAjusteFinanceiro(unidade,operacao.getId(),
                    TipoMovimentacao.AJUSTE_DIVERGENCIA,c.entrada(),c.valor(),c.usuarioId(),
                    c.idempotencyKey(),c.justificativa().trim(),agora);
            port.registrarIdempotencia(c.idempotencyKey(),"AJUSTE_DIVERGENCIA",
                    operacao.getId(),c.usuarioId(),agora);
            return salva;
        });
    }

    private Usuario gestor(Long id) {
        var u=usuarios.buscarPorId(id).orElseThrow(UsuarioNaoEncontradoException::new);
        if (!u.isAtivo()) {
            throw new RegraOperacaoNumerarioException(
                    "O usuário está inativo e não pode executar operações financeiras.");
        }
        if(u.getPerfil()!=PerfilUsuario.GESTOR) throw new ApenasGestorPodeDecidirException();
        return u;
    }
    private void rejeitarChave(String key) {
        if(port.existeIdempotencyKey(key)) throw new IdempotencyKeyDuplicadaException();
    }
    private static void validarComando(Object c,Object... campos) {
        if (c == null) throw new CampoObrigatorioException("comando");
        for(Object campo:campos)
            if(campo==null || campo instanceof String s && s.isBlank())
                throw new CampoObrigatorioException("dados do comando");
    }
}
