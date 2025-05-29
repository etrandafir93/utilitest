package io.github.etr.junit.lambdas;

@FunctionalInterface
public interface ThrowingSupplier<E extends Exception, T> {

    T get() throws E;
}
