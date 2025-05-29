package io.github.etr.junit.lambdas;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a field for execution of a specified method after all tests in a test class have run.
 * This is particularly useful for cleanup operations or final assertions that need to be performed
 * after all tests have completed.
 *
 * <p>
 * It can be applied to test fields that are:
 * <ul>
 *   <li> supported functional interfaces ({@link Runnable},{@link java.util.function.Supplier}, {@link ThrowingRunnable}, {@link ThrowingSupplier}):
 *
 *      <pre>{@code
 *      static File resource = Files.createTempFile("test", ".tmp").toFile();
 *
 *      @DoAfterAll
 *      static ThrowingRunnable tearDown = () -> resource.delete();
 *      }</pre>
 *
 *   </li>
 *   <li> other objects having a method matching the name specified in {@code invoke}.
 *
 *      <pre>{@code
 *          @DoAfterAll(invoke = "delete")
 *          static File resource = Files.createTempFile("test", ".tmp").toFile();
 *      }</pre>
 *
 *   </li>
 * </ul>
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DoAfterAll {

    String invoke() default "";
}
