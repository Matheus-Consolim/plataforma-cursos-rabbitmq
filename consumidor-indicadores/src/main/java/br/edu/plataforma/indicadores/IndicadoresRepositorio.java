package br.edu.plataforma.indicadores;

import br.edu.plataforma.contrato.EventoDominio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/** Efeito: atualiza o total de conclusões por curso (banco compartilhado pelas duas instâncias). */
@Repository
public class IndicadoresRepositorio {

    private static final Logger log = LoggerFactory.getLogger("EFEITO");
    private final JdbcTemplate jdbc;

    public IndicadoresRepositorio(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void contabilizar(EventoDominio e) {
        jdbc.update("MERGE INTO conclusoes_por_curso t "
                        + "USING (VALUES (CAST(? AS VARCHAR(40)), CAST(? AS VARCHAR(200)))) s(curso_id, curso_nome) "
                        + "ON t.curso_id = s.curso_id "
                        + "WHEN MATCHED THEN UPDATE SET total = t.total + 1, atualizado_em = CURRENT_TIMESTAMP "
                        + "WHEN NOT MATCHED THEN INSERT (curso_id, curso_nome, total, atualizado_em) "
                        + "VALUES (s.curso_id, s.curso_nome, 1, CURRENT_TIMESTAMP)",
                e.texto("cursoId"), e.texto("cursoNome"));
        Long total = jdbc.queryForObject("SELECT total FROM conclusoes_por_curso WHERE curso_id = ?",
                Long.class, e.texto("cursoId"));
        log.info("[INDICADOR] curso={} totalConclusoes={}", e.texto("cursoId"), total);
    }

    public List<Map<String, Object>> totais() {
        return jdbc.queryForList("SELECT curso_id, curso_nome, total, atualizado_em FROM conclusoes_por_curso "
                + "ORDER BY curso_id");
    }

    public List<Map<String, Object>> porInstancia() {
        return jdbc.queryForList("SELECT instancia, COUNT(*) AS eventos FROM eventos_processados "
                + "GROUP BY instancia ORDER BY instancia");
    }
}
