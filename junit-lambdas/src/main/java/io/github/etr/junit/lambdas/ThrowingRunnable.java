package io.github.etr.junit.lambdas;

@FunctionalInterface
public interface ThrowingRunnable<E extends Exception> {

    void run() throws E;
}
