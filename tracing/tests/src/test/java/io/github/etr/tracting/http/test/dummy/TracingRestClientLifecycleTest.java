package io.github.etr.tracting.http.test.dummy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.web.client.RestClient;
import org.wiremock.spring.EnableWireMock;

import io.github.etr.junit.lambdas.JunitLambdasExtension;
import io.github.etr.tracting.http.HttpTracingExtension;
import io.github.etr.tracting.kafka.Traceable;
import io.github.etr.tracting.kafka.Traceparent;

@SpringBootTest(properties = {
    "todo.api.url=http://dummy.api.url",
    "utilitest.tracing.add-to-mdc=true"
})
@ExtendWith({HttpTracingExtension.class, JunitLambdasExtension.class})
class TracingRestClientLifecycleTest {

    @Traceable
    RestClient restClientField;
    @Traceable
    Traceparent traceparentField;

    @RepeatedTest(5)
    void shouldInjectTraceparent(Traceparent traceparentParam) {
        assertThat(traceparentParam)
            .isNotNull()
            .isEqualTo(traceparentField);

        assertThat(MDC.getCopyOfContextMap())
            .containsEntry("traceId", traceparentParam.traceId())
            .containsEntry("spanId", traceparentParam.spanId());
    }
    @RepeatedTest(5)
    void shouldInjectRestClient(RestClient restClientParam) {
        assertThat(restClientParam)
            .isNotNull()
            .isEqualTo(restClientField);
    }

    @RepeatedTest(5)
    void shouldInjectBoth(Traceparent traceparentParam, RestClient restClientParam) {
        assertThat(traceparentParam)
            .isNotNull()
            .isEqualTo(traceparentField);

        assertThat(restClientParam)
            .isNotNull()
            .isEqualTo(restClientField);
    }

}