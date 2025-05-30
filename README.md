# Utilitest

Utilitest is a collection of test utilities that provides integration support for popular 
testing libraries like AssertJ, Mockito, and Awaitility.

Go ahead and add the latest version of the [library](https://github.com/etrandafir93/utilitest/packages) to your project:
```xml
<dependency>
  <groupId>io.github.etrandafir93</groupId>
  <artifactId>utilitest</artifactId>
  <version>${version}</version>
  <scope>test</scope>
</dependency>
```

## JUnit Lambdas

Utilitest provides a JUnit extension that enables us to use lambda expressions 
instead of the commonly used Before/After Each/All block methods.

This library provides annotations to simplify test lifecycle management in JUnit 
by allowing you to define setup and cleanup logic directly on fields. 
The annotations [@DoBeforeAll](junit-lambdas/src/main/java/io/github/etr/junit/lambdas/DoBeforeAll.java),
[@DoBeforeEach](junit-lambdas/src/main/java/io/github/etr/junit/lambdas/DoBeforeEach.java),
[@DoAfterAll](junit-lambdas/src/main/java/io/github/etr/junit/lambdas/DoAfterAll.java),
and [@DoAfterEach](junit-lambdas/src/main/java/io/github/etr/junit/lambdas/DoAfterEach.java),
enable concise and readable test code by invoking specified methods 
or functional interfaces before/after tests.

We can use these annotations at a field level in two ways:
1. **We can annotate fields that are functional interfaces**: 
```java
static File resource;

@DoBeforeAll
static ThrowingRunnable setUp = () -> 
    resource = Files.createTempFile("test", ".tmp").toFile();

@DoAfterAll
static ThrowingRunnable tearDown = () -> 
    resource.delete();
```
1. **We can annotate other fields and define the method to invoke**: 
```java
@DoBeforeEach(invoke = "init")
@DoAfterEach(invoke = "reset")
static Resource resource = ...;
```
### Example

Here is a simple test which doesn't use any of the annotations:

```java
import java.io.IOException;

class JunitLambdasReadmeTests {

    static File resource = null;

    @BeforeAll
    static void setUp() throws IOException {
        resource = Files.createTempFile("test", ".txt").toFile();
    }

    @BeforeEach
    void clean() throws IOException {
        Files.writeString(resource.toPath(), "", TRUNCATE_EXISTING, WRITE);
    }

    @AfterAll
    static void tearDown() throws IOException {
        Files.delete(resource.toPath());
    }

    // some tests...
}
```

With Junit-lambdas, we can simplify the code to:

```java 
@ExtendWith(JunitLambdasExtension.class)
class JunitLambdasReadmeTests {

    @DoAfterAll(invoke = "delete")
    static File resource = null;

    @DoBeforeAll
    static ThrowingRunnable setUp = () -> 
        resource = Files.createTempFile("test", ".txt").toFile();

    @DoBeforeEach
    ThrowingRunnable clean = () -> 
        Files.writeString(resource.toPath(), "", TRUNCATE_EXISTING, WRITE);

    // some tests...
}
```    

## AssertJ + Awaitility

This module offers a simple way to combine AssertJ assertions with Awaitility.

We'll take advantage of the _Condition<>_ class, one of AssertJ's extension points.
We can use `eventually().having(...)` to create an eventual condition:

```java
assertThat(legolas)
    .is(
      eventually()
        .having(
          it -> it.hasFieldOrPropertyWithValue("age", 100)));
```

Or we can use a more concise syntax:

```java
assertThat(legolas)
    .is(
      eventually(
          it -> it.hasFieldOrPropertyWithValue("age", "Legolas")));
```

For the polling interval and timeout, we'll rely on Awaitility's default settings. 
They can be set via these static methods:
```java
static {
    Awaitility.setDefaultPollInterval(Duration.ofMillis(1));
    Awaitility.setDefaultTimeout(Duration.ofSeconds(5));
}
```

## AssertJ + Mockito's _AssertMatcher_ 

To use _verify()_ mocks, we generally have two options. We can either capture the arguments 
using a _Captor_, which adds some boilerplate code and overhead, 
or we can use Mockito's _ArgumentMatchers_ to verify the arguments directly with a lambda:

```java
@Test
void customAssertMatcher() {
  FooService mock = Mockito.mock();
  mock.process(new Account(1L, "John Doe", "johnDoe@gmail.com"));

  Mockito.verify(mock).process(
    Mockito.argThat(
      it -> it.getAccountId().equals(1L)
        && it.getName().equals("John Doe")
        && it.getEmail().equals("johndoe@gmail.com")));
}
```

However, the failure messages from these custom argument matchers are often cryptic, 
making it difficult to pinpoint the cause of the test failure:

```plaintext
Argument(s) are different! Wanted:
fooService.process(
    <custom argument matcher>
);
-> at io.github.etr.utilitest.mockito.ReadmeExampelsTest$FooService.process(ReadmeExampelsTest.java:26)
Actual invocations have different arguments:
fooService.process(
    io.github.etr.utilitest.mockito.ReadmeExampelsTest$Account@245a26e1
);
-> at io.github.etr.utilitest.mockito.ReadmeExampelsTest.customAssertMatcher(ReadmeExampelsTest.java:62)
```

As a workaround, we can use a fluent AssertJ assertion within the custom _ArgumentMatcher_ 
and always return true:

```java
@Test
void assertMatcherWithAssertJ() {
    FooService mock = Mockito.mock();
    mock.process(new Account(1L, "John Doe", "johnDoe@gmail.com"));

    Mockito.verify(mock).process(
      Mockito.argThat(it -> {
        Assertions.assertThat(it)
          .hasFieldOrPropertyWithValue("accountId", 1L)
          .hasFieldOrPropertyWithValue("name", "John Doe")
          .hasFieldOrPropertyWithValue("email", "johndoe@gmail.com");
        return true;
      }));
}
```
While this solution provides much clearer error messages, it comes with 
the downside of adding a lot of boilerplate code. If the code is repetitive, 
it can make the tests harder to read and maintain.

So, let's remove all this ceremony and use utilitest's `MockitoAndAssertJ::argThat` instead:

```java
Mockito.verify(mock).process(
  MockitoAndAssertJ.argThat(it -> it
    .hasFieldOrPropertyWithValue("accountId", 1L)
    .hasFieldOrPropertyWithValue("name", "John Doe")
    .hasFieldOrPropertyWithValue("email", "johndoe@gmail.com")));
```
This approach brings together the best of both worlds: the convenience of verifying 
the argument using a lambda expression and the fluent API of AssertJ, 
providing clear and descriptive error messages:

```plaintext
java.lang.AssertionError: 
Expecting
  io.github.etr.utilitest.mockito.ReadmeExampelsTest$Account@f5c79a6
to have a property or a field named "email" with value
  "johndoe@gmail.com"
but value was:
  "johnDoe@gmail.com"
```

### Other Assertions

The `MockitoAndAssertJ::argThat` from the previous example enables us 
to consume an ObjectAssert from AsserJ, that provides some basic assertions. 
The assertJ API allows us to change this type to a more specialized instance of assertion, 
to verify specific properties. For example, we can change the assertion type to a MapAssert 
to be able to check specific key-value entries:

```java
@Test
void asInstanceOf() {
  Map<Long, String> data = Map.of(
    1L, "John",
    2L, "Bobby"
  );

  FooService mock = Mockito.mock();
  mock.processMap(data);

  verify(mock).processMap(
    MockitoAndAssertJ.argThat(it -> it
      .asInstanceOf(InstanceOfAssertFactories.MAP)
      .containsEntry(1L, "John")
      .containsEntry(2L, "Bobby")));
}
```
With MockitoAndAssertJ, we can also specify InstanceOfAssertFactories upfront. 
To achieve this, we split the argThat into two separate methods: 
`MockitoAndAssertJ.arg(InstanceOfAssertFactories.MAP).that(it -> ...)`. 

Let's use this API to verify a method that accepts a _LocalDateTime_ and a _List_ of Strings:

```java
@Test
void arg_that() {
  FooService mock = Mockito.mock();
  mock.processDateAndList(now(), List.of("A", "B", "C"));

  verify(mock).processDateAndList(
     arg(TEMPORAL).that(time -> time.isCloseTo(now(), within(500, MILLIS))),
     arg(LIST).that(list -> list.containsExactly("A", "B", "C"))
  );
}
```


