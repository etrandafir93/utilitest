package io.github.etr.tracting.http.test.dummy;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import io.github.etr.junit.lambdas.JunitLambdasExtension;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = { "todo.api.url=${wiremock.server.baseUrl}",
    "utilitest.tracing.add-to-mdc=true" })
@EnableWireMock
@AutoConfigureObservability
@ExtendWith(JunitLambdasExtension.class)
abstract class IntegrationTest {

    @Container
    @ServiceConnection
    protected static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));

    @InjectWireMock
    protected WireMockServer todosService;

    protected static ConsumerRecord<String, String> consumeOneMessage(String topic, Duration timeout) {
        try (var consumer = testConsumer()) {
            consumer.subscribe(singletonList(topic));
            ConsumerRecords<String, String> poll = consumer.poll(timeout);
            assertThat(poll).hasSize(1);
            return poll.iterator()
                .next();
        }
    }

    // spotless:off
    protected static KafkaConsumer<String, String> testConsumer() {
        return new KafkaConsumer<>(Map.of(
            "bootstrap.servers", kafka.getBootstrapServers(),
            "auto.offset.reset", "earliest",
            "group.id", "test-group",
            "key.deserializer","org.apache.kafka.common.serialization.StringDeserializer",
            "value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer"));
    }

    protected void stubTodoEndpoint(Map<Long, String> stubs) {
        stubs.forEach((id, title) -> todosService.stubFor(WireMock.get("/todos/" + id)
            .willReturn(WireMock.ok("""
                    {
                      "userId": 1,
                      "id": %d,
                      "title": "%s",
                      "completed": false
                    }
                    """.formatted(id, title))
                .withHeader("Content-Type", "application/json"))));
    }
    // spotless:on


}
