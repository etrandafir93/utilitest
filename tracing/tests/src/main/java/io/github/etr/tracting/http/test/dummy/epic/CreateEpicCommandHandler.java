package io.github.etr.tracting.http.test.dummy.epic;

import static io.github.etr.tracting.http.test.dummy.LogColors.cyan;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class CreateEpicCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CreateEpicCommandHandler.class);

    private final TaskClient taskClient;
    private final KafkaTemplate<String, EpicCreatedEvent> kafkaTemplate;

    @KafkaListener(topics = "create.epic.command", containerFactory = "kafkaListenerContainerFactory")
    void createEpic(CreateEpicCommand command) {
        LOG.info(cyan("Received create epic command: {}"), command);

        EpicCreatedEvent epicCreated = command.todoIds()
            .stream()
            .map(taskId -> new EpicCreatedEvent.Task(taskId, taskClient.taskTitle(taskId)))
            .collect(collectingAndThen(toList(), taskList -> new EpicCreatedEvent(command.title(), taskList)));

        // other logic ...

        LOG.info(cyan("Created epic: {}. Publishing to 'epic.created.event'"), epicCreated);
        kafkaTemplate.send("epic.created.event", epicCreated);
    }

    public CreateEpicCommandHandler(TaskClient taskClient, KafkaTemplate<String, EpicCreatedEvent> kafkaTemplate) {
        this.taskClient = taskClient;
        this.kafkaTemplate = kafkaTemplate;
    }
}
