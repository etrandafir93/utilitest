package io.github.etr.utilitest.lambda;

@FunctionalInterface
public interface ThrowingRunnable<E extends Exception> {

    void run() throws RuntimeException;
}
