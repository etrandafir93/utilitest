package io.github.etr.assertj.awaitility;

import java.util.function.Consumer;
import java.util.function.Predicate;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ObjectAssert;
import org.awaitility.Awaitility;

public class AssertjAndAwaitility {

    private AssertjAndAwaitility() {}

    /**
     * Creates a builder for an eventual condition.
     * This method is used to define conditions that are expected to be met eventually,
     * within a specified timeout.
     *
     * Example usage:
     * <pre>{@code
     * assertThat(legolas)
     *     .is(eventually()
     *         .having(it -> it.hasFieldOrPropertyWithValue("age", 555)));
     * }</pre>
     *
     * @param <T> the type of the object being tested
     * @return a builder for creating an eventual condition
     */
    public static <T> EventualCondition.Builder<T> eventually() {
        return new EventualCondition.Builder<>();
    }

    /**
     * Creates an eventual condition based on the provided test.
     * This method is used to define a condition that will wait until the specified
     * assertion is satisfied or a timeout occurs.
     *
     * Example usage:
     * <pre>{@code
     * assertThat(legolas)
     *     .is(eventually(it -> it.hasFieldOrPropertyWithValue("age", 555)));
     * }</pre>
     *
     * @param <T> the type of the object being tested
     * @param test a consumer that performs assertions on the object
     * @return an eventual condition for the specified test
     */
    public static <T> UtilitestCondition<T> eventually(Consumer<ObjectAssert<T>> test) {
        return new EventualCondition<>(test);
    }

    /**
     * Creates a prompt condition based on the provided test.
     * This method is used to define a condition that is immediately evaluated.
     *
     * Example usage:
     * <pre>{@code
     * assertThat(legolas)
     *     .is(having(it -> it.hasFieldOrPropertyWithValue("name", "Legolas")));
     * }</pre>
     *
     * @param <T> the type of the object being tested
     * @param test a consumer that performs assertions on the object
     * @return a prompt condition for the specified test
     */
    public static <T> UtilitestCondition<T> having(Consumer<ObjectAssert<T>> test) {
        return new PromptCondition<>(test);
    }

    public abstract static sealed class UtilitestCondition<T> extends Condition<T>
            permits PromptCondition, EventualCondition {
        private final Predicate<T> test;

        public UtilitestCondition(Predicate<T> test, String description) {
            super(test, description);
            this.test = test;
        }

        public UtilitestCondition<T> eventually() {
            return switch (this) {
                case EventualCondition<T> __ -> this;
                case PromptCondition<T> __ -> new EventualCondition<>(this.test);
            };
        }
    }

    public static final class PromptCondition<T> extends UtilitestCondition<T> {
        public PromptCondition(Consumer<ObjectAssert<T>> test) {
            this(consumerToPredicate(test));
        }

        public PromptCondition(Predicate<T> test) {
            super(test, "custom prompt condition");
        }

        private static <T> Predicate<T> consumerToPredicate(Consumer<ObjectAssert<T>> test) {
            return actual -> {
                test.accept(Assertions.assertThat(actual));
                return true;
            };
        }
    }

    public static final class EventualCondition<T> extends UtilitestCondition<T> {
        EventualCondition(Consumer<ObjectAssert<T>> test) {
            this(consumerToPredicate(test));
        }

        EventualCondition(Predicate<T> test) {
            super(eventually(test), "custom eventual condition");
        }

        private static <T> Predicate<T> consumerToPredicate(Consumer<ObjectAssert<T>> test) {
            return eventually(actual -> {
                test.accept(Assertions.assertThat(actual));
                return true;
            });
        }

        public static <T> Predicate<T> eventually(Predicate<T> test) {
            return actual -> {
                Awaitility.await().untilAsserted(() -> test.test(actual));
                return true;
            };
        }

        public static class Builder<T> {
            public UtilitestCondition<T> having(Consumer<ObjectAssert<T>> test) {
                return new EventualCondition<>(test);
            }
        }
    }
}
