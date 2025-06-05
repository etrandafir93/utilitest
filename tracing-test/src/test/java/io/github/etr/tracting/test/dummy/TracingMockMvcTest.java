package io.github.etr.tracting.test.dummy;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import io.github.etr.junit.lambdas.DoBeforeEach;
import io.github.etr.junit.lambdas.JunitLambdasExtension;
import io.github.etr.tracting.webmvc.HttpTracingExtension;
import io.github.etr.tracting.webmvc.Traceable;
import io.github.etr.tracting.webmvc.Traceparent;

// spotless:off
@SpringBootTest(
    webEnvironment = WebEnvironment.MOCK,
    classes = DummyApp.class,
    properties = {
        "todo.api.url=${wiremock.server.baseUrl}",
        "utilitest.tracing.add-to-mdc=false"
    })
@EnableWireMock
@AutoConfigureObservability
@ExtendWith(HttpTracingExtension.class)
@ExtendWith(JunitLambdasExtension.class)
// spotless:on
class TracingMockMvcTest {

    @InjectWireMock
    WireMockServer todosService;

    @Traceable
    MockMvc mockMvc;

    @DoBeforeEach
    Runnable stub = this::stubTodoEndpoint;

    @RepeatedTest(5)
    void shouldInjectMockMvc() {
        assertThat(mockMvc).isNotNull();
        assertThat(MDC.getCopyOfContextMap())
            .doesNotContainKey("traceId")
            .doesNotContainKey("spanId");
    }

    @Test
    @Disabled("propagation not working with MockMvc")
    void mockMvcShouldPropagateTrace(Traceparent trace) throws Exception {
        todosService.stubFor(WireMock.get("/todos/1")
            .willReturn(WireMock.ok("""
                    {
                      "userId": 1,
                      "id": 1,
                      "title": "fugiat veniam minus",
                      "completed": false
                    }
                    """)
                .withHeader("Content-Type", "application/json")));

        String resp = mockMvc.perform(MockMvcRequestBuilders.get("/api/dummy?todo=1"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThat(resp).isEqualTo("fugiat veniam minus");

        var requestOut = todosService.getServeEvents()
            .getRequests()
            .getFirst()
            .getRequest();

        var traceparentOut = requestOut.getHeaders()
            .getHeader("traceparent");

        assertThat(traceparentOut.values()).hasSize(1)
            .first()
            .asString()
            .contains(trace.traceId())
            .doesNotContain(trace.spanId());
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