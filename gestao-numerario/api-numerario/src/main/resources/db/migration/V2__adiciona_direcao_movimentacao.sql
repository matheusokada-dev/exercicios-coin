ALTER TABLE movimentacao
    ADD COLUMN entrada BOOLEAN NULL AFTER tipo;

UPDATE movimentacao
SET entrada = CASE
    WHEN tipo IN ('ABASTECIMENTO', 'DEPOSITO') THEN TRUE
    WHEN tipo IN ('RECOLHIMENTO', 'SAQUE') THEN FALSE
    WHEN saldo_posterior >= saldo_anterior THEN TRUE
    ELSE FALSE
END;

ALTER TABLE movimentacao
    MODIFY COLUMN entrada BOOLEAN NOT NULL;
