package io.github.etr.tracting.http.test.dummy.task;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("todos")
public interface ExternalTodoClient {
    @GetExchange("/{id}")
    ResponseEntity<ExternalTodoDto> get(@PathVariable("id") Long id);
}