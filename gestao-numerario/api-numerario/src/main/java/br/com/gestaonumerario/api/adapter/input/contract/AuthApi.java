package br.com.gestaonumerario.api.adapter.input.contract;

import br.com.gestaonumerario.api.adapter.input.auth.UsuarioAutenticado;
import br.com.gestaonumerario.api.adapter.input.controller.AuthController.LoginRequest;
import br.com.gestaonumerario.api.adapter.input.controller.AuthController.LoginResponse;
import br.com.gestaonumerario.api.adapter.input.controller.AuthController.SessaoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Autenticação", description = "Login e consulta da sessão autenticada.")
public interface AuthApi {

    @Operation(
            summary = "Autenticar usuário",
            description = "Valida login e senha e devolve um access token JWT com validade de oito horas."
    )
    @SecurityRequirements
    LoginResponse login(LoginRequest request);

    @Operation(
            summary = "Consultar sessão atual",
            description = "Retorna a identidade e o perfil associados ao JWT informado."
    )
    SessaoResponse me(@Parameter(hidden = true) UsuarioAutenticado autenticado);
}
