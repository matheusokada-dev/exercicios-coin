package br.com.gestaonumerario.api.adapter.input.contract;

import br.com.gestaonumerario.api.adapter.input.controller.dto.request.CriarUsuarioRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Usuários", description = "Cadastro e consulta de usuários do sistema.")
public interface UsuarioApi {

    @Operation(
            summary = "Criar usuário",
            description = "Cadastra um usuário ativo e armazena a senha de forma segura."
    )
    ResponseEntity<UsuarioResponse> criar(CriarUsuarioRequest request);

    @Operation(
            summary = "Consultar usuário por identificador",
            description = "Retorna os dados públicos e o perfil do usuário."
    )
    UsuarioResponse buscarPorId(Long usuarioId);
}
