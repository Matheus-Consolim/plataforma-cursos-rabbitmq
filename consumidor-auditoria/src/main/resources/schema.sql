CREATE TABLE IF NOT EXISTS eventos_processados (
    id_evento            VARCHAR(64)  PRIMARY KEY,
    tipo                 VARCHAR(100) NOT NULL,
    instancia            VARCHAR(100) NOT NULL,
    processado_em        TIMESTAMP    NOT NULL,
    duplicatas_ignoradas INT          DEFAULT 0 NOT NULL
);

CREATE TABLE IF NOT EXISTS auditoria_eventos (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_evento   VARCHAR(64)  NOT NULL UNIQUE,
    tipo        VARCHAR(100) NOT NULL,
    routing_key VARCHAR(200),
    ocorrido_em TIMESTAMP    NOT NULL,
    recebido_em TIMESTAMP    NOT NULL,
    origem      VARCHAR(100),
    payload     CLOB         NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_auditoria_tipo ON auditoria_eventos (tipo);
CREATE INDEX IF NOT EXISTS idx_auditoria_ocorrido ON auditoria_eventos (ocorrido_em);
