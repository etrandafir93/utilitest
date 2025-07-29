package io.github.etr.tracting.http.test.dummy.task;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private static final Logger LOG = LoggerFactory.getLogger(TaskController.class);
    
    private final ExternalTodoClient todoClient;

    public TaskController(ExternalTodoClient todoClient) {
        this.todoClient = todoClient;
    }

    @GetMapping("/{id}/title")
    String taskTitle(@PathVariable Long id, @RequestHeader Map<String, Object> headers) {
        LOG.info("Received GET request for todo #{}", id);
        LOG.info("Headers: {}", headers);
        LOG.info("Sending Request to TODO service");
        ExternalTodoDto response = todoClient.get(id)
            .getBody();
        LOG.info("Received Response from TODO service: {}", response);
        return response.title();
    }
}
