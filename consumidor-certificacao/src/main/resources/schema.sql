CREATE TABLE IF NOT EXISTS eventos_processados (
    id_evento            VARCHAR(64)  PRIMARY KEY,
    tipo                 VARCHAR(100) NOT NULL,
    instancia            VARCHAR(100) NOT NULL,
    processado_em        TIMESTAMP    NOT NULL,
    duplicatas_ignoradas INT          DEFAULT 0 NOT NULL
);

CREATE TABLE IF NOT EXISTS certificados (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo        VARCHAR(40)  NOT NULL,
    id_evento     VARCHAR(64)  NOT NULL UNIQUE,
    aluno_id      VARCHAR(40)  NOT NULL,
    aluno_nome    VARCHAR(200),
    curso_id      VARCHAR(40)  NOT NULL,
    curso_nome    VARCHAR(200),
    carga_horaria INT,
    concluido_em  TIMESTAMP,
    emitido_em    TIMESTAMP    NOT NULL
);
