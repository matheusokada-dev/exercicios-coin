package br.com.gestaonumerario.api.adapter.input.controller;

import br.com.gestaonumerario.api.adapter.input.mapper.UsuarioRestMapper;
import br.com.gestaonumerario.api.adapter.input.controller.dto.request.CriarUsuarioRequest;
import br.com.gestaonumerario.api.adapter.input.controller.dto.response.UsuarioResponse;
import br.com.gestaonumerario.api.port.input.UsuarioInputPort;
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
public class UsuarioController {

    private final UsuarioInputPort criarUseCase;
    private final UsuarioInputPort consultarUseCase;
    private final UsuarioRestMapper mapper;

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody CriarUsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(criarUseCase.criar(mapper.toCommand(request))));
    }

    @GetMapping("/{usuarioId}")
    public UsuarioResponse buscarPorId(@PathVariable Long usuarioId) {
        return mapper.toResponse(consultarUseCase.buscarPorId(usuarioId));
    }
}



