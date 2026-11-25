package br.edu.plataforma.contrato;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfigComum {

    /** Declara exchange, filas, DLQs e vínculos (idempotente). */
    @Bean
    public Declarables topologiaPlataforma() {
        return new Declarables(Topologia.declaraveis());
    }

    /** Mensagens trafegam como JSON (contrato {@link EventoDominio}). */
    @Bean
    public MessageConverter conversorJson(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter c = new Jackson2JsonMessageConverter(objectMapper);
        c.setAlwaysConvertToInferredType(true);
        return c;
    }
}
