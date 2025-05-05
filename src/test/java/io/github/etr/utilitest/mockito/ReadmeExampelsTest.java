package io.github.etr.utilitest.mockito;

import static io.github.etr.utilitest.mockito.MockitoAndAssertJ.arg;
import static io.github.etr.utilitest.mockito.MockitoAndAssertJ.argThat;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.assertj.core.api.InstanceOfAssertFactories.TEMPORAL;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ReadmeExamplesTest {

    static class FooService {

        public void process(Account data) {
            // some logic ...
        }

        public void processMap(Map<Long, String> data) {
            // some logic ...
        }

        public void processDateAndList(LocalDateTime date, List<String> list) {
            // some logic ...
        }
    }

    static class Account {
        Long accountId;
        String name;
        String email;

        public Account(Long accountId, String name, String email) {
            this.accountId = accountId;
            this.name = name;
            this.email = email;
        }

        public Long getAccountId() {
            return accountId;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }
    }

    @Test
    void customAssertMatcher() {
        FooService mock = Mockito.mock();
        mock.process(new Account(1L, "John Doe", "johnDoe@gmail.com"));

        verify(mock)
                .process(Mockito.argThat(it -> it.getAccountId().equals(1L)
                        && it.getName().equals("John Doe")
                        && it.getEmail().equals("johnDoe@gmail.com")));
    }

    @Test
    void assertMatcherWithAssertJ() {
        FooService mock = Mockito.mock();
        mock.process(new Account(1L, "John Doe", "johnDoe@gmail.com"));

        verify(mock).process(Mockito.argThat(it -> {
            Assertions.assertThat(it)
                    .hasFieldOrPropertyWithValue("accountId", 1L)
                    .hasFieldOrPropertyWithValue("name", "John Doe")
                    .hasFieldOrPropertyWithValue("email", "johnDoe@gmail.com");
            return true;
        }));
    }

    @Test
    void assertMatcherWithutilitest() {
        FooService mock = Mockito.mock();
        mock.process(new Account(1L, "John Doe", "johnDoe@gmail.com"));

        verify(mock).process(argThat(it -> it.hasFieldOrPropertyWithValue("accountId", 1L)
                .hasFieldOrPropertyWithValue("name", "John Doe")
                .hasFieldOrPropertyWithValue("email", "johnDoe@gmail.com")));
    }

    @Test
    void asInstanceOf() {
        Map<Long, String> data = Map.of(
                1L, "John",
                2L, "Bobby");

        FooService mock = Mockito.mock();
        mock.processMap(data);

        verify(mock).processMap(MockitoAndAssertJ.argThat(it -> it.asInstanceOf(InstanceOfAssertFactories.MAP)
                .containsEntry(1L, "John")
                .containsEntry(2L, "Bobby")));
    }

    @Test
    void arg_that() {
        FooService mock = Mockito.mock();
        mock.processDateAndList(now(), List.of("A", "B", "C"));

        verify(mock)
                .processDateAndList(
                        arg(TEMPORAL).that(it -> it.isCloseTo(now(), within(1_000, MILLIS))),
                        arg(LIST).that(it -> it.containsExactly("A", "B", "C")));
    }
}
