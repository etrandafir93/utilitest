package io.github.etr.tracting.http.test.dummy;

import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.kafka.core.KafkaTemplate;

import io.github.etr.junit.lambdas.DoBeforeEach;
import io.github.etr.tracting.http.test.dummy.epic.CreateEpicCommand;
import io.github.etr.tracting.kafka.KafkaTracingExtension;
import io.github.etr.tracting.kafka.Traceable;
import io.github.etr.tracting.kafka.Traceparent;

@ExtendWith(KafkaTracingExtension.class)
class TracingKafkaTest  extends IntegrationTest {

    //spotless:off
    @DoBeforeEach
    Runnable stubs = () -> stubTodoEndpoint(Map.of(
        1L, "upgrade pom versions",
        2L, "update test assertions"));

    @Test
    // command --kafka--> app
    // app <--REST--> 3rd party
    // app --kafka--> domain event
    void integrationTest(
        @Traceable Traceparent traceparent,
        @Traceable KafkaTemplate<Object, Object> kafkaTemplate
    ) throws Exception {

        // given
        kafkaTemplate.send("create.epic.command",
            new CreateEpicCommand("Migrate to JUnit5", List.of(1L, 2L))).get();

        // when
        var messageOut = consumeOneMessage("epic.created.event", ofSeconds(10));

        // then
        assertThat(messageOut.value()).contains("Migrate to JUnit5")
            .contains("upgrade pom versions")
            .contains("update test assertions");

        var headerOut = messageOut.headers()
            .headers("traceparent")
            .iterator().next();
        assertThat(headerOut)
            .extracting(it -> new String(it.value())).asString()
            .contains(traceparent.traceId())
            .doesNotContain(traceparent.spanId());
    }
    // spotless:on

}