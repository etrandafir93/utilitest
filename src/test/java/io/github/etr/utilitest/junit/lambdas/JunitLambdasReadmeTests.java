package io.github.etr.utilitest.junit.lambdas;

import static java.nio.file.Files.writeString;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JunitLambdasExtension.class)
class JunitLambdasReadmeTests {

    @DoAfterAll(invoke = "delete")
    static File resource = null;

    @DoBeforeAll
    static ThrowingRunnable setUp =
            () -> resource = Files.createTempFile("test", ".txt").toFile();

    @DoBeforeEach
    ThrowingRunnable clean = () -> Files.writeString(resource.toPath(), "", TRUNCATE_EXISTING, WRITE);

    //    @BeforeAll
    //    static void setUp() {
    //        try {
    //            resource = Files.createTempFile("test", ".txt").toFile();
    //        } catch (IOException e) {
    //            throw new RuntimeException(e);
    //        }
    //    }
    //
    //    @BeforeEach
    //    void clean() {
    //        try {
    //            Files.writeString(resource.toPath(), "", TRUNCATE_EXISTING, WRITE);
    //        } catch (IOException e) {
    //            throw new RuntimeException(e);
    //        }
    //    }
    //
    //    @AfterAll
    //    static void tearDown() {
    //        try {
    //            Files.delete(resource.toPath());
    //        } catch (IOException e) {
    //            throw new RuntimeException(e);
    //        }
    //    }

    @Test
    void shouldPassIfFileCleared1() throws IOException {
        String initialContent = Files.readString(resource.toPath());
        assertThat(initialContent).isEmpty(); // Passes only if the file is cleared

        String content = "Test Content 1";
        writeString(resource.toPath(), content);
    }

    @Test
    void shouldPassIfFileCleared2() throws IOException {
        String initialContent = Files.readString(resource.toPath());
        assertThat(initialContent).isEmpty(); // Passes only if the file is cleared

        String content = "Test Content 2";
        writeString(resource.toPath(), content);
    }

    @Test
    void shouldPassIfFileCleared3() throws IOException {
        String initialContent = Files.readString(resource.toPath());
        assertThat(initialContent).isEmpty(); // Passes only if the file is cleared

        String content = "Test Content 3";
        writeString(resource.toPath(), content);
    }
}
