package br.com.gestaonumerario.api.adapter.input.mapper;

import br.com.gestaonumerario.api.adapter.input.controller.dto.response.DashboardResponse;
import br.com.gestaonumerario.api.core.domain.model.ResumoDashboard;
import org.springframework.stereotype.Component;

@Component
public class DashboardRestMapper {

    public DashboardResponse toResponse(ResumoDashboard resumo) {
        return new DashboardResponse(resumo.dataReferencia(), resumo.numerarioTotal(),
                resumo.quantidadeAgenciasEmAlerta(), resumo.quantidadeSolicitacoesPendentes(),
                resumo.quantidadeAbastecimentosHoje(), resumo.valorAbastecidoHoje());
    }
}

