package br.com.gestaonumerario.bff.controller;

import br.com.gestaonumerario.bff.dto.AjustarDivergenciaRequest;
import br.com.gestaonumerario.bff.dto.AtualizarAgenciaRequest;
import br.com.gestaonumerario.bff.dto.CargaInicialTesourariaRequest;
import br.com.gestaonumerario.bff.dto.ConciliarOperacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.CriarAgenciaRequest;
import br.com.gestaonumerario.bff.dto.CriarSolicitacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.DecidirSolicitacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.ExecutarOperacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.OcorrenciaOperacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.ProgramarOperacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.ReceberOperacaoNumerarioRequest;
import br.com.gestaonumerario.bff.dto.VersaoOperacaoNumerarioRequest;
import br.com.gestaonumerario.bff.service.AgenciaService;
import br.com.gestaonumerario.bff.service.ComandoNumerarioService;
import br.com.gestaonumerario.bff.service.ConsultaNumerarioService;
import br.com.gestaonumerario.bff.service.DashboardService;
import br.com.gestaonumerario.bff.service.MovimentacaoService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ControllerDelegationTest {

    @Test
    void deveDelegarEndpointsAdministrativos() {
        ConsultaNumerarioService consultas = mock(ConsultaNumerarioService.class);
        ComandoNumerarioService comandos = mock(ComandoNumerarioService.class);
        ConsultaNumerarioController controller = new ConsultaNumerarioController(
                consultas,
                comandos
        );
        LocalDate data = LocalDate.of(
                2026,
                7,
                25
        );
        CriarSolicitacaoNumerarioRequest criar = mock(CriarSolicitacaoNumerarioRequest.class);
        DecidirSolicitacaoNumerarioRequest decidir = mock(DecidirSolicitacaoNumerarioRequest.class);
        ProgramarOperacaoNumerarioRequest programar = mock(ProgramarOperacaoNumerarioRequest.class);
        VersaoOperacaoNumerarioRequest versao = mock(VersaoOperacaoNumerarioRequest.class);
        ExecutarOperacaoNumerarioRequest executar = mock(ExecutarOperacaoNumerarioRequest.class);
        OcorrenciaOperacaoNumerarioRequest ocorrencia = mock(OcorrenciaOperacaoNumerarioRequest.class);
        ReceberOperacaoNumerarioRequest receber = mock(ReceberOperacaoNumerarioRequest.class);
        ConciliarOperacaoNumerarioRequest conciliar = mock(ConciliarOperacaoNumerarioRequest.class);
        CargaInicialTesourariaRequest carga = mock(CargaInicialTesourariaRequest.class);
        AjustarDivergenciaRequest ajuste = mock(AjustarDivergenciaRequest.class);

        controller.solicitacoes(
                "Bearer token",
                1L,
                "SUPRIMENTO",
                "PENDENTE",
                2L,
                3L,
                data,
                data,
                0,
                20
        );
        controller.detalhe(
                "Bearer token",
                1L
        );
        controller.historico(
                "Bearer token",
                1L
        );
        controller.unidades(
                "Bearer token",
                "AGENCIA"
        );
        controller.operacoes(
                "Bearer token",
                "PROGRAMADA",
                2L,
                3L,
                data,
                data,
                0,
                20
        );
        assertThat(
                controller.criar(
                        "Bearer token",
                        criar
                )
                        .getStatusCode()
                        .value()
        ).isEqualTo(201);
        controller.aprovar(
                "Bearer token",
                1L,
                decidir
        );
        controller.rejeitar(
                "Bearer token",
                1L,
                decidir
        );
        controller.cancelar(
                "Bearer token",
                1L,
                decidir
        );
        controller.programar(
                "Bearer token",
                "p-1",
                1L,
                programar
        );
        controller.separar(
                "Bearer token",
                1L,
                versao
        );
        controller.expedir(
                "Bearer token",
                "e-1",
                1L,
                executar
        );
        controller.ocorrencia(
                "Bearer token",
                1L,
                ocorrencia
        );
        controller.receber(
                "Bearer token",
                "r-1",
                1L,
                receber
        );
        controller.conciliar(
                "Bearer token",
                "c-1",
                1L,
                conciliar
        );
        controller.carga(
                "Bearer token",
                "t-1",
                carga
        );
        controller.ajustar(
                "Bearer token",
                "a-1",
                1L,
                ajuste
        );

        verify(comandos).decidir(
                "Bearer token",
                1L,
                "aprovar",
                decidir
        );
        verify(comandos).decidir(
                "Bearer token",
                1L,
                "rejeitar",
                decidir
        );
        verify(comandos).decidir(
                "Bearer token",
                1L,
                "cancelar",
                decidir
        );
    }

    @Test
    void deveDelegarEndpointsDeAgenciaMovimentacaoEDashboard() {
        AgenciaService agencias = mock(AgenciaService.class);
        AgenciaController agenciaController = new AgenciaController(agencias);
        CriarAgenciaRequest criarAgencia = mock(CriarAgenciaRequest.class);
        AtualizarAgenciaRequest atualizar = mock(AtualizarAgenciaRequest.class);

        agenciaController.listar(
                "Bearer token",
                "centro",
                true,
                false,
                "codigo",
                "asc",
                0,
                20
        );
        agenciaController.detalhe(
                "Bearer token",
                1L
        );
        assertThat(
                agenciaController.criar(
                        "Bearer token",
                        criarAgencia
                )
                        .getStatusCode()
                        .value()
        ).isEqualTo(201);
        agenciaController.atualizar(
                "Bearer token",
                1L,
                atualizar
        );
        assertThat(
                agenciaController.desativar(
                        "Bearer token",
                        1L
                )
                        .getStatusCode()
                        .value()
        ).isEqualTo(204);

        MovimentacaoService movimentacoes = mock(MovimentacaoService.class);
        MovimentacaoController movimentacaoController = new MovimentacaoController(movimentacoes);
        movimentacaoController.listar(
                "Bearer token",
                1L,
                "DEPOSITO",
                LocalDate.now(),
                LocalDate.now(),
                0,
                20
        );

        DashboardService dashboard = mock(DashboardService.class);
        new DashboardController(dashboard).consultar("Bearer token");
        verify(dashboard).consultar("Bearer token");
    }
}
