package io.github.etr.tracting.http.test.dummy;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.etr.tracting.http.test.dummy.todo.TodoDto;
import io.github.etr.tracting.http.test.dummy.todo.TodoClient;

@RestController
@RequestMapping("api/dummy")
@SpringBootApplication
public class DummyApp {
    private static final Logger LOG = LoggerFactory.getLogger(DummyApp.class);

    public static void main(String[] args) {
        SpringApplication.run(DummyApp.class, args);
    }

    private final TodoClient toDoClient;

    @GetMapping
    String getDummy(@RequestParam(name = "todo") Long id, @RequestHeader Map<String, Object> headers) {
        LOG.info("Received GET request for todo #{}", id);
        LOG.info("Headers: {}", headers);
        LOG.info("Sending Request to TODO service");
        TodoDto response = toDoClient.get(id)
            .getBody();
        LOG.info("Received Response from TODO service: {}", response);
        return response.title();
    }

    public DummyApp(TodoClient toDoClient) {
        this.toDoClient = toDoClient;
    }
}
