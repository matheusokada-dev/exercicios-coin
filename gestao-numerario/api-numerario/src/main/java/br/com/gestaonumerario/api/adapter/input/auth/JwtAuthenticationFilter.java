package br.com.gestaonumerario.api.adapter.input.auth;

import br.com.gestaonumerario.api.adapter.output.security.JwtTokenService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenService jwtTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }
        try {
            UsuarioAutenticado usuario = jwtTokenService.validar(header.substring(7));
            var auth = new UsernamePasswordAuthenticationToken(usuario, null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + usuario.perfil().name())));
            SecurityContextHolder.getContext().setAuthentication(auth);
            chain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token invÃ¡lido ou expirado.");
        }
    }
}

