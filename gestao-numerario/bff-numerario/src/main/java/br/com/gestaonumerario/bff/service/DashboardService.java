package br.com.gestaonumerario.bff.service;

import br.com.gestaonumerario.bff.client.ApiNumerarioClient;
import br.com.gestaonumerario.bff.dto.DashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ApiNumerarioClient apiNumerarioClient;

    public DashboardResponse consultar(String authorization) {
        return apiNumerarioClient.consultarDashboard(authorization);
    }
}
