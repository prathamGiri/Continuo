package com.example.taskmanager.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** Creates the ticket-events topic on startup (3 partitions so several email workers can share the load). */
@Configuration
@ConditionalOnProperty(name = "app.notifications.enabled", havingValue = "true")
public class KafkaTopicConfig {

    @Bean
    public NewTopic ticketEventsTopic(@Value("${app.notifications.topic}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }
}
