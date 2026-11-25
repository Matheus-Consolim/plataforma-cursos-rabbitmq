package br.edu.plataforma.infra;

import br.edu.plataforma.contrato.EventoDominio;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Confirmação de consumo vinculada ao sucesso do processamento (acknowledge-mode: manual).
 *
 * <ul>
 *   <li><b>basicAck</b> só depois que a transação do efeito foi confirmada no banco
 *       (ou quando o evento é reconhecido como duplicado: o efeito já existe).</li>
 *   <li>Se o processamento lança exceção: <b>basicNack com requeue</b> na primeira entrega
 *       (falha transitória tem nova chance) e <b>basicNack sem requeue</b> se a mensagem
 *       já era uma reentrega - ela vai para a DLQ {@code <fila>.dlq} e não trava a fila.</li>
 *   <li>Se o processo cair antes do ack, o RabbitMQ reentrega a mensagem quando o canal fecha;
 *       a idempotência evita efeito duplicado nesse caso.</li>
 * </ul>
 */
@Component
public class ConfirmacaoManual {

    private static final Logger log = LoggerFactory.getLogger("EVENTOS");

    private final ProcessadorIdempotente processador;
    private final String consumidor;

    public ConfirmacaoManual(ProcessadorIdempotente processador,
                             @Value("${spring.application.name}") String consumidor) {
        this.processador = processador;
        this.consumidor = consumidor;
    }

    public void executar(EventoDominio evento, Channel canal, long tag, boolean reentregue,
                         Consumer<EventoDominio> efeito) throws IOException {
        try {
            simularFalhaSeSolicitado(evento);
            ProcessadorIdempotente.Resultado r = processador.processar(evento, efeito);
            canal.basicAck(tag, false);
            log.info("[ACK] consumidor={} instancia={} idEvento={} tipo={} resultado={}{}",
                    consumidor, processador.instancia(), evento.idEvento(), evento.tipo(), r,
                    r == ProcessadorIdempotente.Resultado.DUPLICADO ? " (efeito NAO repetido)" : "");
        } catch (Exception e) {
            boolean requeue = !reentregue;
            canal.basicNack(tag, false, requeue);
            log.warn("[NACK] consumidor={} instancia={} idEvento={} tipo={} requeue={} motivo={}",
                    consumidor, processador.instancia(), evento.idEvento(), evento.tipo(), requeue,
                    e.getMessage());
        }
    }

    /** Permite demonstrar o nack: dados.simularFalhaEm = nome do consumidor. */
    private void simularFalhaSeSolicitado(EventoDominio evento) {
        String alvo = evento.texto("simularFalhaEm");
        if (alvo != null && (alvo.equals(consumidor) || alvo.equals("todos"))) {
            throw new IllegalStateException("falha simulada para demonstracao");
        }
    }
}
