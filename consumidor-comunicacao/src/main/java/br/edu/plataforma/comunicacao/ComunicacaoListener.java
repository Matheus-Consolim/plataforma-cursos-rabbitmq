package br.edu.plataforma.comunicacao;

import br.edu.plataforma.contrato.EventoDominio;
import br.edu.plataforma.contrato.Topologia;
import br.edu.plataforma.infra.ConfirmacaoManual;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Recebe "curso.concluido", "curso.abandonado" e "avaliacao.revisada" (3 vínculos exatos). */
@Component
public class ComunicacaoListener {

    private final ConfirmacaoManual confirmacao;
    private final AvisoRepositorio avisos;

    public ComunicacaoListener(ConfirmacaoManual confirmacao, AvisoRepositorio avisos) {
        this.confirmacao = confirmacao;
        this.avisos = avisos;
    }

    @RabbitListener(queues = Topologia.FILA_COMUNICACAO)
    public void receber(EventoDominio evento, Channel canal,
                        @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                        @Header(AmqpHeaders.REDELIVERED) boolean reentregue) throws IOException {
        confirmacao.executar(evento, canal, tag, reentregue, avisos::registrar);
    }
}
