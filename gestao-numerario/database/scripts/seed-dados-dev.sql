-- Massa deterministica exclusiva para desenvolvimento local.
-- Execute manualmente somente depois da V1, em um schema vazio/resetado.
-- Senha de todos os usuarios: admin123

INSERT INTO usuario (
    id, nome, login, senha_hash, perfil, ativo, criado_em
)
VALUES
    (1, 'Gestor Desenvolvimento', 'gestor', '$2a$10$4RTuRoL2oSAg9joUUBRZPuv8M29JQoxGHjlThxWeqPzrj.YzysAD2', 'GESTOR', TRUE, '2026-01-02 08:00:00'),
    (2, 'Gestora Aprovadora', 'gestor.aprovador', '$2a$10$4RTuRoL2oSAg9joUUBRZPuv8M29JQoxGHjlThxWeqPzrj.YzysAD2', 'GESTOR', TRUE, '2026-01-02 08:05:00'),
    (3, 'Operador Sao Paulo', 'operador.sp', '$2a$10$4RTuRoL2oSAg9joUUBRZPuv8M29JQoxGHjlThxWeqPzrj.YzysAD2', 'OPERADOR', TRUE, '2026-01-02 08:10:00'),
    (4, 'Operadora Sul', 'operador.sul', '$2a$10$4RTuRoL2oSAg9joUUBRZPuv8M29JQoxGHjlThxWeqPzrj.YzysAD2', 'OPERADOR', TRUE, '2026-01-02 08:15:00'),
    (5, 'Operador Nordeste', 'operador.nordeste', '$2a$10$4RTuRoL2oSAg9joUUBRZPuv8M29JQoxGHjlThxWeqPzrj.YzysAD2', 'OPERADOR', TRUE, '2026-01-02 08:20:00'),
    (6, 'Operadora Centro-Oeste', 'operador.centro', '$2a$10$4RTuRoL2oSAg9joUUBRZPuv8M29JQoxGHjlThxWeqPzrj.YzysAD2', 'OPERADOR', TRUE, '2026-01-02 08:25:00'),
    (7, 'Operador Inativo', 'operador.inativo', '$2a$10$4RTuRoL2oSAg9joUUBRZPuv8M29JQoxGHjlThxWeqPzrj.YzysAD2', 'OPERADOR', FALSE, '2026-01-02 08:30:00');

INSERT INTO agencia (
    id, codigo, nome, cidade, saldo_atual, limite_minimo, ativo, criado_em
)
VALUES
    (1,  '0101', 'Agência Sé',              'São Paulo',       780000.00, 250000.00, TRUE,  '2026-01-03 09:00:00'),
    (2,  '0102', 'Agencia Pinheiros',       'Sao Paulo',       185000.00, 220000.00, TRUE,  '2026-01-03 09:01:00'),
    (3,  '0103', 'Agencia Cambui',          'Campinas',        420000.00, 180000.00, TRUE,  '2026-01-03 09:02:00'),
    (4,  '0104', 'Agencia Gonzaga',         'Santos',           95000.00, 160000.00, TRUE,  '2026-01-03 09:03:00'),
    (5,  '0105', 'Agencia Centro Curitiba', 'Curitiba',        250000.00, 250000.00, TRUE,  '2026-01-03 09:04:00'),
    (6,  '0106', 'Agencia Savassi',         'Belo Horizonte',  620000.00, 300000.00, TRUE,  '2026-01-03 09:05:00'),
    (7,  '0107', 'Agencia Copacabana',      'Rio de Janeiro',  140000.00, 190000.00, TRUE,  '2026-01-03 09:06:00'),
    (8,  '0108', 'Agencia Brasilia Centro', 'Brasilia',        910000.00, 450000.00, TRUE,  '2026-01-03 09:07:00'),
    (9,  '0109', 'Agencia Boa Viagem',      'Recife',          205000.00, 210000.00, TRUE,  '2026-01-03 09:08:00'),
    (10, '0110', 'Agencia Aldeota',         'Fortaleza',       330000.00, 180000.00, TRUE,  '2026-01-03 09:09:00'),
    (11, '0111', 'Agencia Pituba',          'Salvador',         75000.00, 150000.00, TRUE,  '2026-01-03 09:10:00'),
    (12, '0112', 'Agencia Manaus Centro',   'Manaus',          560000.00, 260000.00, TRUE,  '2026-01-03 09:11:00'),
    (13, '0113', 'Agencia Belem Centro',    'Belem',           480000.00, 220000.00, TRUE,  '2026-01-03 09:12:00'),
    (14, '0114', 'Agencia Goiania Sul',     'Goiania',         110000.00, 175000.00, TRUE,  '2026-01-03 09:13:00'),
    (15, '0115', 'Agencia Porto Alegre',    'Porto Alegre',    700000.00, 300000.00, TRUE,  '2026-01-03 09:14:00'),
    (16, '0116', 'Agencia Florianopolis',   'Florianopolis',   290000.00, 290000.00, TRUE,  '2026-01-03 09:15:00'),
    (17, '0117', 'Agencia Vitoria',         'Vitoria',         160000.00, 230000.00, TRUE,  '2026-01-03 09:16:00'),
    (18, '0118', 'Agencia Campo Grande',    'Campo Grande',    840000.00, 350000.00, TRUE,  '2026-01-03 09:17:00'),
    (19, '0119', 'Agencia Cuiaba Centro',   'Cuiaba',          125000.00, 200000.00, TRUE,  '2026-01-03 09:18:00'),
    (20, '0120', 'Agencia Londrina',        'Londrina',        390000.00, 210000.00, TRUE,  '2026-01-03 09:19:00'),
    (21, '0121', 'Agencia Ribeirao Preto',  'Ribeirao Preto',  980000.00, 400000.00, TRUE,  '2026-01-03 09:20:00'),
    (22, '0122', 'Agencia Sorocaba',        'Sorocaba',        145000.00, 195000.00, TRUE,  '2026-01-03 09:21:00'),
    (23, '0123', 'Agencia Sao Jose Campos', 'Sao Jose Campos', 510000.00, 240000.00, TRUE,  '2026-01-03 09:22:00'),
    (24, '0124', 'Agencia Maceio Centro',   'Maceio',           89000.00, 170000.00, TRUE,  '2026-01-03 09:23:00'),
    (25, '0125', 'Agencia Joao Pessoa',     'Joao Pessoa',     450000.00, 200000.00, TRUE,  '2026-01-03 09:24:00'),
    (26, '0126', 'Agencia Natal Centro',    'Natal',           270000.00, 280000.00, TRUE,  '2026-01-03 09:25:00'),
    (27, '0127', 'Agencia Centro Antiga',   'Sao Paulo',       320000.00, 180000.00, FALSE, '2026-01-03 09:26:00'),
    (28, '0128', 'Agencia Porto Antiga',    'Porto Alegre',     90000.00, 160000.00, FALSE, '2026-01-03 09:27:00'),
    (29, '0129', 'Agencia Recife Antiga',   'Recife',          610000.00, 250000.00, FALSE, '2026-01-03 09:28:00'),
    (30, '0130', 'Agencia Brasilia Antiga', 'Brasilia',        130000.00, 190000.00, FALSE, '2026-01-03 09:29:00');

-- Duas solicitacoes de cada status, uma de suprimento e uma de recolhimento.
INSERT INTO solicitacao_numerario (
    id, tipo_operacao, agencia_id, origem_agencia_id, destino_agencia_id,
    valor_solicitado, motivo, data_desejada,
    status, solicitante_id, decisor_id, justificativa_decisao, data_criacao,
    data_decisao, data_conclusao, cancelado_por_id,
    justificativa_cancelamento, data_cancelamento
)
VALUES
    (1,  'SUPRIMENTO',    2, NULL, 2, 120000.00, 'Saldo abaixo do limite operacional',        '2026-08-01', 'PENDENTE',        3, NULL, NULL,                         '2026-07-28 08:00:00', NULL,                  NULL,                  NULL, NULL,                              NULL),
    (2,  'SUPRIMENTO',    4, 1,    4,  90000.00, 'Recomposicao para demanda do fim de semana','2026-07-31', 'APROVADA',        3, 2,    'Necessidade operacional confirmada', '2026-07-27 08:00:00', '2026-07-27 09:00:00', NULL,                  NULL, NULL,                              NULL),
    (3,  'SUPRIMENTO',    1, NULL, 1, 400000.00, 'Reforco preventivo acima da previsao',      '2026-07-25', 'REJEITADA',       3, 2,    'Saldo atual atende a previsao',      '2026-07-22 08:00:00', '2026-07-22 10:00:00', NULL,                  NULL, NULL,                              NULL),
    (4,  'SUPRIMENTO',    7, 1,    7, 100000.00, 'Atendimento de alerta de numerario',        '2026-07-29', 'EM_EXECUCAO',     3, 2,    'Alerta de saldo validado',           '2026-07-24 08:00:00', '2026-07-24 09:00:00', NULL,                  NULL, NULL,                              NULL),
    (5,  'SUPRIMENTO',   11, 1,   11,  50000.00, 'Normalizacao do saldo da agencia',          '2026-07-20', 'CONCLUIDA',       5, 2,    'Suprimento aprovado',                '2026-07-16 08:00:00', '2026-07-16 09:00:00', '2026-07-20 15:00:00', NULL, NULL,                              NULL),
    (6,  'SUPRIMENTO',    3, NULL, 3,  80000.00, 'Evento regional posteriormente cancelado', '2026-07-18', 'CANCELADA',       3, NULL, NULL,                         '2026-07-14 08:00:00', NULL,                  NULL,                  3,    'Evento regional foi cancelado',      '2026-07-15 11:00:00'),
    (7,  'SUPRIMENTO',   14, 1,   14, 100000.00, 'Reforco emergencial de caixa',              '2026-07-23', 'COM_DIVERGENCIA', 6, 2,    'Emergencia operacional confirmada',  '2026-07-18 08:00:00', '2026-07-18 09:00:00', NULL,                  NULL, NULL,                              NULL),
    (8,  'RECOLHIMENTO', 21, 21, NULL,300000.00, 'Excedente acima da necessidade projetada',  '2026-08-02', 'PENDENTE',        3, NULL, NULL,                         '2026-07-28 08:30:00', NULL,                  NULL,                  NULL, NULL,                              NULL),
    (9,  'RECOLHIMENTO',  8, 8,    1, 200000.00, 'Reducao preventiva de exposicao',           '2026-07-31', 'APROVADA',        6, 1,    'Excedente confirmado',               '2026-07-27 08:30:00', '2026-07-27 09:30:00', NULL,                  NULL, NULL,                              NULL),
    (10, 'RECOLHIMENTO',  6, 6,  NULL, 500000.00, 'Recolhimento integral solicitado',          '2026-07-25', 'REJEITADA',       3, 1,    'Valor comprometeria o limite minimo','2026-07-22 08:30:00', '2026-07-22 10:30:00', NULL,                  NULL, NULL,                              NULL),
    (11, 'RECOLHIMENTO', 15,15,    1, 150000.00, 'Transferencia de excedente operacional',    '2026-07-29', 'EM_EXECUCAO',     4, 1,    'Excedente disponivel',               '2026-07-24 08:30:00', '2026-07-24 09:30:00', NULL,                  NULL, NULL,                              NULL),
    (12, 'RECOLHIMENTO', 20,20,    1, 100000.00, 'Reducao de saldo apos evento',              '2026-07-20', 'CONCLUIDA',       4, 1,    'Evento encerrado',                   '2026-07-16 08:30:00', '2026-07-16 09:30:00', '2026-07-20 16:00:00', NULL, NULL,                              NULL),
    (13, 'RECOLHIMENTO', 23,23, NULL, 120000.00, 'Reducao sazonal cancelada',                 '2026-07-18', 'CANCELADA',       3, NULL, NULL,                         '2026-07-14 08:30:00', NULL,                  NULL,                  3,    'Previsao de demanda foi revisada',    '2026-07-15 11:30:00'),
    (14, 'RECOLHIMENTO', 25,25,    1, 100000.00, 'Recolhimento de excedente',                 '2026-07-23', 'COM_DIVERGENCIA', 5, 1,    'Excedente validado',                 '2026-07-18 08:30:00', '2026-07-18 09:30:00', NULL,                  NULL, NULL,                              NULL);

-- Todos os estados de operacao aparecem ao menos uma vez.
INSERT INTO operacao_numerario (
    id, solicitacao_id, origem_agencia_id, destino_agencia_id,
    status, valor_programado, valor_expedido,
    valor_recebido, valor_divergencia, programado_por_id, expedido_por_id,
    recebido_por_id, conciliado_por_id, data_programacao, data_expedicao,
    data_recebimento, data_conciliacao, justificativa_divergencia,
    descricao_ocorrencia, idempotency_key
)
VALUES
    (1, 2,  1,  4, 'PROGRAMADA',       90000.00, NULL,      NULL,      NULL,     2, NULL, NULL, NULL, '2026-07-27 10:00:00', NULL,                  NULL,                  NULL,                  NULL,                              NULL,                              'seed-operacao-programada'),
    (2, 9,  8,  1, 'EM_SEPARACAO',    200000.00, NULL,      NULL,      NULL,     1, NULL, NULL, NULL, '2026-07-27 10:30:00', NULL,                  NULL,                  NULL,                  NULL,                              NULL,                              'seed-operacao-separacao'),
    (3, 4,  1,  7, 'EM_TRANSITO',     100000.00, 100000.00, NULL,      NULL,     2, 3,    NULL, NULL, '2026-07-24 10:00:00', '2026-07-28 08:00:00', NULL,                  NULL,                  NULL,                              NULL,                              'seed-operacao-transito'),
    (4, 11,15,  1, 'RECEBIDA',        150000.00, 150000.00, 150000.00,0.00,     1, 4,    3,    NULL, '2026-07-24 10:30:00', '2026-07-28 08:30:00', '2026-07-29 08:00:00', NULL,                  NULL,                              NULL,                              'seed-operacao-recebida'),
    (5, 5,  1, 11, 'CONCILIADA',       50000.00,  50000.00,  50000.00,0.00,     2, 5,    5,    2,    '2026-07-16 10:00:00', '2026-07-19 08:00:00', '2026-07-20 14:00:00', '2026-07-20 15:00:00', NULL,                              NULL,                              'seed-operacao-conciliada'),
    (6, 7,  1, 14, 'COM_DIVERGENCIA', 100000.00, 100000.00,  80000.00,20000.00, 2, 6,    6,    NULL, '2026-07-18 10:00:00', '2026-07-22 08:00:00', '2026-07-23 14:00:00', NULL,                  'Volume recebido abaixo do expedido','Lacre conferido sem violacao',    'seed-operacao-divergencia'),
    (7, 12,20,  1, 'CONCILIADA',      100000.00, 100000.00, 100000.00,0.00,     1, 4,    4,    1,    '2026-07-16 10:30:00', '2026-07-19 08:30:00', '2026-07-20 15:00:00', '2026-07-20 16:00:00', NULL,                              NULL,                              'seed-operacao-recolhimento'),
    (8, 14,25,  1, 'COM_DIVERGENCIA', 100000.00, 100000.00,  95000.00,5000.00,  1, 5,    5,    NULL, '2026-07-18 10:30:00', '2026-07-22 08:30:00', '2026-07-23 14:30:00', NULL,                  'Diferenca na conferencia de malote','Ocorrencia em analise',          'seed-operacao-divergencia-recolh');

INSERT INTO movimentacao (
    id, agencia_id, solicitacao_id, operacao_id, tipo, entrada, valor,
    saldo_anterior, saldo_posterior, descricao, data_movimento,
    usuario_id, idempotency_key
)
VALUES
    (1,  1, 5,  5, 'SAIDA_PARA_TRANSITO', FALSE, 50000.00, 685000.00, 635000.00, 'Expedicao da Agencia Se para Agencia Pituba',             '2026-07-19 08:00:00', 5, 'seed-mov-op5-origem'),
    (2, 20, 12, 7, 'SAIDA_PARA_TRANSITO', FALSE,100000.00, 490000.00, 390000.00, 'Expedicao da Agencia Londrina para Agencia Se',           '2026-07-19 08:30:00', 4, 'seed-mov-op7-origem'),
    (3, 11, 5,  5, 'ENTRADA_DE_TRANSITO', TRUE,   50000.00,  25000.00,  75000.00, 'Recebimento na Agencia Pituba',                           '2026-07-20 14:00:00', 5, 'seed-mov-op5-destino'),
    (4,  1, 12, 7, 'ENTRADA_DE_TRANSITO', TRUE,  100000.00, 635000.00, 735000.00, 'Recebimento na Agencia Se',                               '2026-07-20 15:00:00', 4, 'seed-mov-op7-destino'),
    (5,  1, 7,  6, 'SAIDA_PARA_TRANSITO', FALSE,100000.00, 735000.00, 635000.00, 'Expedicao da Agencia Se para Agencia Goiania Sul',        '2026-07-22 08:00:00', 6, 'seed-mov-op6-origem'),
    (6, 25, 14, 8, 'SAIDA_PARA_TRANSITO', FALSE,100000.00, 550000.00, 450000.00, 'Expedicao da Agencia Joao Pessoa para Agencia Se',        '2026-07-22 08:30:00', 5, 'seed-mov-op8-origem'),
    (7, 14, 7,  6, 'ENTRADA_DE_TRANSITO', TRUE,   80000.00,  30000.00, 110000.00, 'Recebimento divergente na Agencia Goiania Sul',           '2026-07-23 14:00:00', 6, 'seed-mov-op6-destino'),
    (8,  1, 14, 8, 'ENTRADA_DE_TRANSITO', TRUE,   95000.00, 635000.00, 730000.00, 'Recebimento divergente na Agencia Se',                   '2026-07-23 14:30:00', 5, 'seed-mov-op8-destino'),
    (9,  1, 4,  3, 'SAIDA_PARA_TRANSITO', FALSE,100000.00, 730000.00, 630000.00, 'Expedicao da Agencia Se para Agencia Copacabana',         '2026-07-28 08:00:00', 3, 'seed-mov-op3-origem'),
    (10,15, 11, 4, 'SAIDA_PARA_TRANSITO', FALSE,150000.00, 850000.00, 700000.00, 'Expedicao da Agencia Porto Alegre para Agencia Se',       '2026-07-28 08:30:00', 4, 'seed-mov-op4-origem'),
    (11, 7, 4,  3, 'AJUSTE',              TRUE,   10000.00, 130000.00, 140000.00, 'Ajuste operacional anterior ao recebimento',              '2026-07-28 17:00:00', 2, 'seed-mov-ajuste'),
    (12, 2, NULL,NULL,'SAQUE',             FALSE,  15000.00, 200000.00, 185000.00, 'Saques do movimento diario',                            '2026-07-28 17:00:00', 3, 'seed-mov-saque'),
    (13, 9, NULL,NULL,'DEPOSITO',           TRUE,   25000.00, 180000.00, 205000.00, 'Depositos do movimento diario',                         '2026-07-28 17:15:00', 5, 'seed-mov-deposito'),
    (14, 1, 11, 4, 'ENTRADA_DE_TRANSITO',  TRUE,  150000.00, 630000.00, 780000.00, 'Recebimento na Agencia Se',                             '2026-07-29 08:00:00', 3, 'seed-mov-op4-destino');

-- Evento de criacao para todas as solicitacoes.
INSERT INTO historico_solicitacao_numerario (
    solicitacao_id, operacao_id, evento, status_anterior, status_novo,
    usuario_id, data_evento, justificativa, dados_complementares
)
SELECT
    s.id,
    NULL,
            'SOLICITACAO_CRIADA',
    NULL,
    'PENDENTE',
    s.solicitante_id,
    s.data_criacao,
    s.motivo,
    JSON_OBJECT('tipoOperacao', s.tipo_operacao, 'valor', s.valor_solicitado)
FROM solicitacao_numerario s;

-- Estado final de cada solicitacao para alimentar a linha do tempo.
INSERT INTO historico_solicitacao_numerario (
    solicitacao_id, operacao_id, evento, status_anterior, status_novo,
    usuario_id, data_evento, justificativa, dados_complementares
)
SELECT
    s.id,
    o.id,
    CASE s.status
        WHEN 'APROVADA'         THEN 'SOLICITACAO_APROVADA'
        WHEN 'REJEITADA'        THEN 'SOLICITACAO_REJEITADA'
        WHEN 'EM_EXECUCAO'      THEN 'NUMERARIO_EXPEDIDO'
        WHEN 'CONCLUIDA'        THEN 'SOLICITACAO_CONCLUIDA'
        WHEN 'CANCELADA'        THEN 'SOLICITACAO_CANCELADA'
        WHEN 'COM_DIVERGENCIA'  THEN 'DIVERGENCIA_REGISTRADA'
        ELSE 'SOLICITACAO_CRIADA'
    END,
    CASE WHEN s.status = 'PENDENTE' THEN NULL ELSE 'PENDENTE' END,
    s.status,
    COALESCE(s.cancelado_por_id, s.decisor_id, s.solicitante_id),
    COALESCE(s.data_conclusao, s.data_cancelamento, s.data_decisao, s.data_criacao),
    COALESCE(s.justificativa_cancelamento, s.justificativa_decisao, s.motivo),
    JSON_OBJECT('massa', 'V2', 'cenario', LOWER(s.status))
FROM solicitacao_numerario s
LEFT JOIN operacao_numerario o ON o.solicitacao_id = s.id
WHERE s.status <> 'PENDENTE';

INSERT INTO comando_idempotente (
    idempotency_key, tipo_comando, chave_execucao_unica, operacao_id,
    usuario_id, data_processamento
)
SELECT
    CONCAT('seed-comando-', o.id),
    CASE o.status
        WHEN 'PROGRAMADA'   THEN 'PROGRAMAR'
        WHEN 'EM_SEPARACAO' THEN 'INICIAR_SEPARACAO'
        WHEN 'EM_TRANSITO'  THEN 'EXPEDIR'
        WHEN 'RECEBIDA'     THEN 'RECEBER'
        WHEN 'CONCILIADA'   THEN 'CONCILIAR'
        ELSE 'REGISTRAR_DIVERGENCIA'
    END,
    CONCAT('SEED-', LPAD(o.id, 4, '0')),
    o.id,
    o.programado_por_id,
    o.data_programacao
FROM operacao_numerario o;
