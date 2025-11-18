package isi.dan.ms.pedidos.conf;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String STOCK_UPDATE_QUEUE = "stock-update-queue";

    // Configuración para órdenes ejecutadas (descuento de stock)
    public static final String ORDEN_EJECUTADA_QUEUE = "orden.ejecutada.queue";
    public static final String ORDEN_EJECUTADA_EXCHANGE = "orden.ejecutada.exchange";
    public static final String ORDEN_EJECUTADA_ROUTING_KEY = "orden.ejecutada.routing.key";

    // Configuración para devolución de stock
    public static final String STOCK_DEVOLUCION_QUEUE = "stock.devolucion.queue";
    public static final String STOCK_DEVOLUCION_EXCHANGE = "stock.devolucion.exchange";
    public static final String STOCK_DEVOLUCION_ROUTING_KEY = "stock.devolucion";

    // Beans para órdenes ejecutadas
    @Bean
    public Queue stockUpdateQueue() {
        return new Queue(STOCK_UPDATE_QUEUE, true);
    }

    @Bean
    public Queue ordenEjecutadaQueue() {
        return new Queue(ORDEN_EJECUTADA_QUEUE, true);
    }

    @Bean
    public TopicExchange ordenEjecutadaExchange() {
        return new TopicExchange(ORDEN_EJECUTADA_EXCHANGE);
    }

    @Bean
    public Binding ordenEjecutadaBinding() {
        return BindingBuilder
                .bind(ordenEjecutadaQueue())
                .to(ordenEjecutadaExchange())
                .with(ORDEN_EJECUTADA_ROUTING_KEY);
    }

    // Beans para devolución de stock
    @Bean
    public Queue stockDevolucionQueue() {
        return new Queue(STOCK_DEVOLUCION_QUEUE, true);
    }

    @Bean
    public TopicExchange stockDevolucionExchange() {
        return new TopicExchange(STOCK_DEVOLUCION_EXCHANGE);
    }

    @Bean
    public Binding stockDevolucionBinding() {
        return BindingBuilder
                .bind(stockDevolucionQueue())
                .to(stockDevolucionExchange())
                .with(STOCK_DEVOLUCION_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
