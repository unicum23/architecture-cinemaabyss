package ru.shudn.events.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


@Slf4j
@RestController
public class ProxyApi {

    @Value("${GRADUAL_MIGRATION}")
    private boolean gradualMigration;
    @Value("${MOVIES_MIGRATION_PERCENT}")
    private int moviesMigrationPercent;
    @Value("${MONOLITH_URL}")
    private String monolithUrl;
    @Value("${MOVIES_SERVICE_URL}")
    private String moviesServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();


    @GetMapping("/api/movies")
    public String getMovies() {
        if (gradualMigration) {
            if (Math.random() * 100 < moviesMigrationPercent) {
                return restTemplate.getForObject(moviesServiceUrl + "/api/movies", String.class);
            } else {
                return restTemplate.getForObject(monolithUrl + "/api/movies", String.class);
            }
        } else {
            return restTemplate.getForObject(monolithUrl + "/api/movies", String.class);
        }
    }

    @GetMapping("/api/users")
    public String getUsers() {
        return restTemplate.getForObject(monolithUrl + "/api/users", String.class);
    }

    @GetMapping("/health")
    public String health() {
        return restTemplate.getForObject(monolithUrl + "/health", String.class);
    }

}
