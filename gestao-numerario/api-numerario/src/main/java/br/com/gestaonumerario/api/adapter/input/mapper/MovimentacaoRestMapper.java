package br.com.gestaonumerario.api.adapter.input.mapper;

import br.com.gestaonumerario.api.adapter.input.controller.dto.response.MovimentacaoResponse;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.RegistrarMovimentacaoRequest;
import br.com.gestaonumerario.api.adapter.input.auth.UsuarioAutenticado;
import br.com.gestaonumerario.api.core.domain.model.Movimentacao;
import br.com.gestaonumerario.api.core.domain.model.command.RegistrarMovimentacaoCommand;
import org.springframework.stereotype.Component;

@Component
public class MovimentacaoRestMapper {

    public RegistrarMovimentacaoCommand toCommand(RegistrarMovimentacaoRequest request, UsuarioAutenticado usuario) {
        return new RegistrarMovimentacaoCommand(request.agenciaId(), usuario.id(), request.tipo(),
                request.entradaAjuste(), request.valor(), request.descricao(), request.idempotencyKey());
    }

    public MovimentacaoResponse toResponse(Movimentacao movimentacao) {
        return new MovimentacaoResponse(movimentacao.getId(), movimentacao.getAgencia().getId(),
                movimentacao.getSolicitacao() == null ? null : movimentacao.getSolicitacao().getId(),
                movimentacao.getTipo(), movimentacao.isEntrada(), movimentacao.getValor(),
                movimentacao.getSaldoAnterior(), movimentacao.getSaldoPosterior(), movimentacao.getDescricao(),
                movimentacao.getDataMovimento(), movimentacao.getUsuario().getId());
    }
}


