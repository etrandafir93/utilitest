package io.github.etr.utilitest.junit.lambdas;

import static java.util.function.Predicate.not;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.function.ThrowingSupplier;

public class JunitLambdasExtension
        implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        invokeFunctionsAnnotatedWith(DoBeforeAll.class, context);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        invokeFunctionsAnnotatedWith(DoBeforeEach.class, context);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        invokeFunctionsAnnotatedWith(DoAfterAll.class, context);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        invokeFunctionsAnnotatedWith(DoAfterEach.class, context);
    }

    private static void invokeFunctionsAnnotatedWith(
            Class<? extends Annotation> annotationClass, ExtensionContext context) {
        Stream.of(context.getRequiredTestClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(annotationClass))
                .forEach(field -> methodToInvoke(field, annotationClass)
                        .filter(not(String::isEmpty))
                        .ifPresentOrElse(
                                method -> invokeMethodOnFieldInstance(field, context.getTestInstance(), method),
                                () -> invokeFunction(field, context.getTestInstance())));
    }

    private static Optional<String> methodToInvoke(Field field, Class<? extends Annotation> annotation) {
        String methodName =
                switch (annotation.getSimpleName()) {
                    case "DoBeforeAll" -> field.getAnnotation(DoBeforeAll.class).invoke();
                    case "DoBeforeEach" -> field.getAnnotation(DoBeforeEach.class)
                            .invoke();
                    case "DoAfterAll" -> field.getAnnotation(DoAfterAll.class).invoke();
                    case "DoAfterEach" -> field.getAnnotation(DoAfterEach.class).invoke();
                    default -> throw new IllegalArgumentException("Unknown annotation: " + annotation);
                };
        return Optional.ofNullable(methodName).filter(not(String::isEmpty));
    }

    private static void invokeMethodOnFieldInstance(Field field, Optional<Object> testInstance, String methodName) {
        try {
            field.setAccessible(true);
            Object target = field.get(testInstance.orElse(null));
            Method methodToInvoke = target.getClass().getDeclaredMethod(methodName);
            methodToInvoke.setAccessible(true);
            methodToInvoke.invoke(target);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static void invokeFunction(Field field, Optional<Object> testInstance) {
        try {
            field.setAccessible(true);
            Object target = field.get(testInstance.orElse(null));
            switch (target) {
                case ThrowingRunnable<?> it -> it.run();
                case Runnable it -> it.run();
                case Supplier<?> it -> it.get();
                case ThrowingSupplier<?> it -> it.get();
                case Callable<?> it -> it.call();
                default -> throw new IllegalStateException("Unsupported value: " + target);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
