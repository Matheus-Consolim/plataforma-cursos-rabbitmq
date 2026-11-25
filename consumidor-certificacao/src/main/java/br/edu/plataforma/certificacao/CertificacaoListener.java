package br.edu.plataforma.certificacao;

import br.edu.plataforma.contrato.EventoDominio;
import br.edu.plataforma.contrato.Topologia;
import br.edu.plataforma.infra.ConfirmacaoManual;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Recebe somente "curso.concluido" (vínculo exato na fila plataforma.certificacao). */
@Component
public class CertificacaoListener {

    private final ConfirmacaoManual confirmacao;
    private final CertificadoRepositorio repositorio;

    public CertificacaoListener(ConfirmacaoManual confirmacao, CertificadoRepositorio repositorio) {
        this.confirmacao = confirmacao;
        this.repositorio = repositorio;
    }

    @RabbitListener(queues = Topologia.FILA_CERTIFICACAO)
    public void receber(EventoDominio evento, Channel canal,
                        @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                        @Header(AmqpHeaders.REDELIVERED) boolean reentregue) throws IOException {
        confirmacao.executar(evento, canal, tag, reentregue, repositorio::emitir);
    }
}
