package io.github.etr.tracting.kafka;

import static java.util.Arrays.stream;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class KafkaTracingExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    public static final String ADD_TO_MDC_PROPERTY_KEY = "utilitest.tracing.add-to-mdc";
    private static final ExtensionContext.Namespace TEST_TRACING_EXTENSION_STORE =
            ExtensionContext.Namespace.create(KafkaTracingExtension.class.getSimpleName());
    private static final Logger LOG = LoggerFactory.getLogger(KafkaTracingExtension.class);

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
                        case "KafkaTemplate" -> setq(it, testInstance, () -> currentKafkaTemplate(extensionCtx));
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
            extensionCtx.getStore(TEST_TRACING_EXTENSION_STORE).remove("kafkaTemplate");

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
        return type.equals(Traceparent.class) || type.equals(KafkaTemplate.class);
    }

    @Override
    public Object resolveParameter(ParameterContext paramCtx, ExtensionContext extensionCtx)
            throws ParameterResolutionException {
        String type = paramCtx.getParameter().getType().getSimpleName();
        return switch (type) {
            case "Traceparent" -> currentTraceparent(extensionCtx);
            case "KafkaTemplate" -> currentKafkaTemplate(extensionCtx);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    private static Traceparent currentTraceparent(ExtensionContext extensionCtx) {
        return extensionCtx
                .getStore(TEST_TRACING_EXTENSION_STORE)
                .getOrComputeIfAbsent("traceparent", __ -> newTraceparent(extensionCtx), Traceparent.class);
    }

    private static KafkaTemplate currentKafkaTemplate(ExtensionContext extensionCtx) {
        return extensionCtx
                .getStore(TEST_TRACING_EXTENSION_STORE)
                .getOrComputeIfAbsent("kafkaTemplate", __ -> newKafkaTemplate(extensionCtx), KafkaTemplate.class);
    }

    private static KafkaTemplate newKafkaTemplate(ExtensionContext extensionCtx) {
        DefaultKafkaProducerFactory<?, ?> producerFactory =
                SpringExtension.getApplicationContext(extensionCtx).getBean(DefaultKafkaProducerFactory.class);

        var template = new KafkaTemplate<>(producerFactory);
        template.setProducerInterceptor(new TraceparentHeaderInterceptor(() -> currentTraceparent(extensionCtx)));
        return template;
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

    private static void setq(Field field, Object instance, Supplier<Object> value) {
        try {
            field.setAccessible(true);
            field.set(instance, value.get());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static class TraceparentHeaderInterceptor implements ProducerInterceptor {
        private final Supplier<Traceparent> currentTraceparent;

        @Override
        public ProducerRecord onSend(ProducerRecord producerRecord) {
            producerRecord
                    .headers()
                    .add("traceparent", currentTraceparent.get().toString().getBytes());
            return producerRecord;
        }

        @Override
        public void onAcknowledgement(RecordMetadata recordMetadata, Exception e) {}

        @Override
        public void close() {}

        @Override
        public void configure(Map<String, ?> map) {}

        private TraceparentHeaderInterceptor(Supplier<Traceparent> currentTraceparent) {
            this.currentTraceparent = currentTraceparent;
        }
    }
}
