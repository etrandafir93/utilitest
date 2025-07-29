package io.github.etr.tracting.http.test.dummy.epic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CreateEpicConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(CreateEpicConsumer.class);
    
    @KafkaListener(
            topics = "create.epic.command",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    void createEpic(CreateEpicCommand command) {
        LOG.info("Received create epic command: {}", command);
        
        // Process the command
        LOG.info("Creating epic with title: '{}' and {} todo items", 
                command.title(), command.todoIds().size());
        
        // Here you would implement the actual epic creation logic
        // For example, calling a service to store the epic data
    }
}
