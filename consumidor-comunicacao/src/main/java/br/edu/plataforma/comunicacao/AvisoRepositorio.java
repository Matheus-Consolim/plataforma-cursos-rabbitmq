package br.edu.plataforma.comunicacao;

import br.edu.plataforma.contrato.EventoDominio;
import br.edu.plataforma.contrato.TiposEvento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Efeito: registra avisos de conclusão, abandono ou revisão de avaliação.
 * O envio real (e-mail/push) é simulado: o aviso fica gravado com canal "email-simulado".
 */
@Repository
public class AvisoRepositorio {

    private static final Logger log = LoggerFactory.getLogger("EFEITO");
    private final JdbcTemplate jdbc;

    public AvisoRepositorio(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void registrar(EventoDominio e) {
        String aluno = e.texto("alunoNome");
        String tipoAviso;
        String mensagem;
        switch (e.tipo()) {
            case TiposEvento.CURSO_CONCLUIDO -> {
                tipoAviso = "CONCLUSAO";
                mensagem = "Parabens, " + aluno + "! Voce concluiu o curso " + e.texto("cursoNome")
                        + " (" + e.inteiro("cargaHoraria") + "h). Seu certificado sera emitido.";
            }
            case TiposEvento.CURSO_ABANDONADO -> {
                tipoAviso = "ABANDONO";
                mensagem = "Ola, " + aluno + ". Sentimos sua falta no curso " + e.texto("cursoNome")
                        + " (" + e.texto("percentualConcluido") + "% concluido). Que tal retomar?";
            }
            case TiposEvento.AVALIACAO_REVISADA -> {
                tipoAviso = "REVISAO_AVALIACAO";
                mensagem = "Ola, " + aluno + ". Sua avaliacao " + e.texto("avaliacaoId")
                        + " foi revisada. Nota: " + e.texto("nota") + ".";
            }
            default -> {
                // Não deveria chegar (a fila só tem os 3 vínculos acima); registrado para rastreio.
                log.warn("[AVISO IGNORADO] tipo {} nao possui regra de comunicacao", e.tipo());
                return;
            }
        }
        jdbc.update("INSERT INTO avisos (id_evento, tipo_aviso, aluno_id, destinatario, canal, mensagem, criado_em) "
                        + "VALUES (?,?,?,?,?,?,?)",
                e.idEvento(), tipoAviso, e.texto("alunoId"), aluno, "email-simulado", mensagem,
                Timestamp.from(Instant.now()));
        log.info("[AVISO REGISTRADO] tipo={} destinatario={} mensagem=\"{}\"", tipoAviso, aluno, mensagem);
    }

    public List<Map<String, Object>> listar(String tipoAviso) {
        if (tipoAviso != null) {
            return jdbc.queryForList("SELECT * FROM avisos WHERE tipo_aviso = ? ORDER BY id", tipoAviso);
        }
        return jdbc.queryForList("SELECT * FROM avisos ORDER BY id");
    }
}
