CREATE TABLE IF NOT EXISTS eventos_processados (
    id_evento            VARCHAR(64)  PRIMARY KEY,
    tipo                 VARCHAR(100) NOT NULL,
    instancia            VARCHAR(100) NOT NULL,
    processado_em        TIMESTAMP    NOT NULL,
    duplicatas_ignoradas INT          DEFAULT 0 NOT NULL
);

CREATE TABLE IF NOT EXISTS avisos (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_evento    VARCHAR(64)   NOT NULL,
    tipo_aviso   VARCHAR(40)   NOT NULL,
    aluno_id     VARCHAR(40),
    destinatario VARCHAR(200),
    canal        VARCHAR(40)   NOT NULL,
    mensagem     VARCHAR(1000) NOT NULL,
    criado_em    TIMESTAMP     NOT NULL
);
