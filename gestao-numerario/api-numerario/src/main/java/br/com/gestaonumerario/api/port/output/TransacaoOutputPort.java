package br.com.gestaonumerario.api.port.output;

public interface TransacaoOutputPort {

    <T> T executar(AcaoTransacional<T> acao);

    @FunctionalInterface
    interface AcaoTransacional<T> {
        T executar();
    }
}
