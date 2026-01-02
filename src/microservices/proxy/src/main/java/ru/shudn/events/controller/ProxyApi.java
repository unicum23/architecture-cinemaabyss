package ru.shudn.events.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;
import java.util.Set;


@Slf4j
@RestController
public class ProxyApi {

    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "host",
            "connection",
            "keep-alive",
            "proxy-authenticate",
            "proxy-authorization",
            "te",
            "trailer",
            "transfer-encoding",
            "upgrade",
            "content-length"
    );

    @Value("${GRADUAL_MIGRATION}")
    private boolean gradualMigration;
    @Value("${MOVIES_MIGRATION_PERCENT}")
    private int moviesMigrationPercent;
    @Value("${MONOLITH_URL}")
    private String monolithUrl;
    @Value("${MOVIES_SERVICE_URL}")
    private String moviesServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();


    @RequestMapping("/api/movies/**")
    public ResponseEntity<byte[]> proxyMoviesRequest(HttpServletRequest request) throws IOException {
        return redirectRequest(request, needRouteToMoviesService() ? moviesServiceUrl : monolithUrl);
    }

    @RequestMapping("/api/users/**")
    public ResponseEntity<byte[]> proxyUsersRequest(HttpServletRequest request) throws IOException {
        return redirectRequest(request, monolithUrl);
    }

    @RequestMapping("/api/payments/**")
    public ResponseEntity<byte[]> proxyPaymentsRequest(HttpServletRequest request) throws IOException {
        return redirectRequest(request, monolithUrl);
    }

    @RequestMapping("/api/subscriptions/**")
    public ResponseEntity<byte[]> proxySubscriptionsRequest(HttpServletRequest request) throws IOException {
        return redirectRequest(request, monolithUrl);
    }

    @GetMapping("/health")
    public String health() {
        return restTemplate.getForObject(monolithUrl + "/health", String.class);
    }

    private boolean needRouteToMoviesService() {
        return gradualMigration && random.nextInt(100) <= moviesMigrationPercent;
    }

    public ResponseEntity<byte[]> redirectRequest(
            HttpServletRequest request,
            String targetBaseUrl
    ) throws IOException {

        URI targetUri = UriComponentsBuilder
                .fromUriString(targetBaseUrl)
                .path(normalizePath(request.getRequestURI()))
                .query(request.getQueryString())
                .build(true)
                .toUri();

        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames()).forEach(headerName -> {
            if (!HOP_BY_HOP_HEADERS.contains(headerName.toLowerCase())) {
                headers.put(headerName, Collections.list(request.getHeaders(headerName)));
            }
        });

        byte[] body = request.getInputStream().available() > 0
                ? request.getInputStream().readAllBytes()
                : null;

        HttpEntity<byte[]> httpEntity = new HttpEntity<>(body, headers);

        long start = System.currentTimeMillis();

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    targetUri,
                    method,
                    httpEntity,
                    byte[].class
            );

            long duration = System.currentTimeMillis() - start;

            log.debug(
                    "Proxy {} {} -> {} [{}] {}ms",
                    method,
                    request.getRequestURI(),
                    targetUri,
                    response.getStatusCode(),
                    duration
            );

            return ResponseEntity
                    .status(response.getStatusCode())
                    .headers(filterResponseHeaders(response.getHeaders()))
                    .body(response.getBody());

        } catch (HttpStatusCodeException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .headers(filterResponseHeaders(Objects.requireNonNull(e.getResponseHeaders())))
                    .body(e.getResponseBodyAsByteArray());
        }
    }

    private String normalizePath(String path) {
        if (path.endsWith("/") && path.length() > 1) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    private HttpHeaders filterResponseHeaders(HttpHeaders original) {
        HttpHeaders filtered = new HttpHeaders();
        original.forEach((key, values) -> {
            if (!HOP_BY_HOP_HEADERS.contains(key.toLowerCase())) {
                filtered.put(key, values);
            }
        });
        return filtered;
    }

}
