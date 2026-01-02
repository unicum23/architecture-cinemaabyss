package ru.shudn.events.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class EventsProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.topics.user}")
    private String userTopic;
    @Value("${app.topics.payment}")
    private String paymentTopic;
    @Value("${app.topics.movie}")
    private String movieTopic;


    public <T> void sendUserEvent(String key, T payload) {
        send(userTopic, key, payload);
    }

    public <T> void sendPaymentEvent(String key, T payload) {
        send(paymentTopic, key, payload);
    }

    public <T> void sendMovieEvent(String key, T payload) {
        send(movieTopic, key, payload);
    }

    private <T> void send(String topic, String key, T payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, key, json)
                    .whenComplete((res, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send to topic={} key={} payload={}", topic, key, json, ex);
                        } else {
                            log.info("Produced event to topic={} partition={} offset={} key={} payload={}",
                                    topic,
                                    res.getRecordMetadata().partition(),
                                    res.getRecordMetadata().offset(),
                                    key,
                                    json);
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("Serialization error for topic={} key={}", topic, key, e);
        }
    }

}