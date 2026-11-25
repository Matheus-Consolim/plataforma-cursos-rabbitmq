package br.edu.plataforma.contrato;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Contrato JSON único para todos os eventos da plataforma.
 *
 * <pre>
 * {
 *   "idEvento":   "9b2f0c1e-...",          identificador único (UUID) - base da idempotência
 *   "tipo":       "curso.concluido",       também é a routing key no exchange topic
 *   "versao":     1,                       versão do contrato
 *   "ocorridoEm": "2026-11-25T13:00:00Z",  momento da ocorrência (ISO-8601, UTC)
 *   "origem":     "plataforma-produtor",
 *   "dados":      { ... }                  dados necessários aos efeitos de cada consumidor
 * }
 * </pre>
 */
public record EventoDominio(
        String idEvento,
        String tipo,
        int versao,
        Instant ocorridoEm,
        String origem,
        Map<String, Object> dados) {

    public static final int VERSAO_ATUAL = 1;
    public static final String ORIGEM_PADRAO = "plataforma-produtor";

    public static EventoDominio novo(String tipo, Map<String, Object> dados) {
        return new EventoDominio(UUID.randomUUID().toString(), tipo, VERSAO_ATUAL,
                Instant.now(), ORIGEM_PADRAO, dados);
    }

    public String texto(String campo) {
        Object v = dados == null ? null : dados.get(campo);
        return v == null ? null : v.toString();
    }

    public Integer inteiro(String campo) {
        Object v = dados == null ? null : dados.get(campo);
        if (v == null) return null;
        if (v instanceof Number n) return n.intValue();
        return Integer.valueOf(v.toString());
    }

    public Double decimal(String campo) {
        Object v = dados == null ? null : dados.get(campo);
        if (v == null) return null;
        if (v instanceof Number n) return n.doubleValue();
        return Double.valueOf(v.toString());
    }
}
