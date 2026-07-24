ALTER TABLE usuario
    ADD COLUMN tentativas_login_falhas INT NOT NULL DEFAULT 0,
    ADD COLUMN bloqueado_ate TIMESTAMP(6) NULL;

ALTER TABLE usuario
    ADD CONSTRAINT ck_usuario_tentativas_login_falhas
        CHECK (tentativas_login_falhas >= 0);
