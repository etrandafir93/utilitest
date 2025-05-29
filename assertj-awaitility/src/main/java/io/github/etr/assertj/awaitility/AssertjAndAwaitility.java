package io.github.etr.assertj.awaitility;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ObjectAssert;
import org.awaitility.Awaitility;

public class AssertjAndAwaitility {

    public static <T> EventualCondition.Builder<T> eventually() {
        return new EventualCondition.Builder<>();
    }

    public static <T> UtilitestCondition<T> having(Consumer<ObjectAssert<T>> test) {
        return new PromptCondition<>(test, "description");
    }

    public static sealed class UtilitestCondition<T> extends Condition<T> permits PromptCondition, EventualCondition {
        private final Predicate<T> test;
        public UtilitestCondition(Predicate<T> test, String description) {
            super(test, description);
            this.test = test;
        }

        public UtilitestCondition<T> eventually() {
            return switch (this) {
                case EventualCondition<T> __ -> this;
                case PromptCondition<T> __ -> new EventualCondition<>(this.test, this.description().value());
                default -> throw new IllegalStateException("Unexpected value: " + this);
            };
        }

    }


    static final class PromptCondition<T> extends UtilitestCondition<T> {
        public PromptCondition(Predicate<T> test, String description) {
            super(test, description);
        }
        public PromptCondition(Consumer<ObjectAssert<T>> test, String description) {
            super(consumerToPredicate(test), description);
        }
        private static <T> Predicate<T> consumerToPredicate(Consumer<ObjectAssert<T>> test) {
            return actual -> {
                test.accept(Assertions.assertThat(actual));
                return true;
            };
        }
    }

    static final class EventualCondition<T> extends UtilitestCondition<T> {
        public EventualCondition(Predicate<T> test, String description) {
            super(eventually(test), description);
        }
        public EventualCondition(Consumer<ObjectAssert<T>> test, String description) {
            super(consumerToPredicate(test), description);
        }
        private static <T> Predicate<T> consumerToPredicate(Consumer<ObjectAssert<T>> test) {
            return actual -> {
                Awaitility.await().untilAsserted(() ->
                    test.accept(Assertions.assertThat(actual)));
                return true;
            };
        }

        static <T> Predicate<T> eventually(Predicate<T> test) {
            return actual -> {
                Awaitility.await().untilAsserted(
                    () -> test.test(actual));
                return true;
            };
        }

        public static class Builder<T> {
            public UtilitestCondition<T> having(Consumer<ObjectAssert<T>> test) {
                return new EventualCondition<>(test, "description");
            }
        }
    }

}