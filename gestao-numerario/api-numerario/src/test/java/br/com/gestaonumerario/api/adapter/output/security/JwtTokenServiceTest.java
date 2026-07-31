package br.com.gestaonumerario.api.adapter.output.security;

import br.com.gestaonumerario.api.core.domain.enums.PerfilUsuario;
import br.com.gestaonumerario.api.core.domain.model.Usuario;
import io.jsonwebtoken.JwtException;
import java.time.Instant;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {

    private static final String SECRET = "segredo-local-com-mais-de-trinta-e-dois-caracteres";

    @Test
    void geraEValidaAccessTokenComIdentidadeEPerfil() {
        var service = new JwtTokenService(
                SECRET,
                480
        );
        var usuario = new Usuario(
                7L,
                "Gestor",
                "gestor",
                "hash",
                PerfilUsuario.GESTOR,
                true,
                Instant.parse("2026-01-01T00:00:00Z"),
                0,
                null
        );

        var token = service.gerar(usuario);
        var autenticado = service.validar(token.valor());

        assertThat(token.valor()).isNotBlank();
        assertThat(token.expiraEm()).isAfter(
                Instant.now()
                        .plusSeconds(7 * 60 * 60)
        );
        assertThat(autenticado.id()).isEqualTo(7L);
        assertThat(autenticado.login()).isEqualTo("gestor");
        assertThat(autenticado.perfil()).isEqualTo(PerfilUsuario.GESTOR);
    }

    @Test
    void rejeitaTokenAssinadoComOutraChave() {
        var emissor = new JwtTokenService(
                SECRET,
                480
        );
        var validador = new JwtTokenService(
                "outra-chave-segura-com-mais-de-trinta-e-dois-caracteres",
                480
        );
        var usuario = new Usuario(
                7L,
                "Gestor",
                "gestor",
                "hash",
                PerfilUsuario.GESTOR,
                true,
                Instant.now(),
                0,
                null
        );

        var token = emissor.gerar(usuario);

        assertThatThrownBy(() -> validador.validar(token.valor())).isInstanceOf(JwtException.class);
    }
}
