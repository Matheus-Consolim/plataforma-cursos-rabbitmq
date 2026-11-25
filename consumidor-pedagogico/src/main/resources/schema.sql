CREATE TABLE IF NOT EXISTS eventos_processados (
    id_evento            VARCHAR(64)  PRIMARY KEY,
    tipo                 VARCHAR(100) NOT NULL,
    instancia            VARCHAR(100) NOT NULL,
    processado_em        TIMESTAMP    NOT NULL,
    duplicatas_ignoradas INT          DEFAULT 0 NOT NULL
);

CREATE TABLE IF NOT EXISTS acompanhamento_avaliacoes (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_evento     VARCHAR(64)  NOT NULL,
    tipo_evento   VARCHAR(100) NOT NULL,
    etapa         VARCHAR(60)  NOT NULL,
    avaliacao_id  VARCHAR(40),
    aluno_id      VARCHAR(40),
    aluno_nome    VARCHAR(200),
    curso_id      VARCHAR(40),
    nota          DECIMAL(5,2),
    ocorrido_em   TIMESTAMP,
    registrado_em TIMESTAMP    NOT NULL
);
