package io.github.etr.tracting.http.test.dummy.epic;

import java.util.List;

public record EpicCreated(
    String id,
    String title,
    List<Todo> todos) {

    record Todo(
        String id,
        String title) {}
}
