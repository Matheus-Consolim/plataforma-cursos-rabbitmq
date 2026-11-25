package br.edu.plataforma.pedagogico;

import br.edu.plataforma.contrato.EventoDominio;
import br.edu.plataforma.contrato.Topologia;
import br.edu.plataforma.infra.ConfirmacaoManual;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Recebe toda a família "avaliacao" (vínculo "avaliacao.#"), inclusive fatos novos
 * como "avaliacao.contestada", sem nenhuma alteração no broker.
 */
@Component
public class PedagogicoListener {

    private final ConfirmacaoManual confirmacao;
    private final AcompanhamentoRepositorio repositorio;

    public PedagogicoListener(ConfirmacaoManual confirmacao, AcompanhamentoRepositorio repositorio) {
        this.confirmacao = confirmacao;
        this.repositorio = repositorio;
    }

    @RabbitListener(queues = Topologia.FILA_PEDAGOGICO)
    public void receber(EventoDominio evento, Channel canal,
                        @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                        @Header(AmqpHeaders.REDELIVERED) boolean reentregue) throws IOException {
        confirmacao.executar(evento, canal, tag, reentregue, repositorio::registrar);
    }
}
