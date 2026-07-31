package br.com.gestaonumerario.api.core.usecase.dashboard;

import br.com.gestaonumerario.api.core.domain.model.ResumoDashboard;
import br.com.gestaonumerario.api.port.input.ConsultarDashboardInputPort;
import br.com.gestaonumerario.api.port.output.DashboardOutputPort;
import br.com.gestaonumerario.api.port.output.RelogioOutputPort;

public class DashboardUseCase implements ConsultarDashboardInputPort {

    private final DashboardOutputPort dashboardPort;
    private final RelogioOutputPort relogioPort;

    public DashboardUseCase(DashboardOutputPort dashboardPort, RelogioOutputPort relogioPort) {
        this.dashboardPort = dashboardPort;
        this.relogioPort = relogioPort;
    }

    @Override
    public ResumoDashboard consultar() {
        return dashboardPort.consultar(relogioPort.hoje());
    }
}
