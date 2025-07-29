package io.github.etr.tracting.http.test.dummy.epic;

import java.util.List;

public record EpicCreatedEvent(
    String title,
    List<Task> todos) {

    record Task(
        Long id,
        String title) {}
}
