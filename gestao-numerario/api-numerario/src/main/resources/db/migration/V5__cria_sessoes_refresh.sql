CREATE TABLE refresh_token (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    token_hash CHAR(64) NOT NULL,
    criado_em TIMESTAMP(6) NOT NULL,
    expira_em TIMESTAMP(6) NOT NULL,
    revogado_em TIMESTAMP(6) NULL,

    CONSTRAINT pk_refresh_token PRIMARY KEY (id),
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id)
) ENGINE = InnoDB;

CREATE INDEX ix_refresh_token_usuario_ativo
    ON refresh_token (usuario_id, revogado_em, expira_em);
