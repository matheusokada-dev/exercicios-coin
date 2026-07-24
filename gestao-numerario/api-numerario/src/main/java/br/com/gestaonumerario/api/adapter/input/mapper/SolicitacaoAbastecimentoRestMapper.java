package br.com.gestaonumerario.api.adapter.input.mapper;

import br.com.gestaonumerario.api.adapter.input.controller.dto.request.AprovarSolicitacaoRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.AtenderSolicitacaoRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.RejeitarSolicitacaoRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.SolicitacaoAbastecimentoResponse;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.SolicitarAbastecimentoRequest;
import br.com.gestaonumerario.api.adapter.input.auth.UsuarioAutenticado;
import br.com.gestaonumerario.api.core.domain.model.SolicitacaoAbastecimento;
import br.com.gestaonumerario.api.core.domain.model.command.AprovarSolicitacaoCommand;
import br.com.gestaonumerario.api.core.domain.model.command.AtenderSolicitacaoCommand;
import br.com.gestaonumerario.api.core.domain.model.command.RejeitarSolicitacaoCommand;
import br.com.gestaonumerario.api.core.domain.model.command.SolicitarAbastecimentoCommand;
import org.springframework.stereotype.Component;

@Component
public class SolicitacaoAbastecimentoRestMapper {

    public SolicitarAbastecimentoCommand toCommand(SolicitarAbastecimentoRequest request, UsuarioAutenticado usuario) {
        return new SolicitarAbastecimentoCommand(
                request.agenciaId(), request.valor(), request.motivo(), request.dataDesejada(), usuario.id());
    }

    public AprovarSolicitacaoCommand toCommand(Long solicitacaoId, AprovarSolicitacaoRequest request, UsuarioAutenticado usuario) {
        return new AprovarSolicitacaoCommand(
                solicitacaoId, usuario.id(), request.justificativaDecisao(), request.justificativaEspecial());
    }

    public RejeitarSolicitacaoCommand toCommand(Long solicitacaoId, RejeitarSolicitacaoRequest request, UsuarioAutenticado usuario) {
        return new RejeitarSolicitacaoCommand(
                solicitacaoId, usuario.id(), request.justificativaDecisao());
    }

    public AtenderSolicitacaoCommand toCommand(Long solicitacaoId, AtenderSolicitacaoRequest request, UsuarioAutenticado usuario) {
        return new AtenderSolicitacaoCommand(solicitacaoId, usuario.id(), request.idempotencyKey());
    }

    public SolicitacaoAbastecimentoResponse toResponse(SolicitacaoAbastecimento solicitacao) {
        return new SolicitacaoAbastecimentoResponse(
                solicitacao.getId(),
                solicitacao.getAgencia().getId(),
                solicitacao.getValor(),
                solicitacao.getMotivo(),
                solicitacao.getDataDesejada(),
                solicitacao.getStatus(),
                solicitacao.getSolicitante().getId(),
                solicitacao.getDecisor() == null ? null : solicitacao.getDecisor().getId(),
                solicitacao.getJustificativaDecisao(),
                solicitacao.getJustificativaEspecial(),
                solicitacao.getDataCriacao(),
                solicitacao.getDataDecisao(),
                solicitacao.getDataAtendimento(),
                solicitacao.getVersao());
    }
}


