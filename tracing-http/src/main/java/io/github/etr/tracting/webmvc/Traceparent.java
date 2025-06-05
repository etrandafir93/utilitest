package io.github.etr.tracting.webmvc;

public record Traceparent(String traceId, String spanId) {

    @Override
    public String toString() {
        return "00-%s-%s-00".formatted(traceId, spanId);
    }
}
