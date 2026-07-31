package br.com.gestaonumerario.api.port.input;

import br.com.gestaonumerario.api.core.domain.model.OperacaoNumerario;
import br.com.gestaonumerario.api.core.domain.model.command.ConciliarOperacaoNumerarioCommand;
import br.com.gestaonumerario.api.core.domain.model.command.ExecutarOperacaoNumerarioCommand;
import br.com.gestaonumerario.api.core.domain.model.command.ProgramarOperacaoNumerarioCommand;
import br.com.gestaonumerario.api.core.domain.model.command.ReceberOperacaoNumerarioCommand;
import br.com.gestaonumerario.api.core.domain.model.command.RegistrarOcorrenciaOperacaoCommand;

public interface OperacaoNumerarioInputPort {
    OperacaoNumerario programar(ProgramarOperacaoNumerarioCommand command);

    OperacaoNumerario iniciarSeparacao(ExecutarOperacaoNumerarioCommand command);

    OperacaoNumerario expedir(ExecutarOperacaoNumerarioCommand command);

    OperacaoNumerario registrarOcorrencia(RegistrarOcorrenciaOperacaoCommand command);

    OperacaoNumerario receber(ReceberOperacaoNumerarioCommand command);

    OperacaoNumerario conciliar(ConciliarOperacaoNumerarioCommand command);
}
