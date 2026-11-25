package br.edu.plataforma.auditoria;

import br.edu.plataforma.contrato.EventoDominio;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Efeito: histórico consultável por identificador, tipo e horário do evento. */
@Repository
public class AuditoriaRepositorio {

    private static final Logger log = LoggerFactory.getLogger("EFEITO");
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;

    public AuditoriaRepositorio(JdbcTemplate jdbc, ObjectMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    public void registrar(EventoDominio e, String routingKey) {
        String payload;
        try {
            payload = json.writeValueAsString(e);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
        jdbc.update("INSERT INTO auditoria_eventos (id_evento, tipo, routing_key, ocorrido_em, recebido_em, "
                        + "origem, payload) VALUES (?,?,?,?,?,?,?)",
                e.idEvento(), e.tipo(), routingKey, Timestamp.from(e.ocorridoEm()), Timestamp.from(Instant.now()),
                e.origem(), payload);
        log.info("[AUDITADO] idEvento={} tipo={} ocorridoEm={}", e.idEvento(), e.tipo(), e.ocorridoEm());
    }

    public List<Map<String, Object>> consultar(String idEvento, String tipo, Instant de, Instant ate) {
        StringBuilder sql = new StringBuilder("SELECT * FROM auditoria_eventos WHERE 1=1");
        List<Object> p = new ArrayList<>();
        if (idEvento != null) { sql.append(" AND id_evento = ?"); p.add(idEvento); }
        if (tipo != null) { sql.append(" AND tipo = ?"); p.add(tipo); }
        if (de != null) { sql.append(" AND ocorrido_em >= ?"); p.add(Timestamp.from(de)); }
        if (ate != null) { sql.append(" AND ocorrido_em <= ?"); p.add(Timestamp.from(ate)); }
        sql.append(" ORDER BY ocorrido_em");
        return jdbc.queryForList(sql.toString(), p.toArray());
    }
}
