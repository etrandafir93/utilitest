package io.github.etr.utilitest.junit.lambdas;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a field for execution of a specified method before each test in a test class is run.
 * This is particularly useful for setup operations or initializing resources that need to be prepared
 * before every test is executed.
 *
 * <p>
 * It can be applied to test fields that are:
 * <ul>
 *   <li> supported functional interfaces ({@link Runnable}, {@link java.util.function.Supplier}, {@link ThrowingRunnable}, {@link ThrowingSupplier}):
 *
 *      <pre>{@code
 *      static File resource = Files.createTempFile("test", ".tmp").toFile();
 *
 *      @DoBeforeEach
 *      static ThrowingRunnable setup = () -> resource.createNewFile();
 *      }</pre>
 *
 *   </li>
 *   <li> other objects having a method matching the name specified in {@code invoke}.
 *
 *      <pre>{@code
 *          @DoBeforeEach(invoke = "createNewFile")
 *          static File resource = Files.createTempFile("test", ".tmp").toFile();
 *      }</pre>
 *
 *   </li>
 * </ul>
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DoBeforeEach {
    String invoke() default "";
}
