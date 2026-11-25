package br.edu.plataforma.contrato;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;

import java.util.ArrayList;
import java.util.List;

/**
 * Topologia do RabbitMQ (fonte única da verdade, espelhada em rabbitmq/definitions.json).
 *
 * <ul>
 *   <li>Exchange principal: {@code plataforma.eventos} (topic, durável).</li>
 *   <li>Uma fila durável por responsabilidade de consumo.</li>
 *   <li>Cada fila tem dead-letter para {@code plataforma.eventos.dlx} (direct),
 *       que encaminha para {@code <fila>.dlq} as mensagens rejeitadas definitivamente.</li>
 * </ul>
 *
 * Todas as aplicações declaram a topologia completa ao subir (declaração idempotente),
 * então a ordem de inicialização não importa e nenhuma publicação se perde por falta de fila.
 */
public final class Topologia {

    public static final String EXCHANGE = "plataforma.eventos";
    public static final String DLX = "plataforma.eventos.dlx";

    public static final String FILA_CERTIFICACAO = "plataforma.certificacao";
    public static final String FILA_COMUNICACAO = "plataforma.comunicacao";
    public static final String FILA_PEDAGOGICO = "plataforma.pedagogico";
    public static final String FILA_AUDITORIA = "plataforma.auditoria";
    public static final String FILA_INDICADORES = "plataforma.indicadores";

    /** Matriz de vínculos: fila -> padrões de routing key. */
    public static final List<String[]> VINCULOS = List.of(
            new String[]{FILA_CERTIFICACAO, TiposEvento.CURSO_CONCLUIDO},
            new String[]{FILA_COMUNICACAO, TiposEvento.CURSO_CONCLUIDO},
            new String[]{FILA_COMUNICACAO, TiposEvento.CURSO_ABANDONADO},
            new String[]{FILA_COMUNICACAO, TiposEvento.AVALIACAO_REVISADA},
            new String[]{FILA_PEDAGOGICO, "avaliacao.#"},
            new String[]{FILA_AUDITORIA, "#"},
            new String[]{FILA_INDICADORES, TiposEvento.CURSO_CONCLUIDO}
    );

    public static final List<String> FILAS = List.of(
            FILA_CERTIFICACAO, FILA_COMUNICACAO, FILA_PEDAGOGICO, FILA_AUDITORIA, FILA_INDICADORES);

    private Topologia() {
    }

    public static List<Declarable> declaraveis() {
        List<Declarable> d = new ArrayList<>();
        TopicExchange exchange = new TopicExchange(EXCHANGE, true, false);
        DirectExchange dlx = new DirectExchange(DLX, true, false);
        d.add(exchange);
        d.add(dlx);
        for (String fila : FILAS) {
            Queue q = QueueBuilder.durable(fila)
                    .deadLetterExchange(DLX)
                    .deadLetterRoutingKey(fila)
                    .build();
            Queue dlq = QueueBuilder.durable(fila + ".dlq").build();
            d.add(q);
            d.add(dlq);
            d.add(BindingBuilder.bind(dlq).to(dlx).with(fila));
        }
        for (String[] v : VINCULOS) {
            d.add(new Binding(v[0], Binding.DestinationType.QUEUE, EXCHANGE, v[1], null));
        }
        return d;
    }
}
