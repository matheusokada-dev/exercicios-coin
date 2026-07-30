package br.com.gestaonumerario.api.adapter.output.security;

import br.com.gestaonumerario.api.adapter.input.auth.UsuarioAutenticado;
import br.com.gestaonumerario.api.core.domain.enums.PerfilUsuario;
import br.com.gestaonumerario.api.core.domain.model.TokenAcesso;
import br.com.gestaonumerario.api.core.domain.model.Usuario;
import br.com.gestaonumerario.api.port.output.TokenJwtOutputPort;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenService implements TokenJwtOutputPort {
    private final SecretKey key;
    private final long expirationMinutes;

    public JwtTokenService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expiration-minutes}") long expirationMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    @Override
    public TokenAcesso gerar(Usuario usuario) {

        Instant agora = Instant.now();

        Instant expiraEm = agora.plusSeconds(expirationMinutes * 60);

        String token = Jwts.builder()
                .subject(usuario.getLogin())
                .claim(
                        "uid",
                        usuario.getId()
                )
                .claim(
                        "perfil",
                        usuario.getPerfil()
                                .name()
                )
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiraEm))
                .signWith(key)
                .compact();
        return new TokenAcesso(
                token,
                expiraEm
        );
    }

    public UsuarioAutenticado validar(String token) {
        var claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return new UsuarioAutenticado(
                claims.get(
                        "uid",
                        Long.class
                ),
                claims.getSubject(),
                PerfilUsuario.valueOf(
                        claims.get(
                                "perfil",
                                String.class
                        )
                )
        );
    }
}
