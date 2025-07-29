package io.github.etr.tracting.http.test.dummy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import io.github.etr.tracting.http.test.dummy.epic.TaskClient;
import io.github.etr.tracting.http.test.dummy.task.ExternalTodoClient;

@SpringBootApplication
public class DummyApp {

    public static void main(String[] args) {
        SpringApplication.run(DummyApp.class, args);
    }

    @Bean
    ExternalTodoClient externalTodoClient(@Value("${todo.api.url}") String baseUrl, RestClient.Builder builder) {
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(builder.baseUrl(baseUrl)
                .build()))
            .build()
            .createClient(ExternalTodoClient.class);
    }

    @Bean
    TaskClient taskClient(RestClient.Builder builder) {
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(builder.baseUrl("http://localhost:8080")
                .build()))
            .build()
            .createClient(TaskClient.class);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
        ConsumerFactory<String, Object> consumerFactory
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
        factory.setConsumerFactory(consumerFactory);
        ContainerProperties containerProps = factory.getContainerProperties();
        containerProps.setObservationEnabled(true);
        return factory;
    }

    @Bean
    KafkaTemplate<String, ?> kafkaTemplate(
        ProducerFactory<String, ?> producerFactory)
    {
        var template = new KafkaTemplate<>(producerFactory);
        template.setObservationEnabled(true);
        return template;
    }
}
