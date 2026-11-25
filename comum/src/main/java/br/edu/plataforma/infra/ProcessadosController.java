package br.edu.plataforma.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Endpoints comuns a todos os consumidores, para verificação na demonstração. */
@RestController
public class ProcessadosController {

    private final JdbcTemplate jdbc;
    private final String consumidor;
    private final String instancia;

    public ProcessadosController(JdbcTemplate jdbc,
                                 @Value("${spring.application.name}") String consumidor,
                                 @Value("${app.instancia}") String instancia) {
        this.jdbc = jdbc;
        this.consumidor = consumidor;
        this.instancia = instancia;
    }

    /** Eventos já processados por este consumidor (registro de idempotência). */
    @GetMapping("/processados")
    public List<Map<String, Object>> processados() {
        return jdbc.queryForList("SELECT id_evento, tipo, instancia, processado_em, duplicatas_ignoradas "
                + "FROM eventos_processados ORDER BY processado_em");
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("consumidor", consumidor);
        m.put("instancia", instancia);
        m.put("eventosProcessados", jdbc.queryForObject("SELECT COUNT(*) FROM eventos_processados", Long.class));
        m.put("duplicatasIgnoradas", jdbc.queryForObject(
                "SELECT COALESCE(SUM(duplicatas_ignoradas),0) FROM eventos_processados", Long.class));
        return m;
    }
}
