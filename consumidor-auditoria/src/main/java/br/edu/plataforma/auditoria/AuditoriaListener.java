package br.edu.plataforma.auditoria;

import br.edu.plataforma.contrato.EventoDominio;
import br.edu.plataforma.contrato.Topologia;
import br.edu.plataforma.infra.ConfirmacaoManual;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Recebe todos os eventos do cenário (vínculo "#"), inclusive tipos que nenhuma outra regra atende. */
@Component
public class AuditoriaListener {

    private final ConfirmacaoManual confirmacao;
    private final AuditoriaRepositorio repositorio;

    public AuditoriaListener(ConfirmacaoManual confirmacao, AuditoriaRepositorio repositorio) {
        this.confirmacao = confirmacao;
        this.repositorio = repositorio;
    }

    @RabbitListener(queues = Topologia.FILA_AUDITORIA)
    public void receber(EventoDominio evento, Channel canal,
                        @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                        @Header(AmqpHeaders.REDELIVERED) boolean reentregue,
                        @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) throws IOException {
        confirmacao.executar(evento, canal, tag, reentregue, e -> repositorio.registrar(e, routingKey));
    }
}
