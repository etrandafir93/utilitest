package io.github.etr.tracting.http;

import static java.util.Arrays.stream;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import io.github.etr.tracting.kafka.Traceable;
import io.github.etr.tracting.kafka.Traceparent;
import java.lang.reflect.Field;
import java.util.function.Supplier;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.WebApplicationContext;

public class HttpTracingExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    public static final String ADD_TO_MDC_PROPERTY_KEY = "utilitest.tracing.add-to-mdc";
    private static final Namespace TEST_TRACING_EXTENSION_STORE =
            Namespace.create(HttpTracingExtension.class.getSimpleName());
    private static final Logger LOG = LoggerFactory.getLogger(HttpTracingExtension.class);

    @Override
    public void beforeEach(ExtensionContext extensionCtx) throws Exception {
        Traceparent traceparent = currentTraceparent(extensionCtx);

        String testName = extensionCtx.getTestMethod().get().getName();
        LOG.info("Starting test execution: {} with traceparent: {}", testName, traceparent);

        Object testInstance = extensionCtx.getRequiredTestInstance();
        stream(testInstance.getClass().getDeclaredFields())
                .filter(it -> it.isAnnotationPresent(Traceable.class))
                .peek(it -> LOG.debug("Injecting field: {} of type: {}", it.getName(), it.getType()))
                .forEach(it -> {
                    switch (it.getType().getSimpleName()) {
                        case "RestClient" -> setq(it, testInstance, () -> currentRestClient(extensionCtx));
                        case "MockMvc" -> setq(it, testInstance, () -> mockMvc(extensionCtx));
                        case "Traceparent" -> setq(it, testInstance, () -> currentTraceparent(extensionCtx));
                        default -> throw new IllegalStateException("Unsupported annotated type: " + it);
                    }
                });
    }

    @Override
    public void afterEach(ExtensionContext extensionCtx) throws Exception {
        try {
            String testName = extensionCtx.getTestMethod().get().getName();
            LOG.info("Finished test execution: {} with traceparent: {}", testName, currentTraceparent(extensionCtx));
            extensionCtx.getStore(TEST_TRACING_EXTENSION_STORE).remove("traceparent");
            extensionCtx.getStore(TEST_TRACING_EXTENSION_STORE).remove("RestClient");

            if (shouldAddTraceToMdc(extensionCtx)) {
                MDC.remove("traceId");
                MDC.remove("spanId");
            }
        } catch (Exception e) {
            LOG.error("Error during afterEach callback for test: {}", extensionCtx.getDisplayName(), e);
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext paramCtx, ExtensionContext extensionCtx)
            throws ParameterResolutionException {
        Class<?> type = paramCtx.getParameter().getType();
        return type.equals(Traceparent.class) || type.equals(RestClient.class);
    }

    @Override
    public Object resolveParameter(ParameterContext paramCtx, ExtensionContext extensionCtx)
            throws ParameterResolutionException {
        String type = paramCtx.getParameter().getType().getSimpleName();
        return switch (type) {
            case "Traceparent" -> currentTraceparent(extensionCtx);
            case "RestClient" -> currentRestClient(extensionCtx);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    private static Traceparent currentTraceparent(ExtensionContext extensionCtx) {
        return extensionCtx
                .getStore(TEST_TRACING_EXTENSION_STORE)
                .getOrComputeIfAbsent("traceparent", __ -> newTraceparent(extensionCtx), Traceparent.class);
    }

    private static Traceparent newTraceparent(ExtensionContext ctx) {
        Traceparent trace = Traceparent.random();
        if (shouldAddTraceToMdc(ctx)) {
            MDC.put("traceId", trace.traceId());
            MDC.put("spanId", trace.spanId());
        }
        return trace;
    }

    private static boolean shouldAddTraceToMdc(ExtensionContext extensionCtx) {
        String value = SpringExtension.getApplicationContext(extensionCtx)
                .getEnvironment()
                .getProperty(ADD_TO_MDC_PROPERTY_KEY, "false");
        return Boolean.parseBoolean(value);
    }

    private static RestClient currentRestClient(ExtensionContext extensionCtx) {
        Traceparent traceparent = currentTraceparent(extensionCtx);
        return extensionCtx
                .getStore(TEST_TRACING_EXTENSION_STORE)
                .getOrComputeIfAbsent(
                        "RestClient",
                        __ -> RestClient.builder()
                                .defaultHeaders(headers -> headers.set("traceparent", traceparent.toString()))
                                .build(),
                        RestClient.class);
    }

    private static MockMvc mockMvc(ExtensionContext extensionCtx) {
        var traceparent = currentTraceparent(extensionCtx);
        var appContext = SpringExtension.getApplicationContext(extensionCtx);
        if (appContext instanceof WebApplicationContext webCtx) {
            return MockMvcBuilders.webAppContextSetup(webCtx)
                    .defaultRequest(get("/").header("traceparent", traceparent.toString()))
                    .build();
        } else {
            throw new IllegalStateException("Not a WebApplicationContext: " + appContext.getClass());
        }
    }

    private static void setq(Field field, Object instance, Supplier<Object> value) {
        try {
            field.setAccessible(true);
            field.set(instance, value.get());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
