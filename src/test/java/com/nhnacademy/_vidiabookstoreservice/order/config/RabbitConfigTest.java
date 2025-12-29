package com.nhnacademy._vidiabookstoreservice.order.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RabbitConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(RabbitConfig.class)
            .withBean(ConnectionFactory.class, () -> mock(ConnectionFactory.class));

    @Test
    @DisplayName("TTL(대기) 큐가 올바른 속성(15분, DLX 설정)을 가지고 생성되는가?")
    void waitingQueue_Config_Check() {
        contextRunner.run(context -> {
            assertThat(context).hasBean("waitingQueue");

            Queue waitingQueue = context.getBean("waitingQueue", Queue.class);

            assertThat(waitingQueue.getArguments().get("x-message-ttl")).isEqualTo(15 * 60 * 1000);
            assertThat(waitingQueue.getArguments().get("x-dead-letter-exchange")).isEqualTo("processing.exchange");
            assertThat(waitingQueue.getArguments().get("x-dead-letter-routing-key")).isEqualTo("order.process");
        });
    }

    @Test
    @DisplayName("Binding 설정이 오타 없이 정확하게 연결되어 있는가?")
    void binding_Config_Check() {
        contextRunner.run(context -> {
            Binding processingBinding = context.getBean("processingBinding", Binding.class);
            assertThat(processingBinding.getExchange()).isEqualTo("processing.exchange");
            assertThat(processingBinding.getDestination()).isEqualTo("processing.queue");
            assertThat(processingBinding.getRoutingKey()).isEqualTo("order.process");

            Binding waitingBinding = context.getBean("waitingBinding", Binding.class);
            assertThat(waitingBinding.getExchange()).isEqualTo("waiting.exchange");
            assertThat(waitingBinding.getDestination()).isEqualTo("waiting.queue");
            assertThat(waitingBinding.getRoutingKey()).isEqualTo("order.wait");
        });
    }

}