CREATE TABLE IF NOT EXISTS eventos_processados (
    id_evento            VARCHAR(64)  PRIMARY KEY,
    tipo                 VARCHAR(100) NOT NULL,
    instancia            VARCHAR(100) NOT NULL,
    processado_em        TIMESTAMP    NOT NULL,
    duplicatas_ignoradas INT          DEFAULT 0 NOT NULL
);

CREATE TABLE IF NOT EXISTS conclusoes_por_curso (
    curso_id      VARCHAR(40)  PRIMARY KEY,
    curso_nome    VARCHAR(200),
    total         BIGINT       NOT NULL,
    atualizado_em TIMESTAMP    NOT NULL
);
