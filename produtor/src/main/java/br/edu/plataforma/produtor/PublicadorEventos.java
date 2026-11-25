package br.edu.plataforma.produtor;

import br.edu.plataforma.contrato.EventoDominio;
import br.edu.plataforma.contrato.Topologia;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Publica eventos no exchange topic usando o tipo como routing key.
 *
 * <p>Usa publisher confirms (o broker confirma que recebeu e roteou/persistiu a mensagem)
 * e mandatory=true (se nenhuma fila casar com a routing key, a mensagem volta e é registrada
 * no log como NAO ROTEADA). Mensagens são persistentes.</p>
 */
@Service
public class PublicadorEventos {

    private static final Logger log = LoggerFactory.getLogger("PUBLICACAO");

    private final RabbitTemplate rabbit;
    /** Histórico em memória do que foi publicado, para permitir republicar com o mesmo id. */
    private final Map<String, EventoDominio> publicados = Collections.synchronizedMap(new LinkedHashMap<>());

    public PublicadorEventos(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    @PostConstruct
    void configurarRetornos() {
        rabbit.setMandatory(true);
        rabbit.setReturnsCallback(r -> log.warn("[NAO ROTEADA] routingKey={} motivo={} {}",
                r.getRoutingKey(), r.getReplyCode(), r.getReplyText()));
    }

    public EventoDominio publicar(EventoDominio evento) {
        CorrelationData cd = new CorrelationData(evento.idEvento());
        rabbit.convertAndSend(Topologia.EXCHANGE, evento.tipo(), evento, m -> {
            m.getMessageProperties().setMessageId(evento.idEvento());
            m.getMessageProperties().setType(evento.tipo());
            m.getMessageProperties().setTimestamp(Date.from(evento.ocorridoEm()));
            m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            m.getMessageProperties().setAppId(EventoDominio.ORIGEM_PADRAO);
            return m;
        }, cd);
        boolean confirmado = aguardarConfirmacao(cd);
        boolean republicacao = publicados.containsKey(evento.idEvento());
        publicados.put(evento.idEvento(), evento);
        log.info("[PUBLICADO] idEvento={} tipo={} confirmadoPeloBroker={}{}", evento.idEvento(),
                evento.tipo(), confirmado, republicacao ? " (REPUBLICACAO com o mesmo id)" : "");
        return evento;
    }

    private boolean aguardarConfirmacao(CorrelationData cd) {
        try {
            CorrelationData.Confirm c = cd.getFuture().get(5, TimeUnit.SECONDS);
            if (!c.isAck()) log.error("[NACK DO BROKER] idEvento={} motivo={}", cd.getId(), c.getReason());
            return c.isAck();
        } catch (Exception e) {
            log.error("[SEM CONFIRMACAO] idEvento={} erro={}", cd.getId(), e.toString());
            return false;
        }
    }

    public EventoDominio buscar(String idEvento) {
        return publicados.get(idEvento);
    }

    public List<EventoDominio> listar() {
        synchronized (publicados) {
            return new ArrayList<>(publicados.values());
        }
    }
}
