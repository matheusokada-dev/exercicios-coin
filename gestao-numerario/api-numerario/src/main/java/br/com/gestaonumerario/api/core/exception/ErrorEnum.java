package br.com.gestaonumerario.api.core.exception;

public enum ErrorEnum {

    ERRO_GENERICO(500, 1,
            "Algo deu errado. Tente novamente mais tarde."),
    CAMPO_OBRIGATORIO(400, 1000,
            "Campo obrigatório."),
    CAMPO_INVALIDO(400, 1006,
            "Um ou mais campos informados são inválidos."),
    VALOR_MONETARIO_OBRIGATORIO(400, 1001,
            "Valor monetário é obrigatório."),
    VALOR_DEVE_SER_MAIOR_QUE_ZERO(400, 1002,
            "O valor deve ser maior que zero."),
    VALOR_NAO_PODE_SER_NEGATIVO(400, 1003,
            "O valor não pode ser negativo."),
    SALDO_INSUFICIENTE(422, 2000,
            "A operação não pode deixar o saldo da agência negativo."),
    DATA_DESEJADA_NO_PASSADO(400, 1004,
            "A data desejada não pode estar no passado."),
    JUSTIFICATIVA_OBRIGATORIA(400, 1005,
            "A justificativa é obrigatória."),
    APENAS_GESTOR_PODE_DECIDIR(403, 2001,
            "Apenas um gestor pode aprovar ou rejeitar solicitações."),
    AUTO_APROVACAO_NAO_PERMITIDA(422, 2002,
            "O solicitante não pode aprovar a própria solicitação."),
    JUSTIFICATIVA_ESPECIAL_OBRIGATORIA(422, 2003,
            "Solicitações acima de R$ 500.000 exigem justificativa especial."),
    TRANSICAO_STATUS_INVALIDA(409, 2004,
            "A transição de status da solicitação não é permitida."),
    AGENCIA_NAO_ENCONTRADA(404, 3000,
            "Agência não encontrada."),
    USUARIO_NAO_ENCONTRADO(404, 3001,
            "Usuário não encontrado."),
    SOLICITACAO_NAO_ENCONTRADA(404, 3002,
            "Solicitação não encontrada."),
    SOLICITACAO_ABERTA_DUPLICADA(409, 3003,
            "Já existe uma solicitação aberta para esta agência."),
    IDEMPOTENCY_KEY_DUPLICADA(409, 3004,
            "Esta operação já foi processada."),
    CODIGO_AGENCIA_JA_CADASTRADO(409, 3005,
            "Já existe uma agência com este código."),
    LOGIN_JA_CADASTRADO(409, 3006,
            "Já existe um usuário com este login."),
    TIPO_MOVIMENTACAO_NAO_PERMITIDO(400, 3007,
            "Este tipo de movimentação não pode ser registrado manualmente."),
    PERIODO_CONSULTA_INVALIDO(400, 3008,
            "A data final não pode ser anterior à data inicial."),
    CREDENCIAIS_INVALIDAS(401, 3009,
            "Login ou senha inválidos."),
    REGRA_OPERACAO_NUMERARIO_VIOLADA(422, 4000,
            "A operação de numerário viola uma regra de negócio."),
    CONFLITO_VERSAO(409, 4001,
            "O registro foi alterado por outra operação."),
    API_V1_SOMENTE_CONSULTA(410, 4002,
            "Operação indisponível para este contrato.");

    private final int httpStatus;
    private final int errorCode;
    private final String errorMessage;

    ErrorEnum(int httpStatus, int errorCode, String errorMessage) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getHttpStatus() { return httpStatus; }
    public int getErrorCode() { return errorCode; }
    public String getErrorMessage() { return errorMessage; }
}
