package io.github.etr.assertj.awaitility;

import java.util.function.Consumer;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.assertj.core.api.ObjectAssert;
import org.mockito.Mockito;

/**
 * A utility class for integrating Mockito with AssertJ assertions.
 *
 * <p>This class provides static methods to create Mockito argument matchers that use AssertJ's
 * fluent assertions. It simplifies the process of verifying method arguments in Mockito mocks by
 * allowing you to perform detailed and readable assertions.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * FooService mock = Mockito.mock(FooService.class);
 * mock.process(new Account(1L, "John Doe", "johnDoe@gmail.com"));
 *
 * Mockito.verify(mock).process(
 *   MockitoAndAssertJ.argThat(it -> it
 *     .hasFieldOrPropertyWithValue("accountId", 1L)
 *     .hasFieldOrPropertyWithValue("name", "John Doe")
 *     .hasFieldOrPropertyWithValue("email", "johnDoe@gmail.com"))
 * );
 * }</pre>
 *
 * <p>Key methods include:
 *
 * <ul>
 *   <li>{@link #arg(InstanceOfAssertFactory)} - Creates an `Arg` instance with a specified AssertJ
 *       factory.
 *   <li>{@link #argHaving(Consumer)} - Creates a Mockito argument matcher using a generic AssertJ
 *       `ObjectAssert`.
 *   <li>{@link Arg#that(Consumer)} - Applies custom assertions to arguments using the `Arg`
 *       instance.
 * </ul>
 *
 * <p>Author: Emanuel Trandafir
 */
public class MockitoAndAssertJ {

    private MockitoAndAssertJ() {}

    /**
     * Creates an argument matcher using a fluent AssertJ assertion.
     *
     * <p>This method allows you to verify arguments passed to a Mockito mock by applying custom
     * AssertJ assertions. The provided `Consumer` receives an `ObjectAssert` for the argument, which
     * can be used to perform various assertions on the argument's properties.
     *
     * <p>Example usage:
     *
     * <pre>{@code
     * FooService mock = Mockito.mock(FooService.class);
     * mock.process(new Account(1L, "John Doe", "johnDoe@gmail.com"));
     *
     * Mockito.verify(mock).process(
     *   MockitoAndAssertJ.argThat(it -> it
     *     .hasFieldOrPropertyWithValue("accountId", 1L)
     *     .hasFieldOrPropertyWithValue("name", "John Doe")
     *     .hasFieldOrPropertyWithValue("email", "johnDoe@gmail.com"))
     * );
     * }</pre>
     *
     * @param <T> the type of the argument to be matched
     * @param assertion a `Consumer` that accepts an `ObjectAssert` of type `T` to apply custom
     *     assertions
     * @return a Mockito argument matcher that matches arguments satisfying the given assertions
     */
    public static <T> T argHaving(Consumer<ObjectAssert<T>> assertion) {
        return Mockito.argThat(arg -> {
            assertion.accept(Assertions.assertThat(arg));
            return true;
        });
    }

    /**
     * Creates an instance of `Arg` with a specified AssertJ `InstanceOfAssertFactory`.
     *
     * <p>This method initializes an `Arg` object using the provided `InstanceOfAssertFactory`, which
     * defines the type of AssertJ assertions to be used. This allows for more specialized and
     * type-safe assertions on the arguments passed to Mockito mocks.
     *
     * <p>Example usage:
     *
     * <pre>{@code
     * FooService mock = Mockito.mock(FooService.class);
     * mock.processMap(Map.of(1L, "John", 2L, "Bobby"));
     *
     * verify(mock).processMap(
     *   arg(InstanceOfAssertFactories.MAP).that(it -> it
     *     .containsEntry(1L, "John")
     *     .containsEntry(2L, "Bobby"))
     * );
     * }</pre>
     *
     * @param <ASSERT> the type of AssertJ assert class
     * @param type the `InstanceOfAssertFactory` that provides the specific type of assertions
     * @return an `Arg` instance configured with the specified assert factory
     * @see Arg#that(Consumer) for applying custom assertions to arguments using the `Arg` instance
     */
    public static <ASSERT extends AbstractAssert<?, ?>> Arg<ASSERT> arg(InstanceOfAssertFactory<?, ASSERT> type) {
        return new Arg<>(type);
    }

    private static <T, ASSERT extends AbstractAssert<?, ?>> T argHaving(
            InstanceOfAssertFactory<?, ASSERT> assertionType, Consumer<ASSERT> assertion) {
        return Mockito.argThat((T arg) -> {
            assertion.accept(Assertions.assertThat(arg).asInstanceOf(assertionType));
            return true;
        });
    }

    public static class Arg<ASSERT extends AbstractAssert<?, ?>> {

        private final InstanceOfAssertFactory<?, ASSERT> assertionType;

        Arg(InstanceOfAssertFactory<?, ASSERT> type) {
            this.assertionType = type;
        }

        /**
         * Applies a custom assertion on the argument using the `Arg` instance.
         *
         * <p>This method allows you to apply a `Consumer` of AssertJ assertions to the argument. It
         * uses the `Arg` instance to create a Mockito argument matcher that performs the specified
         * assertions on the argument.
         *
         * <p>Example usage:
         *
         * <pre>{@code
         * FooService mock = Mockito.mock(FooService.class);
         * mock.processMap(Map.of(1L, "John", 2L, "Bobby"));
         *
         * verify(mock).processMap(
         *   arg(InstanceOfAssertFactories.MAP).that(it -> it
         *     .containsEntry(1L, "John")
         *     .containsEntry(2L, "Bobby"))
         * );
         * }</pre>
         *
         * @param <T> the type of the argument to be matched
         * @param assertion a `Consumer` that accepts an `ASSERT` instance to apply custom assertions
         * @return a Mockito argument matcher that matches arguments satisfying the given assertions
         * @see MockitoAndAssertJ#arg(InstanceOfAssertFactory) for creating an `Arg` instance
         */
        public <T> T that(Consumer<ASSERT> assertion) {
            return argHaving(assertionType, assertion);
        }
    }
}
