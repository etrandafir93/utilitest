package io.github.etr.tracting.kafka;

import static java.util.stream.Collectors.joining;

import java.util.concurrent.ThreadLocalRandom;

public record Traceparent(String traceId, String spanId) {

    @Override
    public String toString() {
        return "00-%s-%s-00".formatted(traceId, spanId);
    }

    public static Traceparent random() {
        return new Traceparent(randomBytes(16), randomBytes(8));
    }

    private static String randomBytes(int length) {
        return ThreadLocalRandom.current()
                .ints(0, 256)
                .mapToObj(it -> String.format("%02x", it))
                .limit(length)
                .collect(joining());
    }
}
