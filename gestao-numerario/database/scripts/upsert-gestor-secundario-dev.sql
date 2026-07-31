-- Credencial fixa somente para desenvolvimento local.
-- Login: gestor2
-- Senha: admin123

USE gestao_numerario;

INSERT INTO usuario (
    nome,
    login,
    senha_hash,
    perfil,
    ativo
)
VALUES (
    'Gestor Desenvolvimento 2',
    'gestor2',
    '$2a$10$4RTuRoL2oSAg9joUUBRZPuv8M29JQoxGHjlThxWeqPzrj.YzysAD2',
    'GESTOR',
    TRUE
)
ON DUPLICATE KEY UPDATE
    nome = VALUES(nome),
    senha_hash = VALUES(senha_hash),
    perfil = VALUES(perfil),
    ativo = VALUES(ativo),
    tentativas_login_falhas = 0,
    bloqueado_ate = NULL;

SELECT
    id,
    nome,
    login,
    perfil,
    ativo
FROM usuario
WHERE login = 'gestor2';
