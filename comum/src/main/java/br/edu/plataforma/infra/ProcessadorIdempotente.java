package br.edu.plataforma.infra;

import br.edu.plataforma.contrato.EventoDominio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.function.Consumer;

/**
 * Garante que o efeito de um evento ocorra no máximo uma vez por consumidor.
 *
 * <p>Na mesma transação local: (1) insere o idEvento na tabela {@code eventos_processados}
 * (chave primária) e (2) aplica o efeito. Se o idEvento já existe, a inserção falha com
 * chave duplicada, a transação é desfeita e o evento é tratado como DUPLICADO
 * (a mensagem é confirmada, mas nenhum efeito novo acontece).</p>
 *
 * <p>Como a tabela fica no banco do consumidor, a proteção vale também entre as duas
 * instâncias de Indicadores, que compartilham o mesmo banco.</p>
 */
@Component
public class ProcessadorIdempotente {

    public enum Resultado { PROCESSADO, DUPLICADO }

    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final String instancia;

    public ProcessadorIdempotente(JdbcTemplate jdbc, TransactionTemplate tx,
                                  @Value("${app.instancia}") String instancia) {
        this.jdbc = jdbc;
        this.tx = tx;
        this.instancia = instancia;
    }

    public Resultado processar(EventoDominio evento, Consumer<EventoDominio> efeito) {
        Resultado r = tx.execute(status -> {
            try {
                jdbc.update("INSERT INTO eventos_processados (id_evento, tipo, instancia, processado_em) "
                                + "VALUES (?, ?, ?, ?)",
                        evento.idEvento(), evento.tipo(), instancia, Timestamp.from(Instant.now()));
            } catch (DuplicateKeyException e) {
                status.setRollbackOnly();
                return Resultado.DUPLICADO;
            }
            efeito.accept(evento);
            return Resultado.PROCESSADO;
        });
        if (r == Resultado.DUPLICADO) {
            jdbc.update("UPDATE eventos_processados SET duplicatas_ignoradas = duplicatas_ignoradas + 1 "
                    + "WHERE id_evento = ?", evento.idEvento());
        }
        return r;
    }

    public String instancia() {
        return instancia;
    }
}
