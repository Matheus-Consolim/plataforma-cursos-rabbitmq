package br.edu.plataforma.certificacao;

import br.edu.plataforma.contrato.EventoDominio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Repository
public class CertificadoRepositorio {

    private static final Logger log = LoggerFactory.getLogger("EFEITO");
    private final JdbcTemplate jdbc;

    public CertificadoRepositorio(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Efeito: gera um registro de certificado com aluno, curso e carga horária. */
    public void emitir(EventoDominio e) {
        String codigo = "CERT-" + e.idEvento().substring(0, 8).toUpperCase();
        jdbc.update("INSERT INTO certificados (codigo, id_evento, aluno_id, aluno_nome, curso_id, curso_nome, "
                        + "carga_horaria, concluido_em, emitido_em) VALUES (?,?,?,?,?,?,?,?,?)",
                codigo, e.idEvento(), e.texto("alunoId"), e.texto("alunoNome"), e.texto("cursoId"),
                e.texto("cursoNome"), e.inteiro("cargaHoraria"), Timestamp.from(e.ocorridoEm()),
                Timestamp.from(Instant.now()));
        log.info("[CERTIFICADO EMITIDO] codigo={} aluno={} curso={} cargaHoraria={}h",
                codigo, e.texto("alunoNome"), e.texto("cursoNome"), e.inteiro("cargaHoraria"));
    }

    public List<Map<String, Object>> listar(String idEvento) {
        if (idEvento != null) {
            return jdbc.queryForList("SELECT * FROM certificados WHERE id_evento = ?", idEvento);
        }
        return jdbc.queryForList("SELECT * FROM certificados ORDER BY id");
    }
}
