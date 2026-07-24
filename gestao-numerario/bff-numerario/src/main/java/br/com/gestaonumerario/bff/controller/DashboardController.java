package br.com.gestaonumerario.bff.controller;

import br.com.gestaonumerario.bff.dto.DashboardResponse;
import br.com.gestaonumerario.bff.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardResponse consultar(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return dashboardService.consultar(authorization);
    }
}
