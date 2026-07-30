package br.com.gestaonumerario.bff.contract;

import br.com.gestaonumerario.bff.dto.LoginRequest;
import br.com.gestaonumerario.bff.dto.LoginResponse;
import br.com.gestaonumerario.bff.dto.SessaoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Autenticação", description = "Login e sessão consumidos pelo frontend Angular.")
public interface AuthApi {

    @Operation(summary = "Autenticar usuário", description = "Encaminha as credenciais à API e devolve o access token e os dados da sessão.")
    @SecurityRequirements
    LoginResponse login(LoginRequest request);

    @Operation(summary = "Consultar sessão atual", description = "Valida o bearer token e devolve a identidade e o perfil do usuário.")
    SessaoResponse me(@Parameter(hidden = true) String authorization);
}
