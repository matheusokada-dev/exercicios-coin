package br.com.gestaonumerario.api.port.output;

public interface CodificadorSenhaOutputPort {

    String codificar(String senha);

    boolean confere(String senha, String senhaHash);
}
