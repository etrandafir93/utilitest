package io.github.etr.utilitest.junit.lambdas;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a field for execution of a specified method before all tests in a test class are run.
 * This is particularly useful for setup operations or initializing resources that need to be prepared
 * before any tests are executed.
 *
 * <p>
 * It can be applied to test fields that are:
 * <ul>
 *   <li> supported functional interfaces ({@link Runnable}, {@link java.util.function.Supplier}, {@link ThrowingRunnable}, {@link ThrowingSupplier}):
 *
 *      <pre>{@code
 *      static File resource = Files.createTempFile("test", ".tmp").toFile();
 *
 *      @DoBeforeAll
 *      static ThrowingRunnable setup = () -> resource.createNewFile();
 *      }</pre>
 *
 *   </li>
 *   <li> other objects having a method matching the name specified in {@code invoke}.
 *
 *      <pre>{@code
 *          @DoBeforeAll(invoke = "createNewFile")
 *          static File resource = Files.createTempFile("test", ".tmp").toFile();
 *      }</pre>
 *
 *   </li>
 * </ul>
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DoBeforeAll {
    String invoke() default "";
}
