package br.com.gestaonumerario.api.config;

import br.com.gestaonumerario.api.core.usecase.agencia.AgenciaUseCase;
import br.com.gestaonumerario.api.core.usecase.movimentacao.MovimentacaoUseCase;
import br.com.gestaonumerario.api.core.usecase.dashboard.DashboardUseCase;
import br.com.gestaonumerario.api.core.usecase.autenticacao.AutenticacaoUseCase;
import br.com.gestaonumerario.api.core.usecase.solicitacao.SolicitacaoUseCase;
import br.com.gestaonumerario.api.core.usecase.solicitacao.SolicitacaoNumerarioUseCase;
import br.com.gestaonumerario.api.core.usecase.solicitacao.OperacaoNumerarioUseCase;
import br.com.gestaonumerario.api.core.usecase.solicitacao.FinanceiroNumerarioUseCase;
import br.com.gestaonumerario.api.core.usecase.solicitacao.ConsultaNumerarioUseCase;
import br.com.gestaonumerario.api.port.output.NumerarioOutputPort;
import br.com.gestaonumerario.api.core.usecase.usuario.UsuarioUseCase;
import br.com.gestaonumerario.api.port.output.AgenciaOutputPort;
import br.com.gestaonumerario.api.port.output.CodificadorSenhaOutputPort;
import br.com.gestaonumerario.api.port.output.DashboardOutputPort;
import br.com.gestaonumerario.api.port.output.TokenJwtOutputPort;
import br.com.gestaonumerario.api.port.output.MovimentacaoOutputPort;
import br.com.gestaonumerario.api.port.output.RelogioOutputPort;
import br.com.gestaonumerario.api.port.output.SolicitacaoAbastecimentoOutputPort;
import br.com.gestaonumerario.api.port.output.TransacaoOutputPort;
import br.com.gestaonumerario.api.port.output.UsuarioOutputPort;
import br.com.gestaonumerario.api.port.output.RefreshTokenOutputPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;

@Configuration
public class UseCaseConfig {

    @Bean
    SolicitacaoNumerarioUseCase solicitacaoNumerarioService(
            NumerarioOutputPort numerarioPort, UsuarioOutputPort usuarioPort,
            RelogioOutputPort relogioPort, TransacaoOutputPort transacaoPort) {
        return new SolicitacaoNumerarioUseCase(numerarioPort,usuarioPort,relogioPort,transacaoPort);
    }

    @Bean
    OperacaoNumerarioUseCase operacaoNumerarioService(
            NumerarioOutputPort numerarioPort,UsuarioOutputPort usuarioPort,
            RelogioOutputPort relogioPort,TransacaoOutputPort transacaoPort) {
        return new OperacaoNumerarioUseCase(numerarioPort,usuarioPort,relogioPort,transacaoPort);
    }

    @Bean
    FinanceiroNumerarioUseCase financeiroNumerarioService(
            NumerarioOutputPort numerarioPort,UsuarioOutputPort usuarioPort,
            RelogioOutputPort relogioPort,TransacaoOutputPort transacaoPort) {
        return new FinanceiroNumerarioUseCase(numerarioPort,usuarioPort,relogioPort,transacaoPort);
    }

    @Bean
    ConsultaNumerarioUseCase consultaNumerarioService(NumerarioOutputPort numerarioPort) {
        return new ConsultaNumerarioUseCase(numerarioPort);
    }

    @Bean
    SolicitacaoUseCase solicitacaoService(
            AgenciaOutputPort agenciaPort,
            UsuarioOutputPort usuarioPort,
            SolicitacaoAbastecimentoOutputPort solicitacaoPort,
            MovimentacaoOutputPort movimentacaoPort,
            RelogioOutputPort relogioPort,
            TransacaoOutputPort transacaoPort
    ) {
        return new SolicitacaoUseCase(agenciaPort, usuarioPort, solicitacaoPort,
                movimentacaoPort, relogioPort, transacaoPort);
    }

    @Bean
    AgenciaUseCase agenciaService(
            AgenciaOutputPort agenciaPort,
            SolicitacaoAbastecimentoOutputPort solicitacaoPort,
            MovimentacaoOutputPort movimentacaoPort,
            RelogioOutputPort relogioPort,
            TransacaoOutputPort transacaoPort
    ) {
        return new AgenciaUseCase(agenciaPort, solicitacaoPort, movimentacaoPort, relogioPort, transacaoPort);
    }

    @Bean
    UsuarioUseCase usuarioService(
            UsuarioOutputPort usuarioPort,
            CodificadorSenhaOutputPort codificadorSenhaPort,
            RelogioOutputPort relogioPort,
            TransacaoOutputPort transacaoPort
    ) {
        return new UsuarioUseCase(usuarioPort, codificadorSenhaPort, relogioPort, transacaoPort);
    }

    @Bean
    MovimentacaoUseCase movimentacaoService(
            AgenciaOutputPort agenciaPort,
            UsuarioOutputPort usuarioPort,
            MovimentacaoOutputPort movimentacaoPort,
            RelogioOutputPort relogioPort,
            TransacaoOutputPort transacaoPort
    ) {
        return new MovimentacaoUseCase(agenciaPort, usuarioPort, movimentacaoPort, relogioPort, transacaoPort);
    }

    @Bean
    DashboardUseCase dashboardService(DashboardOutputPort dashboardPort, RelogioOutputPort relogioPort) {
        return new DashboardUseCase(dashboardPort, relogioPort);
    }

    @Bean
    AutenticacaoUseCase autenticacaoService(UsuarioOutputPort usuarioPort, CodificadorSenhaOutputPort codificadorSenhaPort,
                                            TokenJwtOutputPort tokenJwtPort, RelogioOutputPort relogioPort,
                                            RefreshTokenOutputPort refreshTokenPort,
                                            @Value("${app.security.login.max-failed-attempts}") int limiteTentativas,
                                            @Value("${app.security.login.lock-duration-minutes}") long minutosBloqueio,
                                            @Value("${app.security.refresh.expiration-hours}") long horasRefresh) {
        return new AutenticacaoUseCase(usuarioPort, codificadorSenhaPort, tokenJwtPort, relogioPort,
                limiteTentativas, Duration.ofMinutes(minutosBloqueio),
                Duration.ofHours(horasRefresh), refreshTokenPort);
    }
}
