-- RASCUNHO PARA REVISAO - NAO EXECUTAR DIRETAMENTE.
-- Este arquivo fica fora de api-numerario/src/main/resources/db/migration
-- para impedir aplicacao automatica pelo Flyway.
--
-- Pre-condicoes:
-- 1. API e BFF parados.
-- 2. Backup logico completo e validado.
-- 3. Codigo compativel com o novo esquema pronto para subir junto da migration.
-- 4. Banco em MySQL 8.4.
--
-- Observacao: DDL no MySQL realiza commits implicitos. O backup e o mecanismo
-- real de recuperacao em caso de falha; este script nao promete rollback
-- transacional.

-- ---------------------------------------------------------------------------
-- 1. Unidades operacionais
-- ---------------------------------------------------------------------------

CREATE TABLE unidade_operacional (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tipo VARCHAR(20) NOT NULL,
    codigo VARCHAR(30) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    controla_saldo BOOLEAN NOT NULL,
    saldo_atual DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    atualizado_em TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_unidade_operacional PRIMARY KEY (id),
    CONSTRAINT uk_unidade_operacional_codigo UNIQUE (codigo),
    CONSTRAINT ck_unidade_operacional_tipo
        CHECK (tipo IN (
            'AGENCIA',
            'TESOURARIA',
            'BDN',
            'CUSTODIANTE',
            'PAB',
            'OUTRO'
        )),
    CONSTRAINT ck_unidade_operacional_saldo
        CHECK (saldo_atual >= 0),
    CONSTRAINT ck_unidade_sem_controle_saldo
        CHECK (controla_saldo OR saldo_atual = 0)
) ENGINE = InnoDB;

INSERT INTO unidade_operacional (
    tipo,
    codigo,
    nome,
    controla_saldo,
    saldo_atual,
    ativo
)
SELECT
    'AGENCIA',
    CONCAT('AGE-', codigo),
    nome,
    TRUE,
    saldo_atual,
    ativo
FROM agencia;

INSERT INTO unidade_operacional (
    tipo,
    codigo,
    nome,
    controla_saldo,
    saldo_atual,
    ativo
)
VALUES
    (
        'TESOURARIA',
        'TES-CENTRAL',
        'Tesouraria Central',
        TRUE,
        0.00,
        TRUE
    ),
    (
        'OUTRO',
        'LEGADO-ORIGEM',
        'Origem historica nao informada',
        FALSE,
        0.00,
        FALSE
    );

ALTER TABLE agencia
    ADD COLUMN unidade_operacional_id BIGINT NULL AFTER id;

UPDATE agencia a
JOIN unidade_operacional u
    ON u.tipo = 'AGENCIA'
   AND u.codigo COLLATE utf8mb4_unicode_ci =
       CONCAT('AGE-', a.codigo) COLLATE utf8mb4_unicode_ci
SET a.unidade_operacional_id = u.id;

ALTER TABLE agencia
    MODIFY COLUMN unidade_operacional_id BIGINT NOT NULL,
    ADD CONSTRAINT uk_agencia_unidade_operacional
        UNIQUE (unidade_operacional_id),
    ADD CONSTRAINT fk_agencia_unidade_operacional
        FOREIGN KEY (unidade_operacional_id)
        REFERENCES unidade_operacional (id);

-- O saldo passa a ter uma unica fonte: unidade_operacional.
DROP INDEX ix_agencia_alerta ON agencia;

ALTER TABLE agencia
    DROP COLUMN saldo_atual;

CREATE INDEX ix_unidade_operacional_tipo_ativo
    ON unidade_operacional (tipo, ativo);

CREATE INDEX ix_unidade_operacional_saldo
    ON unidade_operacional (ativo, saldo_atual);

CREATE INDEX ix_agencia_ativo_limite
    ON agencia (ativo, limite_minimo);

-- ---------------------------------------------------------------------------
-- 2. Transformacao da solicitacao existente
-- ---------------------------------------------------------------------------

RENAME TABLE solicitacao_abastecimento TO solicitacao_numerario;

ALTER TABLE solicitacao_numerario
    DROP INDEX uk_solicitacao_aberta_por_agencia,
    DROP COLUMN agencia_aberta_id,
    DROP CHECK ck_solicitacao_valor,
    DROP CHECK ck_solicitacao_status;

ALTER TABLE solicitacao_numerario
    RENAME COLUMN agencia_id TO agencia_referencia_id,
    RENAME COLUMN valor TO valor_solicitado,
    RENAME COLUMN data_atendimento TO data_conclusao,
    ADD COLUMN tipo_operacao VARCHAR(20) NULL AFTER id,
    ADD COLUMN origem_id BIGINT NULL AFTER agencia_referencia_id,
    ADD COLUMN destino_id BIGINT NULL AFTER origem_id,
    ADD COLUMN cancelado_por_id BIGINT NULL AFTER data_conclusao,
    ADD COLUMN justificativa_cancelamento VARCHAR(500) NULL
        AFTER cancelado_por_id,
    ADD COLUMN data_cancelamento TIMESTAMP(6) NULL
        AFTER justificativa_cancelamento;

UPDATE solicitacao_numerario
SET tipo_operacao = 'SUPRIMENTO';

UPDATE solicitacao_numerario s
JOIN agencia a ON a.id = s.agencia_referencia_id
SET s.destino_id = a.unidade_operacional_id;

UPDATE solicitacao_numerario
SET origem_id = (
        SELECT id
        FROM unidade_operacional
        WHERE codigo = 'LEGADO-ORIGEM'
    )
WHERE status = 'ATENDIDA';

UPDATE solicitacao_numerario
SET status = 'CONCLUIDA'
WHERE status = 'ATENDIDA';

ALTER TABLE solicitacao_numerario
    MODIFY COLUMN tipo_operacao VARCHAR(20) NOT NULL,
    ADD CONSTRAINT fk_solicitacao_origem
        FOREIGN KEY (origem_id) REFERENCES unidade_operacional (id),
    ADD CONSTRAINT fk_solicitacao_destino
        FOREIGN KEY (destino_id) REFERENCES unidade_operacional (id),
    ADD CONSTRAINT fk_solicitacao_cancelado_por
        FOREIGN KEY (cancelado_por_id) REFERENCES usuario (id);

-- ---------------------------------------------------------------------------
-- 3. Operacao logistica unica
-- ---------------------------------------------------------------------------

CREATE TABLE operacao_numerario (
    id BIGINT NOT NULL AUTO_INCREMENT,
    solicitacao_id BIGINT NOT NULL,
    origem_id BIGINT NOT NULL,
    destino_id BIGINT NOT NULL,
    status VARCHAR(25) NOT NULL,
    valor_programado DECIMAL(19,2) NOT NULL,
    valor_expedido DECIMAL(19,2) NULL,
    valor_recebido DECIMAL(19,2) NULL,
    valor_divergencia DECIMAL(19,2) NULL,
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
    CONSTRAINT fk_operacao_origem
        FOREIGN KEY (origem_id) REFERENCES unidade_operacional (id),
    CONSTRAINT fk_operacao_destino
        FOREIGN KEY (destino_id) REFERENCES unidade_operacional (id),
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
    CONSTRAINT ck_operacao_rota
        CHECK (origem_id <> destino_id),
    CONSTRAINT ck_operacao_valor_programado
        CHECK (valor_programado > 0),
    CONSTRAINT ck_operacao_valor_expedido
        CHECK (
            valor_expedido IS NULL
            OR valor_expedido = valor_programado
        ),
    CONSTRAINT ck_operacao_valor_recebido
        CHECK (
            valor_recebido IS NULL
            OR (
                valor_recebido > 0
                AND valor_recebido <= valor_expedido
            )
        ),
    CONSTRAINT ck_operacao_valor_divergencia
        CHECK (
            valor_divergencia IS NULL
            OR valor_divergencia >= 0
        )
) ENGINE = InnoDB;

CREATE INDEX ix_operacao_status_programacao
    ON operacao_numerario (status, data_programacao);

-- Reconstrucao das 90 solicitacoes historicamente atendidas.
-- A origem desconhecida nao sofre qualquer debito retroativo.
INSERT INTO operacao_numerario (
    solicitacao_id,
    origem_id,
    destino_id,
    status,
    valor_programado,
    valor_expedido,
    valor_recebido,
    valor_divergencia,
    programado_por_id,
    expedido_por_id,
    recebido_por_id,
    conciliado_por_id,
    data_programacao,
    data_expedicao,
    data_recebimento,
    data_conciliacao,
    idempotency_key
)
SELECT
    s.id,
    s.origem_id,
    s.destino_id,
    'CONCILIADA',
    s.valor_solicitado,
    s.valor_solicitado,
    s.valor_solicitado,
    0.00,
    COALESCE(m.usuario_id, s.decisor_id, s.solicitante_id),
    COALESCE(m.usuario_id, s.decisor_id, s.solicitante_id),
    COALESCE(m.usuario_id, s.decisor_id, s.solicitante_id),
    COALESCE(m.usuario_id, s.decisor_id, s.solicitante_id),
    COALESCE(s.data_decisao, s.data_criacao),
    COALESCE(m.data_movimento, s.data_conclusao, s.data_decisao, s.data_criacao),
    COALESCE(s.data_conclusao, m.data_movimento, s.data_decisao, s.data_criacao),
    COALESCE(s.data_conclusao, m.data_movimento, s.data_decisao, s.data_criacao),
    CONCAT('MIGRACAO-LEGADO-', s.id)
FROM solicitacao_numerario s
LEFT JOIN (
    SELECT
        solicitacao_id,
        MIN(usuario_id) AS usuario_id,
        MIN(data_movimento) AS data_movimento
    FROM movimentacao
    WHERE solicitacao_id IS NOT NULL
      AND tipo = 'ABASTECIMENTO'
    GROUP BY solicitacao_id
) m ON m.solicitacao_id = s.id
WHERE s.status = 'CONCLUIDA';

-- ---------------------------------------------------------------------------
-- 4. Historico imutavel e eventos sinteticos
-- ---------------------------------------------------------------------------

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

    CONSTRAINT pk_historico_solicitacao PRIMARY KEY (id),
    CONSTRAINT fk_historico_solicitacao
        FOREIGN KEY (solicitacao_id) REFERENCES solicitacao_numerario (id),
    CONSTRAINT fk_historico_operacao
        FOREIGN KEY (operacao_id) REFERENCES operacao_numerario (id),
    CONSTRAINT fk_historico_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id)
) ENGINE = InnoDB;

CREATE INDEX ix_historico_solicitacao_data
    ON historico_solicitacao_numerario (solicitacao_id, data_evento, id);

-- Registro independente das chaves recebidas por cada comando mutável.
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

INSERT INTO historico_solicitacao_numerario (
    solicitacao_id,
    evento,
    status_anterior,
    status_novo,
    usuario_id,
    data_evento,
    justificativa,
    dados_complementares
)
SELECT
    s.id,
    'SOLICITACAO_CRIADA_LEGADO',
    NULL,
    'PENDENTE',
    s.solicitante_id,
    s.data_criacao,
    s.motivo,
    JSON_OBJECT(
        'migrado', TRUE,
        'tipoOperacao', 'SUPRIMENTO',
        'valorSolicitado', s.valor_solicitado,
        'agenciaReferenciaId', s.agencia_referencia_id
    )
FROM solicitacao_numerario s;

INSERT INTO historico_solicitacao_numerario (
    solicitacao_id,
    evento,
    status_anterior,
    status_novo,
    usuario_id,
    data_evento,
    justificativa,
    dados_complementares
)
SELECT
    s.id,
    CASE
        WHEN s.status = 'REJEITADA'
            THEN 'SOLICITACAO_REJEITADA_LEGADO'
        ELSE 'SOLICITACAO_APROVADA_LEGADO'
    END,
    'PENDENTE',
    CASE
        WHEN s.status = 'REJEITADA' THEN 'REJEITADA'
        ELSE 'APROVADA'
    END,
    COALESCE(s.decisor_id, s.solicitante_id),
    COALESCE(s.data_decisao, s.data_criacao),
    s.justificativa_decisao,
    JSON_OBJECT(
        'migrado', TRUE,
        'justificativaEspecialLegada', s.justificativa_especial
    )
FROM solicitacao_numerario s
WHERE s.status IN ('APROVADA', 'REJEITADA', 'CONCLUIDA');

INSERT INTO historico_solicitacao_numerario (
    solicitacao_id,
    operacao_id,
    evento,
    status_anterior,
    status_novo,
    usuario_id,
    data_evento,
    justificativa,
    dados_complementares
)
SELECT
    s.id,
    o.id,
    'SOLICITACAO_CONCLUIDA_LEGADO',
    'APROVADA',
    'CONCLUIDA',
    o.recebido_por_id,
    o.data_recebimento,
    'Registro reconstruido a partir do atendimento legado.',
    JSON_OBJECT(
        'migrado', TRUE,
        'origemNaoInformada', TRUE,
        'valorExpedido', o.valor_expedido,
        'valorRecebido', o.valor_recebido
    )
FROM solicitacao_numerario s
JOIN operacao_numerario o ON o.solicitacao_id = s.id
WHERE s.status = 'CONCLUIDA';

-- A justificativa especial legada foi preservada no historico JSON.
ALTER TABLE solicitacao_numerario
    DROP COLUMN justificativa_especial;

-- ---------------------------------------------------------------------------
-- 5. Constraints finais da solicitacao
-- ---------------------------------------------------------------------------

ALTER TABLE solicitacao_numerario
    ADD COLUMN agencia_aberta_id BIGINT
        GENERATED ALWAYS AS (
            CASE
                WHEN status IN (
                    'PENDENTE',
                    'APROVADA',
                    'EM_EXECUCAO',
                    'COM_DIVERGENCIA'
                )
                THEN agencia_referencia_id
                ELSE NULL
            END
        ) STORED,
    ADD CONSTRAINT uk_solicitacao_aberta_por_agencia
        UNIQUE (agencia_aberta_id),
    ADD CONSTRAINT ck_solicitacao_tipo
        CHECK (tipo_operacao IN ('SUPRIMENTO', 'RECOLHIMENTO')),
    ADD CONSTRAINT ck_solicitacao_valor
        CHECK (valor_solicitado > 0),
    ADD CONSTRAINT ck_solicitacao_status
        CHECK (status IN (
            'PENDENTE',
            'APROVADA',
            'REJEITADA',
            'EM_EXECUCAO',
            'CONCLUIDA',
            'CANCELADA',
            'COM_DIVERGENCIA'
        )),
    ADD CONSTRAINT ck_solicitacao_unidade_referencia
        CHECK (
            (tipo_operacao = 'SUPRIMENTO' AND destino_id IS NOT NULL)
            OR
            (tipo_operacao = 'RECOLHIMENTO' AND origem_id IS NOT NULL)
        ),
    ADD CONSTRAINT ck_solicitacao_rota
        CHECK (
            origem_id IS NULL
            OR destino_id IS NULL
            OR origem_id <> destino_id
        ),
    ADD CONSTRAINT ck_solicitacao_cancelamento
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
        );

-- Cria primeiro o indice substituto porque a FK da agencia ainda depende
-- de um indice iniciado por agencia_referencia_id.
CREATE INDEX ix_solicitacao_agencia_tipo_status
    ON solicitacao_numerario (
        agencia_referencia_id,
        tipo_operacao,
        status
    );

DROP INDEX ix_solicitacao_status_data ON solicitacao_numerario;
DROP INDEX ix_solicitacao_agencia_status ON solicitacao_numerario;

CREATE INDEX ix_solicitacao_status_data
    ON solicitacao_numerario (status, data_criacao);

CREATE INDEX ix_solicitacao_origem_status
    ON solicitacao_numerario (origem_id, status);

CREATE INDEX ix_solicitacao_destino_status
    ON solicitacao_numerario (destino_id, status);

-- ---------------------------------------------------------------------------
-- 6. Movimentacoes passam a referenciar unidade e operacao
-- ---------------------------------------------------------------------------

ALTER TABLE movimentacao
    DROP CHECK ck_movimentacao_tipo,
    ADD COLUMN unidade_operacional_id BIGINT NULL AFTER id,
    ADD COLUMN operacao_id BIGINT NULL AFTER solicitacao_id;

UPDATE movimentacao m
JOIN agencia a ON a.id = m.agencia_id
SET m.unidade_operacional_id = a.unidade_operacional_id;

UPDATE movimentacao m
JOIN operacao_numerario o ON o.solicitacao_id = m.solicitacao_id
SET m.operacao_id = o.id
WHERE m.tipo = 'ABASTECIMENTO';

ALTER TABLE movimentacao
    DROP FOREIGN KEY fk_movimentacao_agencia;

DROP INDEX ix_movimentacao_agencia_data ON movimentacao;

ALTER TABLE movimentacao
    DROP COLUMN agencia_id,
    MODIFY COLUMN unidade_operacional_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_movimentacao_unidade
        FOREIGN KEY (unidade_operacional_id)
        REFERENCES unidade_operacional (id),
    ADD CONSTRAINT fk_movimentacao_operacao
        FOREIGN KEY (operacao_id)
        REFERENCES operacao_numerario (id),
    ADD CONSTRAINT ck_movimentacao_tipo
        CHECK (tipo IN (
            'ABASTECIMENTO',
            'RECOLHIMENTO',
            'SAQUE',
            'DEPOSITO',
            'AJUSTE',
            'SAIDA_PARA_TRANSITO',
            'ENTRADA_DE_TRANSITO',
            'AJUSTE_DIVERGENCIA'
        ));

CREATE INDEX ix_movimentacao_unidade_data
    ON movimentacao (unidade_operacional_id, data_movimento);

CREATE INDEX ix_movimentacao_operacao
    ON movimentacao (operacao_id);

-- ---------------------------------------------------------------------------
-- 7. Validacoes pos-migration
-- Devem retornar zero, exceto contagens explicitamente esperadas.
-- ---------------------------------------------------------------------------

SELECT COUNT(*) AS agencias_sem_unidade
FROM agencia
WHERE unidade_operacional_id IS NULL;

SELECT COUNT(*) AS unidades_agencia_sem_saldo_controlado
FROM unidade_operacional
WHERE tipo = 'AGENCIA'
  AND controla_saldo = FALSE;

SELECT COUNT(*) AS solicitacoes_sem_tipo
FROM solicitacao_numerario
WHERE tipo_operacao IS NULL;

SELECT COUNT(*) AS suprimentos_sem_destino
FROM solicitacao_numerario
WHERE tipo_operacao = 'SUPRIMENTO'
  AND destino_id IS NULL;

SELECT COUNT(*) AS concluidas_sem_operacao
FROM solicitacao_numerario s
LEFT JOIN operacao_numerario o ON o.solicitacao_id = s.id
WHERE s.status = 'CONCLUIDA'
  AND o.id IS NULL;

SELECT COUNT(*) AS movimentacoes_sem_unidade
FROM movimentacao
WHERE unidade_operacional_id IS NULL;

SELECT status, COUNT(*) AS quantidade
FROM solicitacao_numerario
GROUP BY status
ORDER BY status;

SELECT
    (SELECT COUNT(*) FROM agencia) AS agencias,
    (SELECT COUNT(*) FROM unidade_operacional WHERE tipo = 'AGENCIA')
        AS unidades_agencia,
    (SELECT COUNT(*) FROM solicitacao_numerario) AS solicitacoes,
    (SELECT COUNT(*) FROM operacao_numerario) AS operacoes,
    (SELECT COUNT(*) FROM historico_solicitacao_numerario) AS eventos,
    (SELECT COUNT(*) FROM movimentacao) AS movimentacoes;
