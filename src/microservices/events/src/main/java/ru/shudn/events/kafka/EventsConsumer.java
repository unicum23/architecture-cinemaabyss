package ru.shudn.events.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class EventsConsumer {

    @KafkaListener(topics = "${app.topics.user}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenUser(ConsumerRecord<String, String> record, @Payload String payload) {
        log.info("Received User event: topic={} partition={} offset={} key={} value={}",
                record.topic(), record.partition(), record.offset(), record.key(), payload);
    }

    @KafkaListener(topics = "${app.topics.payment}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenPayment(ConsumerRecord<String, String> record, @Payload String payload) {
        log.info("Received Payment event: topic={} partition={} offset={} key={} value={}",
                record.topic(), record.partition(), record.offset(), record.key(), payload);
    }

    @KafkaListener(topics = "${app.topics.movie}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenMovie(ConsumerRecord<String, String> record, @Payload String payload) {
        log.info("Received Movie event: topic={} partition={} offset={} key={} value={}",
                record.topic(), record.partition(), record.offset(), record.key(), payload);
    }

}
