CREATE TABLE usuario (
                         id         BIGINT          NOT NULL AUTO_INCREMENT,
                         nome       VARCHAR(120)    NOT NULL,
                         login      VARCHAR(80)     NOT NULL,
                         senha_hash VARCHAR(255)    NOT NULL,
                         perfil     VARCHAR(20)     NOT NULL,
                         ativo      BOOLEAN         NOT NULL DEFAULT TRUE,
                         criado_em  TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

                         CONSTRAINT pk_usuario PRIMARY KEY (id),
                         CONSTRAINT uk_usuario_login UNIQUE (login),
                         CONSTRAINT ck_usuario_perfil CHECK (perfil IN ('OPERADOR', 'GESTOR'))
) ENGINE = InnoDB;

CREATE TABLE agencia (
                         id BIGINT NOT NULL AUTO_INCREMENT,
                         codigo VARCHAR(10) NOT NULL,
                         nome VARCHAR(120) NOT NULL,
                         cidade VARCHAR(100) NOT NULL,
                         saldo_atual DECIMAL(19,2) NOT NULL,
                         limite_minimo DECIMAL(19,2) NOT NULL,
                         ativo BOOLEAN         NOT NULL DEFAULT TRUE,
                         versao BIGINT          NOT NULL DEFAULT 0,
                         criado_em TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                         atualizado_em TIMESTAMP(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
                             ON UPDATE CURRENT_TIMESTAMP(6),

                         CONSTRAINT pk_agencia PRIMARY KEY (id),
                         CONSTRAINT uk_agencia_codigo UNIQUE (codigo),
                         CONSTRAINT ck_agencia_saldo CHECK (saldo_atual >= 0),
                         CONSTRAINT ck_agencia_limite CHECK (limite_minimo >= 0)
) ENGINE = InnoDB;

CREATE TABLE solicitacao_abastecimento (
                                           id BIGINT NOT NULL AUTO_INCREMENT,
                                           agencia_id BIGINT NOT NULL,
                                           valor DECIMAL(19,2) NOT NULL,
                                           motivo VARCHAR(500) NOT NULL,
                                           data_desejada DATE NOT NULL,
                                           status VARCHAR(20) NOT NULL,
                                           solicitante_id BIGINT NOT NULL,
                                           decisor_id BIGINT NULL,
                                           justificativa_decisao VARCHAR(500) NULL,
                                           justificativa_especial VARCHAR(500) NULL,
                                           data_criacao TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                           data_decisao TIMESTAMP(6) NULL,
                                           data_atendimento TIMESTAMP(6) NULL,
                                           versao BIGINT NOT NULL DEFAULT 0,

                                           agencia_aberta_id BIGINT GENERATED ALWAYS AS (
                                               CASE
                                                   WHEN status IN ('PENDENTE', 'APROVADA') THEN agencia_id
                                                   ELSE NULL
                                                   END
                                               ) STORED,

                                           CONSTRAINT pk_solicitacao_abastecimento PRIMARY KEY (id),
                                           CONSTRAINT fk_solicitacao_agencia
                                               FOREIGN KEY (agencia_id) REFERENCES agencia (id),
                                           CONSTRAINT fk_solicitacao_solicitante
                                               FOREIGN KEY (solicitante_id) REFERENCES usuario (id),
                                           CONSTRAINT fk_solicitacao_decisor
                                               FOREIGN KEY (decisor_id) REFERENCES usuario (id),
                                           CONSTRAINT ck_solicitacao_valor CHECK (valor > 0),
                                           CONSTRAINT ck_solicitacao_status
                                               CHECK (status IN ('PENDENTE', 'APROVADA', 'REJEITADA', 'ATENDIDA')),
                                           CONSTRAINT uk_solicitacao_aberta_por_agencia UNIQUE (agencia_aberta_id)
) ENGINE = InnoDB;

CREATE TABLE movimentacao (
                              id BIGINT NOT NULL AUTO_INCREMENT,
                              agencia_id BIGINT NOT NULL,
                              solicitacao_id BIGINT NULL,
                              tipo VARCHAR(25) NOT NULL,
                              valor DECIMAL(19,2) NOT NULL,
                              saldo_anterior DECIMAL(19,2) NOT NULL,
                              saldo_posterior DECIMAL(19,2) NOT NULL,
                              descricao VARCHAR(500) NULL,
                              data_movimento TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                              usuario_id BIGINT NOT NULL,
                              idempotency_key VARCHAR(80) NULL,

                              CONSTRAINT pk_movimentacao PRIMARY KEY (id),
                              CONSTRAINT fk_movimentacao_agencia
                                  FOREIGN KEY (agencia_id) REFERENCES agencia (id),
                              CONSTRAINT fk_movimentacao_solicitacao
                                  FOREIGN KEY (solicitacao_id) REFERENCES solicitacao_abastecimento (id),
                              CONSTRAINT fk_movimentacao_usuario
                                  FOREIGN KEY (usuario_id) REFERENCES usuario (id),
                              CONSTRAINT ck_movimentacao_valor CHECK (valor > 0),
                              CONSTRAINT ck_movimentacao_tipo
                                  CHECK (tipo IN (
                                                  'ABASTECIMENTO',
                                                  'RECOLHIMENTO',
                                                  'SAQUE',
                                                  'DEPOSITO',
                                                  'AJUSTE'
                                      )),
                              CONSTRAINT uk_movimentacao_idempotencia UNIQUE (idempotency_key)
) ENGINE = InnoDB;

CREATE INDEX ix_agencia_alerta
    ON agencia (ativo, saldo_atual, limite_minimo);

CREATE INDEX ix_movimentacao_agencia_data
    ON movimentacao (agencia_id, data_movimento);

CREATE INDEX ix_movimentacao_tipo_data
    ON movimentacao (tipo, data_movimento);

CREATE INDEX ix_solicitacao_status_data
    ON solicitacao_abastecimento (status, data_criacao);

CREATE INDEX ix_solicitacao_agencia_status
    ON solicitacao_abastecimento (agencia_id, status);