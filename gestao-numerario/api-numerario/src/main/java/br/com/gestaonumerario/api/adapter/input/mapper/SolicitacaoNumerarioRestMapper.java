package br.com.gestaonumerario.api.adapter.input.mapper;

import br.com.gestaonumerario.api.adapter.input.controller.dto.response.*;
import br.com.gestaonumerario.api.core.domain.model.*;
import org.springframework.stereotype.Component;

@Component
public class SolicitacaoNumerarioRestMapper {
    public OperacaoNumerarioResponse toResponse(OperacaoNumerario o) {
        return new OperacaoNumerarioResponse(o.getId(),o.getSolicitacao().getId(),
                o.getOrigem().getId(),o.getDestino().getId(),o.getStatus(),
                o.getValorProgramado(),o.getValorExpedido(),o.getValorRecebido(),
                o.getValorDivergencia(),o.getDataProgramacao(),o.getDataExpedicao(),
                o.getDataRecebimento(),o.getDataConciliacao(),o.getJustificativaDivergencia(),
                o.getDescricaoOcorrencia(),o.getVersao());
    }
    public SolicitacaoNumerarioResponse toResponse(SolicitacaoNumerario s) {
        return new SolicitacaoNumerarioResponse(s.getId(),s.getTipoOperacao(),s.getAgenciaReferenciaId(),
                s.getOrigem()==null?null:s.getOrigem().getId(),s.getDestino()==null?null:s.getDestino().getId(),
                s.getValorSolicitado(),s.getMotivo(),s.getDataDesejada(),s.getStatus(),
                s.getSolicitante().getId(),s.getAprovador()==null?null:s.getAprovador().getId(),
                s.getJustificativaDecisao(),s.getDataCriacao(),s.getDataDecisao(),
                s.getCanceladoPor()==null?null:s.getCanceladoPor().getId(),s.getJustificativaCancelamento(),
                s.getDataCancelamento(),s.getDataConclusao(),s.getVersao());
    }
    public DetalheSolicitacaoNumerarioResponse toResponse(DetalheSolicitacaoNumerario d) {
        var o=d.operacao();
        var operacao=o==null?null:new DetalheSolicitacaoNumerarioResponse.OperacaoResumo(
                o.getId(),o.getStatus(),o.getOrigem().getId(),o.getDestino().getId(),
                o.getValorProgramado(),o.getValorExpedido(),o.getValorRecebido(),
                o.getValorDivergencia(),o.getVersao());
        var historico=d.historico().stream().map(h ->
                new DetalheSolicitacaoNumerarioResponse.HistoricoResumo(h.id(),h.evento(),
                        h.statusAnterior(),h.statusNovo(),h.usuarioId(),h.dataEvento(),
                        h.justificativa(),h.dadosComplementares())).toList();
        return new DetalheSolicitacaoNumerarioResponse(toResponse(d.solicitacao()),operacao,historico);
    }
}
