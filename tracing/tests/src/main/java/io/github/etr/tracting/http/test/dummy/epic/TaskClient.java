package io.github.etr.tracting.http.test.dummy.epic;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import io.github.etr.tracting.http.test.dummy.task.ExternalTodoDto;

@HttpExchange("/api/tasks")
public interface TaskClient {

    @GetExchange("/{id}/title")
    String taskTitle(@PathVariable("id") Long id);

}