package ru.shudn.events.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;


@Configuration
public class KafkaConfig {

    @Value("${app.topics.user}")
    private String userTopic;

    @Value("${app.topics.payment}")
    private String paymentTopic;

    @Value("${app.topics.movie}")
    private String movieTopic;

    @Bean
    public NewTopic userTopic() {
        return TopicBuilder.name(userTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentTopic() {
        return TopicBuilder.name(paymentTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic movieTopic() {
        return TopicBuilder.name(movieTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

}
