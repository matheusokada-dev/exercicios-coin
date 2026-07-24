-- Massa deterministica para desenvolvimento local da Gestao de Numerario.
-- Escopo: usuario, agencia, solicitacao_abastecimento e movimentacao.
-- Pode ser reaplicada: logins, codigos, motivos identificados e chaves de
-- idempotencia impedem duplicidade.
--
-- Credencial dos usuarios criados por este script:
-- senha: admin123
--
-- Pre-requisitos:
-- 1. Banco gestao_numerario criado.
-- 2. Migrations Flyway V1 e V2 aplicadas.

USE gestao_numerario;

-- ---------------------------------------------------------------------------
-- Usuarios
-- ---------------------------------------------------------------------------

INSERT INTO usuario (nome, login, senha_hash, perfil, ativo)
VALUES
    ('Gestor Desenvolvimento', 'gestor',
     '$2a$10$4RTuRoL2oSAg9joUUBRZPuv8M29JQoxGHjlThxWeqPzrj.YzysAD2',
     'GESTOR', TRUE),
    ('Gestora Aprovadora', 'gestor.aprovador',
     '$2a$10$4RTuRoL2oSAg9joUUBRZPuv8M29JQoxGHjlThxWeqPzrj.YzysAD2',
     'GESTOR', TRUE),
    ('Operador Sao Paulo', 'operador.sp',
     '$2a$10$4RTuRoL2oSAg9joUUBRZPuv8M29JQoxGHjlThxWeqPzrj.YzysAD2',
     'OPERADOR', TRUE),
    ('Operadora Sul', 'operador.sul',
     '$2a$10$4RTuRoL2oSAg9joUUBRZPuv8M29JQoxGHjlThxWeqPzrj.YzysAD2',
     'OPERADOR', TRUE),
    ('Operador Nordeste', 'operador.nordeste',
     '$2a$10$4RTuRoL2oSAg9joUUBRZPuv8M29JQoxGHjlThxWeqPzrj.YzysAD2',
     'OPERADOR', TRUE),
    ('Operadora Centro-Oeste', 'operador.centro',
     '$2a$10$4RTuRoL2oSAg9joUUBRZPuv8M29JQoxGHjlThxWeqPzrj.YzysAD2',
     'OPERADOR', TRUE),
    ('Operador Inativo', 'operador.inativo',
     '$2a$10$4RTuRoL2oSAg9joUUBRZPuv8M29JQoxGHjlThxWeqPzrj.YzysAD2',
     'OPERADOR', FALSE)
ON DUPLICATE KEY UPDATE
    nome = VALUES(nome),
    perfil = VALUES(perfil),
    ativo = VALUES(ativo);

SET @gestor_aprovador_id = (
    SELECT id FROM usuario WHERE login = 'gestor.aprovador' LIMIT 1
);
SET @operador_sp_id = (
    SELECT id FROM usuario WHERE login = 'operador.sp' LIMIT 1
);
SET @operador_sul_id = (
    SELECT id FROM usuario WHERE login = 'operador.sul' LIMIT 1
);
SET @operador_nordeste_id = (
    SELECT id FROM usuario WHERE login = 'operador.nordeste' LIMIT 1
);
SET @operador_centro_id = (
    SELECT id FROM usuario WHERE login = 'operador.centro' LIMIT 1
);

-- ---------------------------------------------------------------------------
-- Agencias
-- ---------------------------------------------------------------------------

DROP TEMPORARY TABLE IF EXISTS tmp_seed_agencia;
CREATE TEMPORARY TABLE tmp_seed_agencia (
    ordem INT NOT NULL,
    codigo VARCHAR(10) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    saldo_final DECIMAL(19,2) NOT NULL,
    limite_minimo DECIMAL(19,2) NOT NULL,
    ativo BOOLEAN NOT NULL,
    PRIMARY KEY (codigo)
);

INSERT INTO tmp_seed_agencia
    (ordem, codigo, nome, cidade, saldo_final, limite_minimo, ativo)
VALUES
    (1,  '0101', 'Agencia Se',              'Sao Paulo',        780000.00, 250000.00, TRUE),
    (2,  '0102', 'Agencia Pinheiros',       'Sao Paulo',        185000.00, 220000.00, TRUE),
    (3,  '0103', 'Agencia Cambui',          'Campinas',         420000.00, 180000.00, TRUE),
    (4,  '0104', 'Agencia Gonzaga',         'Santos',            95000.00, 160000.00, TRUE),
    (5,  '0105', 'Agencia Centro Curitiba', 'Curitiba',         250000.00, 250000.00, TRUE),
    (6,  '0106', 'Agencia Savassi',         'Belo Horizonte',   620000.00, 300000.00, TRUE),
    (7,  '0107', 'Agencia Copacabana',      'Rio de Janeiro',   140000.00, 190000.00, TRUE),
    (8,  '0108', 'Agencia Brasilia Centro', 'Brasilia',         910000.00, 450000.00, TRUE),
    (9,  '0109', 'Agencia Boa Viagem',      'Recife',           205000.00, 210000.00, TRUE),
    (10, '0110', 'Agencia Aldeota',         'Fortaleza',        330000.00, 180000.00, TRUE),
    (11, '0111', 'Agencia Pituba',          'Salvador',          75000.00, 150000.00, TRUE),
    (12, '0112', 'Agencia Manaus Centro',   'Manaus',           560000.00, 260000.00, TRUE),
    (13, '0113', 'Agencia Belem Centro',    'Belem',            480000.00, 220000.00, TRUE),
    (14, '0114', 'Agencia Goiania Sul',     'Goiania',          110000.00, 175000.00, TRUE),
    (15, '0115', 'Agencia Porto Alegre',    'Porto Alegre',     700000.00, 300000.00, TRUE),
    (16, '0116', 'Agencia Florianopolis',   'Florianopolis',    290000.00, 290000.00, TRUE),
    (17, '0117', 'Agencia Vitoria',         'Vitoria',          160000.00, 230000.00, TRUE),
    (18, '0118', 'Agencia Campo Grande',    'Campo Grande',     840000.00, 350000.00, TRUE),
    (19, '0119', 'Agencia Cuiaba Centro',   'Cuiaba',           125000.00, 200000.00, TRUE),
    (20, '0120', 'Agencia Londrina',        'Londrina',         390000.00, 210000.00, TRUE),
    (21, '0121', 'Agencia Ribeirao Preto',  'Ribeirao Preto',   980000.00, 400000.00, TRUE),
    (22, '0122', 'Agencia Sorocaba',        'Sorocaba',         145000.00, 195000.00, TRUE),
    (23, '0123', 'Agencia Sao Jose Campos', 'Sao Jose Campos',  510000.00, 240000.00, TRUE),
    (24, '0124', 'Agencia Maceio Centro',   'Maceio',            89000.00, 170000.00, TRUE),
    (25, '0125', 'Agencia Joao Pessoa',     'Joao Pessoa',      450000.00, 200000.00, TRUE),
    (26, '0126', 'Agencia Natal Centro',    'Natal',            270000.00, 280000.00, TRUE),
    (27, '0127', 'Agencia Centro Antiga',   'Sao Paulo',        320000.00, 180000.00, FALSE),
    (28, '0128', 'Agencia Porto Antiga',    'Porto Alegre',      90000.00, 160000.00, FALSE),
    (29, '0129', 'Agencia Recife Antiga',   'Recife',           610000.00, 250000.00, FALSE),
    (30, '0130', 'Agencia Brasilia Antiga', 'Brasilia',         130000.00, 190000.00, FALSE);

INSERT INTO agencia
    (codigo, nome, cidade, saldo_atual, limite_minimo, ativo)
SELECT
    s.codigo,
    s.nome,
    s.cidade,
    s.saldo_final,
    s.limite_minimo,
    s.ativo
FROM tmp_seed_agencia s
WHERE NOT EXISTS (
    SELECT 1 FROM agencia a WHERE a.codigo = s.codigo
);

-- ---------------------------------------------------------------------------
-- Solicitacoes historicas
-- Cinco solicitacoes fechadas por agencia: tres atendidas e duas rejeitadas.
-- ---------------------------------------------------------------------------

DROP TEMPORARY TABLE IF EXISTS tmp_seed_ciclo_solicitacao;
CREATE TEMPORARY TABLE tmp_seed_ciclo_solicitacao (
    ciclo INT NOT NULL,
    status_solicitacao VARCHAR(20) NOT NULL,
    dias_atras INT NOT NULL,
    PRIMARY KEY (ciclo)
);

INSERT INTO tmp_seed_ciclo_solicitacao
    (ciclo, status_solicitacao, dias_atras)
VALUES
    (1, 'ATENDIDA', 300),
    (2, 'REJEITADA', 240),
    (3, 'ATENDIDA', 150),
    (4, 'REJEITADA', 75),
    (5, 'ATENDIDA', 0);

INSERT INTO solicitacao_abastecimento (
    agencia_id,
    valor,
    motivo,
    data_desejada,
    status,
    solicitante_id,
    decisor_id,
    justificativa_decisao,
    justificativa_especial,
    data_criacao,
    data_decisao,
    data_atendimento
)
SELECT
    a.id,
    CASE
        WHEN c.ciclo = 5 AND s.ordem IN (8, 18, 21)
            THEN 550000.00 + (s.ordem * 1000.00)
        WHEN c.status_solicitacao = 'ATENDIDA'
            THEN 60000.00 + (s.ordem * 1000.00)
        ELSE 45000.00 + (s.ordem * 750.00)
    END,
    CONCAT(
        '[SEED-MASSA-V1-', s.codigo, '-', LPAD(c.ciclo, 2, '0'), '] ',
        CASE c.status_solicitacao
            WHEN 'ATENDIDA' THEN 'Reposicao programada concluida'
            ELSE 'Pedido reavaliado no fechamento operacional'
        END
    ),
    CASE
        WHEN c.ciclo = 5 THEN CURRENT_DATE
        ELSE DATE(TIMESTAMPADD(DAY, -(c.dias_atras - 1), UTC_TIMESTAMP(6)))
    END,
    c.status_solicitacao,
    CASE MOD(s.ordem, 4)
        WHEN 0 THEN @operador_sp_id
        WHEN 1 THEN @operador_sul_id
        WHEN 2 THEN @operador_nordeste_id
        ELSE @operador_centro_id
    END,
    @gestor_aprovador_id,
    CASE c.status_solicitacao
        WHEN 'ATENDIDA' THEN 'Aprovada conforme planejamento de numerario'
        ELSE 'Rejeitada apos revisao da necessidade operacional'
    END,
    CASE
        WHEN c.ciclo = 5 AND s.ordem IN (8, 18, 21)
            THEN 'Valor extraordinario para evento regional de alta demanda'
        ELSE NULL
    END,
    CASE
        WHEN c.ciclo = 5
            THEN TIMESTAMPADD(MINUTE, -(180 + s.ordem * 10), UTC_TIMESTAMP(6))
        ELSE TIMESTAMPADD(
            MINUTE,
            -s.ordem,
            TIMESTAMPADD(DAY, -c.dias_atras, UTC_TIMESTAMP(6))
        )
    END,
    CASE
        WHEN c.ciclo = 5
            THEN TIMESTAMPADD(MINUTE, -(120 + s.ordem * 10), UTC_TIMESTAMP(6))
        ELSE TIMESTAMPADD(
            MINUTE,
            60 - s.ordem,
            TIMESTAMPADD(DAY, -c.dias_atras, UTC_TIMESTAMP(6))
        )
    END,
    CASE
        WHEN c.status_solicitacao = 'ATENDIDA' AND c.ciclo = 5
            THEN TIMESTAMPADD(MINUTE, -(60 + s.ordem * 10), UTC_TIMESTAMP(6))
        WHEN c.status_solicitacao = 'ATENDIDA'
            THEN TIMESTAMPADD(
                MINUTE,
                120 - s.ordem,
                TIMESTAMPADD(DAY, -c.dias_atras, UTC_TIMESTAMP(6))
            )
        ELSE NULL
    END
FROM tmp_seed_agencia s
JOIN agencia a
    ON a.codigo = s.codigo
CROSS JOIN tmp_seed_ciclo_solicitacao c
WHERE NOT EXISTS (
    SELECT 1
    FROM solicitacao_abastecimento existente
    WHERE existente.agencia_id = a.id
      AND existente.motivo LIKE CONCAT(
          '[SEED-MASSA-V1-', s.codigo, '-', LPAD(c.ciclo, 2, '0'), ']%'
      )
);

-- Uma solicitacao aberta por agencia ativa, alternando PENDENTE e APROVADA.
INSERT INTO solicitacao_abastecimento (
    agencia_id,
    valor,
    motivo,
    data_desejada,
    status,
    solicitante_id,
    decisor_id,
    justificativa_decisao,
    justificativa_especial,
    data_criacao,
    data_decisao,
    data_atendimento
)
SELECT
    a.id,
    CASE
        WHEN s.ordem = 21 THEN 650000.00
        ELSE 70000.00 + (s.ordem * 2000.00)
    END,
    CONCAT(
        '[SEED-MASSA-V1-', s.codigo, '-ABERTA] ',
        CASE
            WHEN MOD(s.ordem, 3) = 0
                THEN 'Abastecimento aprovado aguardando atendimento'
            ELSE 'Abastecimento aguardando analise'
        END
    ),
    CURRENT_DATE + INTERVAL (1 + MOD(s.ordem, 5)) DAY,
    CASE WHEN MOD(s.ordem, 3) = 0 THEN 'APROVADA' ELSE 'PENDENTE' END,
    CASE MOD(s.ordem, 4)
        WHEN 0 THEN @operador_sp_id
        WHEN 1 THEN @operador_sul_id
        WHEN 2 THEN @operador_nordeste_id
        ELSE @operador_centro_id
    END,
    CASE WHEN MOD(s.ordem, 3) = 0 THEN @gestor_aprovador_id ELSE NULL END,
    CASE
        WHEN MOD(s.ordem, 3) = 0
            THEN 'Aprovada para atendimento na data solicitada'
        ELSE NULL
    END,
    CASE
        WHEN s.ordem = 21
            THEN 'Reforco extraordinario para periodo de alta demanda'
        ELSE NULL
    END,
    TIMESTAMPADD(HOUR, -(24 + s.ordem), UTC_TIMESTAMP(6)),
    CASE
        WHEN MOD(s.ordem, 3) = 0
            THEN TIMESTAMPADD(HOUR, -(20 + s.ordem), UTC_TIMESTAMP(6))
        ELSE NULL
    END,
    NULL
FROM tmp_seed_agencia s
JOIN agencia a
    ON a.codigo = s.codigo
WHERE s.ativo = TRUE
  AND NOT EXISTS (
      SELECT 1
      FROM solicitacao_abastecimento existente
      WHERE existente.agencia_id = a.id
        AND existente.motivo LIKE CONCAT(
            '[SEED-MASSA-V1-', s.codigo, '-ABERTA]%'
        )
  )
  AND NOT EXISTS (
      SELECT 1
      FROM solicitacao_abastecimento aberta
      WHERE aberta.agencia_id = a.id
        AND aberta.status IN ('PENDENTE', 'APROVADA')
  );

-- ---------------------------------------------------------------------------
-- Movimentacoes
-- Dezoito movimentos por agencia, distribuidos em tres ciclos.
-- Cada ciclo possui saque, deposito, recolhimento, ajuste, saque e
-- abastecimento. Os saldos formam uma cadeia e terminam em saldo_atual.
-- ---------------------------------------------------------------------------

DROP TEMPORARY TABLE IF EXISTS tmp_seed_sequencia_movimento;
CREATE TEMPORARY TABLE tmp_seed_sequencia_movimento (
    numero INT NOT NULL,
    PRIMARY KEY (numero)
);

INSERT INTO tmp_seed_sequencia_movimento (numero)
VALUES
    (1), (2), (3), (4), (5), (6),
    (7), (8), (9), (10), (11), (12),
    (13), (14), (15), (16), (17), (18);

DROP TEMPORARY TABLE IF EXISTS tmp_seed_movimento;
CREATE TEMPORARY TABLE tmp_seed_movimento AS
SELECT
    calculado.*,
    calculado.saldo_final
        - calculado.delta_total
        + calculado.delta_acumulado
        - calculado.delta AS saldo_anterior,
    calculado.saldo_final
        - calculado.delta_total
        + calculado.delta_acumulado AS saldo_posterior
FROM (
    SELECT
        bruto.*,
        SUM(bruto.delta) OVER (
            PARTITION BY bruto.codigo
        ) AS delta_total,
        SUM(bruto.delta) OVER (
            PARTITION BY bruto.codigo
            ORDER BY bruto.numero
            ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
        ) AS delta_acumulado
    FROM (
        SELECT
            a.id AS agencia_id,
            s.ordem,
            s.codigo,
            s.saldo_final,
            seq.numero,
            CEIL(seq.numero / 6) AS ciclo,
            MOD(seq.numero - 1, 6) + 1 AS posicao_ciclo,
            CASE MOD(seq.numero - 1, 6)
                WHEN 0 THEN 'SAQUE'
                WHEN 1 THEN 'DEPOSITO'
                WHEN 2 THEN 'RECOLHIMENTO'
                WHEN 3 THEN 'AJUSTE'
                WHEN 4 THEN 'SAQUE'
                ELSE 'ABASTECIMENTO'
            END AS tipo,
            CASE MOD(seq.numero - 1, 6)
                WHEN 1 THEN TRUE
                WHEN 5 THEN TRUE
                ELSE FALSE
            END AS entrada,
            CASE MOD(seq.numero - 1, 6)
                WHEN 0 THEN 28000.00 + (s.ordem * 300.00)
                WHEN 1 THEN 12000.00 + (s.ordem * 200.00)
                WHEN 2 THEN 30000.00 + (s.ordem * 400.00)
                WHEN 3 THEN 5000.00
                WHEN 4 THEN 18000.00 + (s.ordem * 200.00)
                ELSE CASE
                    WHEN CEIL(seq.numero / 6) = 3
                         AND s.ordem IN (8, 18, 21)
                        THEN 550000.00 + (s.ordem * 1000.00)
                    ELSE 60000.00 + (s.ordem * 1000.00)
                END
            END AS valor,
            CASE MOD(seq.numero - 1, 6)
                WHEN 0 THEN -(28000.00 + (s.ordem * 300.00))
                WHEN 1 THEN 12000.00 + (s.ordem * 200.00)
                WHEN 2 THEN -(30000.00 + (s.ordem * 400.00))
                WHEN 3 THEN -5000.00
                WHEN 4 THEN -(18000.00 + (s.ordem * 200.00))
                ELSE CASE
                    WHEN CEIL(seq.numero / 6) = 3
                         AND s.ordem IN (8, 18, 21)
                        THEN 550000.00 + (s.ordem * 1000.00)
                    ELSE 60000.00 + (s.ordem * 1000.00)
                END
            END AS delta,
            TIMESTAMPADD(
                MINUTE,
                -s.ordem,
                TIMESTAMPADD(
                    DAY,
                    -(
                        CASE CEIL(seq.numero / 6)
                            WHEN 1 THEN 306
                            WHEN 2 THEN 156
                            ELSE 6
                        END - (MOD(seq.numero - 1, 6) + 1)
                    ),
                    UTC_TIMESTAMP(6)
                )
            ) AS data_movimento,
            CONCAT(
                'seed-mass-v1-', s.codigo, '-', LPAD(seq.numero, 2, '0')
            ) AS idempotency_key
        FROM tmp_seed_agencia s
        JOIN agencia a
            ON a.codigo = s.codigo
        CROSS JOIN tmp_seed_sequencia_movimento seq
    ) bruto
) calculado;

INSERT INTO movimentacao (
    agencia_id,
    solicitacao_id,
    tipo,
    entrada,
    valor,
    saldo_anterior,
    saldo_posterior,
    descricao,
    data_movimento,
    usuario_id,
    idempotency_key
)
SELECT
    m.agencia_id,
    CASE WHEN m.tipo = 'ABASTECIMENTO' THEN sol.id ELSE NULL END,
    m.tipo,
    m.entrada,
    m.valor,
    m.saldo_anterior,
    m.saldo_posterior,
    CASE m.tipo
        WHEN 'ABASTECIMENTO' THEN 'Abastecimento vinculado a solicitacao atendida'
        WHEN 'RECOLHIMENTO' THEN 'Recolhimento operacional programado'
        WHEN 'SAQUE' THEN 'Saques consolidados do periodo'
        WHEN 'DEPOSITO' THEN 'Depositos consolidados do periodo'
        ELSE 'Ajuste de saida para conciliacao operacional'
    END,
    CASE
        WHEN m.tipo = 'ABASTECIMENTO' THEN sol.data_atendimento
        ELSE m.data_movimento
    END,
    CASE MOD(m.ordem, 4)
        WHEN 0 THEN @operador_sp_id
        WHEN 1 THEN @operador_sul_id
        WHEN 2 THEN @operador_nordeste_id
        ELSE @operador_centro_id
    END,
    m.idempotency_key
FROM tmp_seed_movimento m
LEFT JOIN solicitacao_abastecimento sol
    ON m.tipo = 'ABASTECIMENTO'
   AND sol.agencia_id = m.agencia_id
   AND sol.motivo LIKE CONCAT(
       '[SEED-MASSA-V1-',
       m.codigo,
       '-',
       LPAD((m.ciclo * 2) - 1, 2, '0'),
       ']%'
   )
WHERE NOT EXISTS (
    SELECT 1
    FROM movimentacao existente
    WHERE existente.idempotency_key = m.idempotency_key
);

-- ---------------------------------------------------------------------------
-- Resumo e verificacoes da massa
-- ---------------------------------------------------------------------------

SELECT
    (SELECT COUNT(*) FROM usuario
     WHERE login IN (
         'gestor', 'gestor.aprovador', 'operador.sp', 'operador.sul',
         'operador.nordeste', 'operador.centro', 'operador.inativo'
     )) AS usuarios_seed,
    (SELECT COUNT(*) FROM agencia
     WHERE codigo BETWEEN '0101' AND '0130') AS agencias_seed,
    (SELECT COUNT(*) FROM solicitacao_abastecimento
     WHERE motivo LIKE '[SEED-MASSA-V1-%') AS solicitacoes_seed,
    (SELECT COUNT(*) FROM movimentacao
     WHERE idempotency_key LIKE 'seed-mass-v1-%') AS movimentacoes_seed;

SELECT
    status,
    COUNT(*) AS quantidade
FROM solicitacao_abastecimento
WHERE motivo LIKE '[SEED-MASSA-V1-%'
GROUP BY status
ORDER BY status;

SELECT
    tipo,
    entrada,
    COUNT(*) AS quantidade,
    SUM(valor) AS valor_total
FROM movimentacao
WHERE idempotency_key LIKE 'seed-mass-v1-%'
GROUP BY tipo, entrada
ORDER BY tipo, entrada;

SELECT
    SUM(CASE WHEN a.ativo = TRUE THEN 1 ELSE 0 END) AS agencias_ativas,
    SUM(CASE WHEN a.ativo = FALSE THEN 1 ELSE 0 END) AS agencias_inativas,
    SUM(CASE
        WHEN a.ativo = TRUE AND a.saldo_atual < a.limite_minimo THEN 1
        ELSE 0
    END) AS agencias_em_alerta,
    SUM(CASE
        WHEN a.ativo = TRUE AND a.saldo_atual = a.limite_minimo THEN 1
        ELSE 0
    END) AS agencias_no_limite
FROM agencia a
WHERE a.codigo BETWEEN '0101' AND '0130';

-- Deve retornar zero.
SELECT COUNT(*) AS movimentos_com_saldo_inconsistente
FROM movimentacao m
WHERE m.idempotency_key LIKE 'seed-mass-v1-%'
  AND m.saldo_posterior <> CASE
      WHEN m.entrada = TRUE THEN m.saldo_anterior + m.valor
      ELSE m.saldo_anterior - m.valor
  END;

DROP TEMPORARY TABLE IF EXISTS tmp_seed_movimento;
DROP TEMPORARY TABLE IF EXISTS tmp_seed_sequencia_movimento;
DROP TEMPORARY TABLE IF EXISTS tmp_seed_ciclo_solicitacao;
DROP TEMPORARY TABLE IF EXISTS tmp_seed_agencia;
