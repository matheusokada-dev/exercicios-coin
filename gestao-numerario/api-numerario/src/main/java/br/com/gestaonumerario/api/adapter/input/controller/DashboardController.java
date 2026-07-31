package br.com.gestaonumerario.api.adapter.input.controller;

import br.com.gestaonumerario.api.adapter.input.contract.DashboardApi;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.DashboardResponse;
import br.com.gestaonumerario.api.adapter.input.mapper.DashboardRestMapper;
import br.com.gestaonumerario.api.port.input.ConsultarDashboardInputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController implements DashboardApi {

    private final ConsultarDashboardInputPort consultarUseCase;
    private final DashboardRestMapper mapper;

    @GetMapping
    @Override
    public DashboardResponse consultar() {
        return mapper.toResponse(consultarUseCase.consultar());
    }
}
