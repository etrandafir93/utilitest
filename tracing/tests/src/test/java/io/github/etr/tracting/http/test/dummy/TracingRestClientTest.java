package io.github.etr.tracting.http.test.dummy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.web.client.RestClient;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import io.github.etr.junit.lambdas.DoBeforeEach;
import io.github.etr.junit.lambdas.JunitLambdasExtension;
import io.github.etr.tracting.http.HttpTracingExtension;
import io.github.etr.tracting.kafka.Traceable;
import io.github.etr.tracting.kafka.Traceparent;

// spotless:off
@SpringBootTest(
    webEnvironment = WebEnvironment.DEFINED_PORT,
    properties = {
        "todo.api.url=${wiremock.server.baseUrl}",
        "utilitest.tracing.add-to-mdc=true"
    })
@EnableWireMock
@AutoConfigureObservability // !! --> I can include it inside  TracingRestClientExtension !!
@ExtendWith({HttpTracingExtension.class, JunitLambdasExtension.class})
// spotless:on
class TracingRestClientTest {

    @InjectWireMock
    WireMockServer todosService;

    @Traceable
    RestClient restClient;

    @Traceable
    Traceparent trace;

    @DoBeforeEach
    Runnable stub = this::stubTodoEndpoint;

    @Test
    void restClientShouldPropagateTrace_fieldInjection() {
        // when
        String resp = restClient.get()
            .uri("http://localhost:8080/api/tasks/1/title")
            .retrieve()
            .body(String.class);

        assertThat(resp)
            .isEqualTo("fugiat veniam minus");

        // then
        var requestsOut = todosService.getServeEvents()
            .getRequests();
        assertThat(requestsOut)
            .hasSize(1);

        // and
        var traceparentOut = requestsOut.getFirst()
            .getRequest()
            .getHeaders()
            .getHeader("traceparent");

        assertThat(traceparentOut.values())
            .hasSize(1).first()
            .asString()
            .contains(trace.traceId())
            .doesNotContain(trace.spanId());
    }

    @Test
    void restClientShouldPropagateTrace_paramInjection(Traceparent traceParam, RestClient restClientPram) {
        // when
        String resp = restClientPram.get()
            .uri("http://localhost:8080/api/tasks/1/title")
            .retrieve()
            .body(String.class);

        assertThat(resp)
            .isEqualTo("fugiat veniam minus");

        // then
        var requestsOut = todosService.getServeEvents()
            .getRequests();
        assertThat(requestsOut)
            .hasSize(1);

        // and
        var traceparentOut = requestsOut.getFirst()
            .getRequest()
            .getHeaders()
            .getHeader("traceparent");

        assertThat(traceparentOut.values())
            .hasSize(1).first()
            .asString()
            .contains(traceParam.traceId())
            .doesNotContain(traceParam.spanId());
    }

    private StubMapping stubTodoEndpoint() {
        return todosService.stubFor(WireMock.get("/todos/1")
            .willReturn(WireMock.ok("""
                    {
                      "userId": 1,
                      "id": 1,
                      "title": "fugiat veniam minus",
                      "completed": false
                    }
                    """)
                .withHeader("Content-Type", "application/json")));
    }
}