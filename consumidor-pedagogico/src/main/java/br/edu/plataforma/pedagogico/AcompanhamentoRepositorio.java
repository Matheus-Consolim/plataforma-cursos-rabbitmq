package br.edu.plataforma.pedagogico;

import br.edu.plataforma.contrato.EventoDominio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Efeito: registra o ciclo das avaliações (envio, revisão e qualquer novo fato da família)
 * com aluno, referência da avaliação e nota quando disponível.
 */
@Repository
public class AcompanhamentoRepositorio {

    private static final Logger log = LoggerFactory.getLogger("EFEITO");
    private final JdbcTemplate jdbc;

    public AcompanhamentoRepositorio(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void registrar(EventoDominio e) {
        // "avaliacao.enviada" -> "ENVIADA"; um fato novo como "avaliacao.contestada" -> "CONTESTADA"
        String etapa = e.tipo().substring(e.tipo().indexOf('.') + 1).toUpperCase();
        Double nota = e.decimal("nota");
        jdbc.update("INSERT INTO acompanhamento_avaliacoes (id_evento, tipo_evento, etapa, avaliacao_id, aluno_id, "
                        + "aluno_nome, curso_id, nota, ocorrido_em, registrado_em) VALUES (?,?,?,?,?,?,?,?,?,?)",
                e.idEvento(), e.tipo(), etapa, e.texto("avaliacaoId"), e.texto("alunoId"), e.texto("alunoNome"),
                e.texto("cursoId"), nota, Timestamp.from(e.ocorridoEm()), Timestamp.from(Instant.now()));
        log.info("[ACOMPANHAMENTO] etapa={} avaliacao={} aluno={} nota={}",
                etapa, e.texto("avaliacaoId"), e.texto("alunoNome"), nota == null ? "-" : nota);
    }

    public List<Map<String, Object>> listar(String avaliacaoId) {
        if (avaliacaoId != null) {
            return jdbc.queryForList("SELECT * FROM acompanhamento_avaliacoes WHERE avaliacao_id = ? ORDER BY id",
                    avaliacaoId);
        }
        return jdbc.queryForList("SELECT * FROM acompanhamento_avaliacoes ORDER BY id");
    }
}
