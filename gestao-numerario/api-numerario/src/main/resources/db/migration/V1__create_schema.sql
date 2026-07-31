CREATE TABLE usuario (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(120) NOT NULL,
    login VARCHAR(80) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    perfil VARCHAR(20) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    tentativas_login_falhas INT NOT NULL DEFAULT 0,
    bloqueado_ate TIMESTAMP(6) NULL,

    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uk_usuario_login UNIQUE (login),
    CONSTRAINT ck_usuario_perfil CHECK (perfil IN ('OPERADOR', 'GESTOR')),
    CONSTRAINT ck_usuario_tentativas_login_falhas
        CHECK (tentativas_login_falhas >= 0)
) ENGINE = InnoDB;

CREATE TABLE agencia (
    id BIGINT NOT NULL AUTO_INCREMENT,
    codigo VARCHAR(10) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    saldo_atual DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    limite_minimo DECIMAL(19, 2) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    atualizado_em TIMESTAMP(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_agencia PRIMARY KEY (id),
    CONSTRAINT uk_agencia_codigo UNIQUE (codigo),
    CONSTRAINT ck_agencia_saldo CHECK (saldo_atual >= 0),
    CONSTRAINT ck_agencia_limite CHECK (limite_minimo >= 0)
) ENGINE = InnoDB;

CREATE INDEX ix_agencia_ativo_saldo_limite
    ON agencia (ativo, saldo_atual, limite_minimo);

CREATE TABLE solicitacao_numerario (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tipo_operacao VARCHAR(20) NOT NULL,
    agencia_id BIGINT NOT NULL,
    origem_agencia_id BIGINT NULL,
    destino_agencia_id BIGINT NULL,
    valor_solicitado DECIMAL(19, 2) NOT NULL,
    motivo VARCHAR(500) NOT NULL,
    data_desejada DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    solicitante_id BIGINT NOT NULL,
    decisor_id BIGINT NULL,
    justificativa_decisao VARCHAR(500) NULL,
    data_criacao TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    data_decisao TIMESTAMP(6) NULL,
    data_conclusao TIMESTAMP(6) NULL,
    cancelado_por_id BIGINT NULL,
    justificativa_cancelamento VARCHAR(500) NULL,
    data_cancelamento TIMESTAMP(6) NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    agencia_aberta_id BIGINT GENERATED ALWAYS AS (
        CASE
            WHEN status IN (
                'PENDENTE',
                'APROVADA',
                'EM_EXECUCAO',
                'COM_DIVERGENCIA'
            )
            THEN agencia_id
            ELSE NULL
        END
    ) STORED,

    CONSTRAINT pk_solicitacao_numerario PRIMARY KEY (id),
    CONSTRAINT uk_solicitacao_aberta_por_agencia UNIQUE (agencia_aberta_id),
    CONSTRAINT fk_solicitacao_agencia
        FOREIGN KEY (agencia_id) REFERENCES agencia (id),
    CONSTRAINT fk_solicitacao_origem_agencia
        FOREIGN KEY (origem_agencia_id) REFERENCES agencia (id),
    CONSTRAINT fk_solicitacao_destino_agencia
        FOREIGN KEY (destino_agencia_id) REFERENCES agencia (id),
    CONSTRAINT fk_solicitacao_solicitante
        FOREIGN KEY (solicitante_id) REFERENCES usuario (id),
    CONSTRAINT fk_solicitacao_decisor
        FOREIGN KEY (decisor_id) REFERENCES usuario (id),
    CONSTRAINT fk_solicitacao_cancelado_por
        FOREIGN KEY (cancelado_por_id) REFERENCES usuario (id),
    CONSTRAINT ck_solicitacao_tipo
        CHECK (tipo_operacao IN ('SUPRIMENTO', 'RECOLHIMENTO')),
    CONSTRAINT ck_solicitacao_status
        CHECK (status IN (
            'PENDENTE',
            'APROVADA',
            'REJEITADA',
            'EM_EXECUCAO',
            'CONCLUIDA',
            'CANCELADA',
            'COM_DIVERGENCIA'
        )),
    CONSTRAINT ck_solicitacao_valor CHECK (valor_solicitado > 0),
    CONSTRAINT ck_solicitacao_rota
        CHECK (
            origem_agencia_id IS NULL
            OR destino_agencia_id IS NULL
            OR origem_agencia_id <> destino_agencia_id
        ),
    CONSTRAINT ck_solicitacao_cancelamento
        CHECK (
            (
                status = 'CANCELADA'
                AND cancelado_por_id IS NOT NULL
                AND justificativa_cancelamento IS NOT NULL
                AND data_cancelamento IS NOT NULL
            )
            OR
            (
                status <> 'CANCELADA'
                AND cancelado_por_id IS NULL
                AND justificativa_cancelamento IS NULL
                AND data_cancelamento IS NULL
            )
        )
) ENGINE = InnoDB;

CREATE INDEX ix_solicitacao_agencia_tipo_status
    ON solicitacao_numerario (agencia_id, tipo_operacao, status);

CREATE INDEX ix_solicitacao_rota
    ON solicitacao_numerario (origem_agencia_id, destino_agencia_id);

CREATE INDEX ix_solicitacao_status_data
    ON solicitacao_numerario (status, data_criacao);

CREATE TABLE operacao_numerario (
    id BIGINT NOT NULL AUTO_INCREMENT,
    solicitacao_id BIGINT NOT NULL,
    origem_agencia_id BIGINT NOT NULL,
    destino_agencia_id BIGINT NOT NULL,
    status VARCHAR(25) NOT NULL,
    valor_programado DECIMAL(19, 2) NOT NULL,
    valor_expedido DECIMAL(19, 2) NULL,
    valor_recebido DECIMAL(19, 2) NULL,
    valor_divergencia DECIMAL(19, 2) NULL,
    programado_por_id BIGINT NOT NULL,
    expedido_por_id BIGINT NULL,
    recebido_por_id BIGINT NULL,
    conciliado_por_id BIGINT NULL,
    data_programacao TIMESTAMP(6) NOT NULL,
    data_expedicao TIMESTAMP(6) NULL,
    data_recebimento TIMESTAMP(6) NULL,
    data_conciliacao TIMESTAMP(6) NULL,
    justificativa_divergencia VARCHAR(500) NULL,
    descricao_ocorrencia VARCHAR(500) NULL,
    idempotency_key VARCHAR(80) NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT pk_operacao_numerario PRIMARY KEY (id),
    CONSTRAINT uk_operacao_solicitacao UNIQUE (solicitacao_id),
    CONSTRAINT uk_operacao_idempotencia UNIQUE (idempotency_key),
    CONSTRAINT fk_operacao_solicitacao
        FOREIGN KEY (solicitacao_id) REFERENCES solicitacao_numerario (id),
    CONSTRAINT fk_operacao_origem_agencia
        FOREIGN KEY (origem_agencia_id) REFERENCES agencia (id),
    CONSTRAINT fk_operacao_destino_agencia
        FOREIGN KEY (destino_agencia_id) REFERENCES agencia (id),
    CONSTRAINT fk_operacao_programado_por
        FOREIGN KEY (programado_por_id) REFERENCES usuario (id),
    CONSTRAINT fk_operacao_expedido_por
        FOREIGN KEY (expedido_por_id) REFERENCES usuario (id),
    CONSTRAINT fk_operacao_recebido_por
        FOREIGN KEY (recebido_por_id) REFERENCES usuario (id),
    CONSTRAINT fk_operacao_conciliado_por
        FOREIGN KEY (conciliado_por_id) REFERENCES usuario (id),
    CONSTRAINT ck_operacao_status
        CHECK (status IN (
            'PROGRAMADA',
            'EM_SEPARACAO',
            'EM_TRANSITO',
            'RECEBIDA',
            'CONCILIADA',
            'COM_DIVERGENCIA'
        )),
    CONSTRAINT ck_operacao_valor_programado CHECK (valor_programado > 0),
    CONSTRAINT ck_operacao_rota
        CHECK (origem_agencia_id <> destino_agencia_id),
    CONSTRAINT ck_operacao_valor_expedido
        CHECK (valor_expedido IS NULL OR valor_expedido = valor_programado),
    CONSTRAINT ck_operacao_valor_recebido
        CHECK (
            valor_recebido IS NULL
            OR (
                valor_recebido > 0
                AND valor_expedido IS NOT NULL
                AND valor_recebido <= valor_expedido
            )
        ),
    CONSTRAINT ck_operacao_valor_divergencia
        CHECK (valor_divergencia IS NULL OR valor_divergencia >= 0)
) ENGINE = InnoDB;

CREATE INDEX ix_operacao_status_programacao
    ON operacao_numerario (status, data_programacao);

CREATE INDEX ix_operacao_rota
    ON operacao_numerario (origem_agencia_id, destino_agencia_id);

CREATE TABLE movimentacao (
    id BIGINT NOT NULL AUTO_INCREMENT,
    agencia_id BIGINT NOT NULL,
    solicitacao_id BIGINT NULL,
    operacao_id BIGINT NULL,
    tipo VARCHAR(25) NOT NULL,
    entrada BOOLEAN NOT NULL,
    valor DECIMAL(19, 2) NOT NULL,
    saldo_anterior DECIMAL(19, 2) NOT NULL,
    saldo_posterior DECIMAL(19, 2) NOT NULL,
    descricao VARCHAR(500) NULL,
    data_movimento TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    usuario_id BIGINT NOT NULL,
    idempotency_key VARCHAR(80) NULL,

    CONSTRAINT pk_movimentacao PRIMARY KEY (id),
    CONSTRAINT uk_movimentacao_idempotencia UNIQUE (idempotency_key),
    CONSTRAINT fk_movimentacao_agencia
        FOREIGN KEY (agencia_id) REFERENCES agencia (id),
    CONSTRAINT fk_movimentacao_solicitacao
        FOREIGN KEY (solicitacao_id) REFERENCES solicitacao_numerario (id),
    CONSTRAINT fk_movimentacao_operacao
        FOREIGN KEY (operacao_id) REFERENCES operacao_numerario (id),
    CONSTRAINT fk_movimentacao_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT ck_movimentacao_tipo
        CHECK (tipo IN (
            'ABASTECIMENTO',
            'RECOLHIMENTO',
            'SAQUE',
            'DEPOSITO',
            'AJUSTE',
            'SAIDA_PARA_TRANSITO',
            'ENTRADA_DE_TRANSITO',
            'AJUSTE_DIVERGENCIA'
        )),
    CONSTRAINT ck_movimentacao_valor CHECK (valor > 0),
    CONSTRAINT ck_movimentacao_saldo CHECK (
        (entrada AND saldo_posterior = saldo_anterior + valor)
        OR
        (NOT entrada AND saldo_posterior = saldo_anterior - valor)
    )
) ENGINE = InnoDB;

CREATE INDEX ix_movimentacao_tipo_data
    ON movimentacao (tipo, data_movimento);

CREATE INDEX ix_movimentacao_agencia_data
    ON movimentacao (agencia_id, data_movimento);

CREATE INDEX ix_movimentacao_operacao
    ON movimentacao (operacao_id);

CREATE TABLE historico_solicitacao_numerario (
    id BIGINT NOT NULL AUTO_INCREMENT,
    solicitacao_id BIGINT NOT NULL,
    operacao_id BIGINT NULL,
    evento VARCHAR(40) NOT NULL,
    status_anterior VARCHAR(25) NULL,
    status_novo VARCHAR(25) NOT NULL,
    usuario_id BIGINT NOT NULL,
    data_evento TIMESTAMP(6) NOT NULL,
    justificativa VARCHAR(500) NULL,
    dados_complementares JSON NULL,

    CONSTRAINT pk_historico_solicitacao_numerario PRIMARY KEY (id),
    CONSTRAINT fk_historico_solicitacao
        FOREIGN KEY (solicitacao_id) REFERENCES solicitacao_numerario (id),
    CONSTRAINT fk_historico_operacao
        FOREIGN KEY (operacao_id) REFERENCES operacao_numerario (id),
    CONSTRAINT fk_historico_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id)
) ENGINE = InnoDB;

CREATE INDEX ix_historico_solicitacao_data
    ON historico_solicitacao_numerario (solicitacao_id, data_evento, id);

CREATE TABLE comando_idempotente (
    id BIGINT NOT NULL AUTO_INCREMENT,
    idempotency_key VARCHAR(80) NOT NULL,
    tipo_comando VARCHAR(40) NOT NULL,
    chave_execucao_unica VARCHAR(40) NULL,
    operacao_id BIGINT NULL,
    usuario_id BIGINT NOT NULL,
    data_processamento TIMESTAMP(6) NOT NULL,

    CONSTRAINT pk_comando_idempotente PRIMARY KEY (id),
    CONSTRAINT uk_comando_idempotente_key UNIQUE (idempotency_key),
    CONSTRAINT uk_comando_execucao_unica UNIQUE (chave_execucao_unica),
    CONSTRAINT fk_comando_idempotente_operacao
        FOREIGN KEY (operacao_id) REFERENCES operacao_numerario (id),
    CONSTRAINT fk_comando_idempotente_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id)
) ENGINE = InnoDB;

CREATE INDEX ix_comando_idempotente_operacao
    ON comando_idempotente (operacao_id, data_processamento);
