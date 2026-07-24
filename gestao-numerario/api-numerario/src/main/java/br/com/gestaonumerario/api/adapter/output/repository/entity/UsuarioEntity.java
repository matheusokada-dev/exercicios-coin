package br.com.gestaonumerario.api.adapter.output.repository.entity;

import br.com.gestaonumerario.api.core.domain.enums.PerfilUsuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@Table(name = "usuario")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 80)
    private String login;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PerfilUsuario perfil;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "tentativas_login_falhas", nullable = false)
    private int tentativasLoginFalhas;

    @Column(name = "bloqueado_ate")
    private Instant bloqueadoAte;

    public UsuarioEntity(
            Long id,
            String nome,
            String login,
            String senhaHash,
            PerfilUsuario perfil,
            boolean ativo,
            Instant criadoEm,
            int tentativasLoginFalhas,
            Instant bloqueadoAte
    ) {
        this.id = id;
        this.nome = nome;
        this.login = login;
        this.senhaHash = senhaHash;
        this.perfil = perfil;
        this.ativo = ativo;
        this.criadoEm = criadoEm;
        this.tentativasLoginFalhas = tentativasLoginFalhas;
        this.bloqueadoAte = bloqueadoAte;
    }
}
