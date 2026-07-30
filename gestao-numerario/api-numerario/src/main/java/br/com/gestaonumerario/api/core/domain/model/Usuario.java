package br.com.gestaonumerario.api.core.domain.model;

import br.com.gestaonumerario.api.core.domain.enums.PerfilUsuario;
import br.com.gestaonumerario.api.core.exception.CampoObrigatorioException;
import java.time.Duration;
import java.time.Instant;

public class Usuario {

    private final Long id;
    private String nome;
    private final String login;
    private String senhaHash;
    private PerfilUsuario perfil;
    private boolean ativo;
    private final Instant criadoEm;
    private int tentativasLoginFalhas;
    private Instant bloqueadoAte;

    public Usuario(
            Long id,
            String nome,
            String login,
            String senhaHash,
            PerfilUsuario perfil,
            boolean ativo,
            Instant criadoEm,
            int tentativasLoginFalhas,
            Instant bloqueadoAte) {
        this.id = id;
        this.nome = textoObrigatorio(nome);
        this.login = textoObrigatorio(login);
        this.senhaHash = textoObrigatorio(senhaHash);
        this.perfil = perfilObrigatorio(perfil);
        this.ativo = ativo;
        this.criadoEm = criadoEm == null ? Instant.now() : criadoEm;
        this.tentativasLoginFalhas = Math.max(
                0,
                tentativasLoginFalhas
        );
        this.bloqueadoAte = bloqueadoAte;
    }

    public void atualizarNome(String nome) {
        this.nome = textoObrigatorio(nome);
    }

    public void alterarPerfil(PerfilUsuario perfil) {
        this.perfil = perfilObrigatorio(perfil);
    }

    public void alterarSenhaHash(String senhaHash) {
        this.senhaHash = textoObrigatorio(senhaHash);
    }

    public void ativar() {
        ativo = true;
    }

    public void desativar() {
        ativo = false;
    }

    public boolean estaBloqueado(Instant agora) {
        return bloqueadoAte != null && agora.isBefore(bloqueadoAte);
    }

    public void registrarFalhaLogin(Instant agora, int limiteTentativas, Duration duracaoBloqueio) {
        if (bloqueadoAte != null && !agora.isBefore(bloqueadoAte)) {
            limparTentativasLogin();
        }

        tentativasLoginFalhas++;
        if (tentativasLoginFalhas >= limiteTentativas) {
            bloqueadoAte = agora.plus(duracaoBloqueio);
        }
    }

    public void limparTentativasLogin() {
        tentativasLoginFalhas = 0;
        bloqueadoAte = null;
    }

    public boolean possuiFalhasLogin() {
        return tentativasLoginFalhas > 0 || bloqueadoAte != null;
    }

    public int tentativasLoginRestantes(int limiteTentativas) {
        return Math.max(
                0,
                limiteTentativas - tentativasLoginFalhas
        );
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getLogin() {
        return login;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public int getTentativasLoginFalhas() {
        return tentativasLoginFalhas;
    }

    public Instant getBloqueadoAte() {
        return bloqueadoAte;
    }

    private static String textoObrigatorio(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new CampoObrigatorioException();
        }

        return valor.trim();
    }

    private static PerfilUsuario perfilObrigatorio(PerfilUsuario perfil) {
        if (perfil == null) {
            throw new CampoObrigatorioException();
        }

        return perfil;
    }
}
