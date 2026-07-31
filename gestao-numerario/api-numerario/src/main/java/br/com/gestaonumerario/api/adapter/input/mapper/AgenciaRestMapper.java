package br.com.gestaonumerario.api.adapter.input.mapper;

import br.com.gestaonumerario.api.adapter.input.controller.dto.request.AtualizarAgenciaRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.CriarAgenciaRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.AgenciaResponse;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.DetalheAgenciaResponse;
import br.com.gestaonumerario.api.core.domain.model.Agencia;
import br.com.gestaonumerario.api.core.domain.model.DetalheAgencia;
import br.com.gestaonumerario.api.core.domain.model.command.AtualizarAgenciaCommand;
import br.com.gestaonumerario.api.core.domain.model.command.CriarAgenciaCommand;
import org.springframework.stereotype.Component;

@Component
public class AgenciaRestMapper {

    public CriarAgenciaCommand toCommand(CriarAgenciaRequest request) {
        return new CriarAgenciaCommand(
                request.codigo(),
                request.nome(),
                request.cidade(),
                request.saldoAtual(),
                request.limiteMinimo()
        );
    }

    public AtualizarAgenciaCommand toCommand(Long id, AtualizarAgenciaRequest request) {
        return new AtualizarAgenciaCommand(
                id,
                request.nome(),
                request.cidade(),
                request.limiteMinimo()
        );
    }

    public AgenciaResponse toResponse(Agencia agencia) {
        return new AgenciaResponse(
                agencia.getId(),
                agencia.getCodigo(),
                agencia.getNome(),
                agencia.getCidade(),
                agencia.getSaldoAtual(),
                agencia.getLimiteMinimo(),
                agencia.isAtivo(),
                agencia.estaAbaixoDoLimite(),
                agencia.calcularSugestaoAbastecimento(),
                agencia.getVersao()
        );
    }

    public DetalheAgenciaResponse toResponse(DetalheAgencia detalhe) {
        return new DetalheAgenciaResponse(
                toResponse(detalhe.agencia()),
                detalhe.dataReferencia(),
                detalhe.valorEntradasHoje(),
                detalhe.valorSaidasHoje(),
                detalhe.valorAbastecimentoAprovado(),
                detalhe.saldoPrevistoAposAbastecimentoAprovado()
        );
    }
}
