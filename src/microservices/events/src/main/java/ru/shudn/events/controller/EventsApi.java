package ru.shudn.events.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.shudn.events.dto.MovieDto;
import ru.shudn.events.dto.PaymentDto;
import ru.shudn.events.dto.UserDto;
import ru.shudn.events.kafka.EventsProducer;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;


@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventsApi {

    private final EventsProducer producer;


    @PostMapping("/user")
    public ResponseEntity<Map<String, Object>> createUserEvent(@RequestBody UserDto model) {

        String key = Objects.requireNonNullElse(model.userId(), UUID.randomUUID().toString());
        producer.sendUserEvent(key, model);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        Map.of("status", "success",
                                "key", key)
                );
    }

    @PostMapping("/payment")
    public ResponseEntity<Map<String, Object>> createPaymentEvent(@RequestBody PaymentDto model) {
        String key = Objects.requireNonNullElse(model.paymentId(), UUID.randomUUID().toString());
        producer.sendPaymentEvent(key, model);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        Map.of("status", "success",
                                "key", key)
                );
    }

    @PostMapping("/movie")
    public ResponseEntity<Map<String, Object>> createMovieEvent(@RequestBody MovieDto model) {
        String key = Objects.requireNonNullElse(model.movieId(), UUID.randomUUID().toString());
        producer.sendMovieEvent(key, model);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        Map.of("status", "success",
                                "key", key)
                );
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("status", true));
    }

}
