package io.github.etr.tracting.test.dummy.todo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("todos")
public interface TodoClient {
    @GetExchange("/{id}")
    ResponseEntity<TodoDto> get(@PathVariable("id") Long id);
}