package io.github.etr.utilitest.junit.lambdas;

@FunctionalInterface
public interface ThrowingRunnable<E extends Exception> {

    void run() throws E;
}
