package br.com.gestaonumerario.api.adapter.input.controller;

import br.com.gestaonumerario.api.adapter.input.contract.UsuarioApi;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.CriarUsuarioRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.UsuarioResponse;
import br.com.gestaonumerario.api.adapter.input.mapper.UsuarioRestMapper;
import br.com.gestaonumerario.api.port.input.UsuarioInputPort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController implements UsuarioApi {

    private final UsuarioInputPort usuarioUseCase;
    private final UsuarioRestMapper mapper;

    @PostMapping
    @Override
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody CriarUsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(usuarioUseCase.criar(mapper.toCommand(request))));
    }

    @GetMapping("/{usuarioId}")
    @Override
    public UsuarioResponse buscarPorId(@PathVariable Long usuarioId) {
        return mapper.toResponse(usuarioUseCase.buscarPorId(usuarioId));
    }
}
