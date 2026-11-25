package br.edu.plataforma.indicadores;

import br.edu.plataforma.contrato.EventoDominio;
import br.edu.plataforma.contrato.Topologia;
import br.edu.plataforma.infra.ConfirmacaoManual;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Recebe somente "curso.concluido". As duas instâncias consomem a MESMA fila
 * (plataforma.indicadores): é o padrão "competing consumers" - o RabbitMQ entrega cada
 * mensagem a apenas uma delas (round-robin, respeitando prefetch=1).
 */
@Component
public class IndicadoresListener {

    private final ConfirmacaoManual confirmacao;
    private final IndicadoresRepositorio repositorio;
    private final long tempoProcessamentoMs;

    public IndicadoresListener(ConfirmacaoManual confirmacao, IndicadoresRepositorio repositorio,
                               @Value("${app.tempo-processamento-ms:400}") long tempoProcessamentoMs) {
        this.confirmacao = confirmacao;
        this.repositorio = repositorio;
        this.tempoProcessamentoMs = tempoProcessamentoMs;
    }

    @RabbitListener(queues = Topologia.FILA_INDICADORES)
    public void receber(EventoDominio evento, Channel canal,
                        @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                        @Header(AmqpHeaders.REDELIVERED) boolean reentregue) throws IOException, InterruptedException {
        // Simula um cálculo mais pesado para tornar visível a divisão do trabalho entre as instâncias.
        Thread.sleep(tempoProcessamentoMs);
        confirmacao.executar(evento, canal, tag, reentregue, repositorio::contabilizar);
    }
}
