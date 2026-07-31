package br.com.gestaonumerario.api.adapter.output.persistence;

import br.com.gestaonumerario.api.port.output.TransacaoOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@RequiredArgsConstructor
public class SpringTransacaoAdapter implements TransacaoOutputPort {

    private final PlatformTransactionManager transactionManager;

    @Override
    public <T> T executar(AcaoTransacional<T> acao) {
        return new TransactionTemplate(transactionManager).execute(status -> acao.executar());
    }
}
