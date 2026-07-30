package br.com.gestaonumerario.api.adapter.input.mapper;

import br.com.gestaonumerario.api.adapter.input.controller.dto.response.MovimentacaoResponse;
import br.com.gestaonumerario.api.core.domain.model.Movimentacao;
import org.springframework.stereotype.Component;

@Component
public class MovimentacaoRestMapper {

    public MovimentacaoResponse toResponse(Movimentacao movimentacao) {
        return new MovimentacaoResponse(
                movimentacao.getId(),
                movimentacao.getAgencia()
                        .getId(),
                movimentacao.getSolicitacao() == null
                        ? null
                        : movimentacao.getSolicitacao()
                                .getId(),
                movimentacao.getTipo(),
                movimentacao.isEntrada(),
                movimentacao.getValor(),
                movimentacao.getSaldoAnterior(),
                movimentacao.getSaldoPosterior(),
                movimentacao.getDescricao(),
                movimentacao.getDataMovimento(),
                movimentacao.getUsuario()
                        .getId()
        );
    }
}
