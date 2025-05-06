package io.github.etr.utilitest.junit.lambdas;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a field for execution of a specified method after each test in a test class is run.
 * This is particularly useful for cleanup operations or releasing resources that need to be handled
 * after every test is executed.
 *
 * <p>
 * It can be applied to test fields that are:
 * <ul>
 *   <li> supported functional interfaces ({@link Runnable}, {@link java.util.function.Supplier}, {@link ThrowingRunnable}, {@link ThrowingSupplier}):
 *
 *      <pre>{@code
 *      static Resource resource = ...;
 *
 *      @DoAfterEach
 *      static ThrowingRunnable cleanup = () -> resource.reset();
 *      }</pre>
 *
 *   </li>
 *   <li> other objects having a method matching the name specified in {@code invoke}.
 *
 *      <pre>{@code
 *          @DoAfterEach(invoke = "reset")
 *          static Resource resource = ...;
 *      }</pre>
 *
 *   </li>
 * </ul>
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DoAfterEach {
    String invoke() default "";
}
