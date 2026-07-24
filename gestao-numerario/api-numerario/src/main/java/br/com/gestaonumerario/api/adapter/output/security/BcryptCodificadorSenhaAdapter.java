package br.com.gestaonumerario.api.adapter.output.security;

import br.com.gestaonumerario.api.port.output.CodificadorSenhaOutputPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptCodificadorSenhaAdapter implements CodificadorSenhaOutputPort {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String codificar(String senha) {
        return passwordEncoder.encode(senha);
    }

    @Override
    public boolean confere(String senha, String senhaHash) {
        return passwordEncoder.matches(senha, senhaHash);
    }
}


