package br.com.gestaonumerario.bff.architecture;

import br.com.gestaonumerario.bff.controller.AgenciaController;
import br.com.gestaonumerario.bff.controller.AuthController;
import br.com.gestaonumerario.bff.controller.DashboardController;
import br.com.gestaonumerario.bff.controller.MovimentacaoController;
import br.com.gestaonumerario.bff.controller.SolicitacaoController;
import br.com.gestaonumerario.bff.service.AgenciaService;
import br.com.gestaonumerario.bff.service.AuthService;
import br.com.gestaonumerario.bff.service.DashboardService;
import br.com.gestaonumerario.bff.service.MovimentacaoService;
import br.com.gestaonumerario.bff.service.SolicitacaoService;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MvcArchitectureTest {

    @Test
    void controllersDependemSomenteDeServices() {
        List<Class<?>> controllers = List.of(
                AuthController.class,
                DashboardController.class,
                AgenciaController.class,
                SolicitacaoController.class,
                MovimentacaoController.class
        );

        List<Class<?>> dependencies = controllers.stream()
                .flatMap(controller -> Arrays.stream(controller.getDeclaredFields()))
                .map(Field::getType)
                .toList();

        assertThat(dependencies).allMatch(
                type -> type.getPackageName()
                        .equals("br.com.gestaonumerario.bff.service")
        );
    }

    @Test
    void servicesDependemSomenteDoClientDaApi() {
        List<Class<?>> services = List.of(
                AuthService.class,
                DashboardService.class,
                AgenciaService.class,
                SolicitacaoService.class,
                MovimentacaoService.class
        );

        List<Class<?>> dependencies = services.stream()
                .flatMap(service -> Arrays.stream(service.getDeclaredFields()))
                .map(Field::getType)
                .toList();

        assertThat(dependencies).allMatch(
                type -> type.getPackageName()
                        .equals("br.com.gestaonumerario.bff.client")
        );
    }
}
