package io.github.etr.assertj.awaitility;

import static io.github.etr.assertj.awaitility.AssertjAndAwaitility.eventually;
import static io.github.etr.assertj.awaitility.AssertjAndAwaitility.having;
import static java.util.concurrent.CompletableFuture.runAsync;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.stream.IntStream;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.CONCURRENT)
class AssertjAndAwaitlityTest {

    static {
        Awaitility.setDefaultPollInterval(Duration.ofMillis(1));
        Awaitility.setDefaultTimeout(Duration.ofSeconds(5));
    }

    // spotless:off
    @Test
    void promptCondition_shouldFail() {
        Elf legoals = new Elf("Legoals");

        assertThrows(AssertionError.class, () ->
            assertThat(legoals)
                .is(having(
                    it -> it.hasFieldOrPropertyWithValue("age", 100)))
        );
    }

    @Test
    void promptCondition_shouldPass() {
        Elf legoals = new Elf("Legolas");

        assertThat(legoals)
            .is(having(
                it -> it.hasFieldOrPropertyWithValue("name", "Legolas")));
    }

    @Test
    void eventualCondition_shouldPass() {
        Elf legoals = new Elf("Legolas");

        runAsync(() -> IntStream.range(0,1_000)
            .peek(__ -> sleep(5L))
            .forEach(legoals::setAge));

        assertThat(legoals)
            .is(having(
                    it -> it.hasFieldOrPropertyWithValue("age", 555))
               .eventually());
    }

    @Test
    void eventualCondition_shouldPass_3() {
        Elf legoals = new Elf("Legolas");

        runAsync(() -> IntStream.range(0,1_000)
            .peek(__ -> sleep(5L))
            .forEach(legoals::setAge));

        assertThat(legoals)
            .is(eventually(
                it -> it.hasFieldOrPropertyWithValue("age", 555)));
    }


    @Test
    void eventualCondition_shouldPass_2() {
        Elf legoals = new Elf("Legolas");

        runAsync(() -> IntStream.range(0, 1_000)
            .peek(__ -> sleep(5L))
            .forEach(legoals::setAge));

        assertThat(legoals)
            .is(
                eventually()
                    .having(
                        it -> it.hasFieldOrPropertyWithValue("age", 555)));
    }

    @Test
    void eventualCondition_shouldFail() {
        Elf legoals = new Elf("Legolas");

        runAsync(() -> IntStream.range(0, 1_000)
            .peek(__ -> sleep(5L))
            .forEach(legoals::setAge));

        assertThrows(ConditionTimeoutException.class, () ->
            assertThat(legoals)
                .is(
                    eventually()
                        .having(
                            it -> it.hasFieldOrPropertyWithValue("age", 9_999)))
        );
    }

    // spotless:on

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static class Elf {
        final String name;
        private int age = 0;

        Elf(String name) {
            this.name = name;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}
