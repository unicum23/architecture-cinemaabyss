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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Random;


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
    private final Random random = new Random();


    @RequestMapping("/api/movies/**")
    public ResponseEntity<byte[]> proxyMoviesRequest(HttpServletRequest request) throws IOException {
        String targetUrl = needRouteToMoviesService() ? moviesServiceUrl : monolithUrl;
        return redirectRequest(request, targetUrl);
    }

    @RequestMapping("/api/users/**")
    public ResponseEntity<byte[]> proxyUsersRequest(HttpServletRequest request) throws IOException {
        String targetUrl = monolithUrl;
        return redirectRequest(request, targetUrl);
    }

    private boolean needRouteToMoviesService() {
        return gradualMigration && random.nextInt(100) <= moviesMigrationPercent;
    }

    private ResponseEntity<byte[]> redirectRequest(HttpServletRequest request, String targetBaseUrl) throws IOException {
        String params = request.getParameter("id");
        String path = normalizePath(request.getRequestURI());
        String targetUrl = targetBaseUrl + path;
        if (params != null && !params.isEmpty()) {
            targetUrl += "?id=" + params;
        }

        log.debug("Request targetUrl: {}", targetUrl);

        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        HttpHeaders headers = new HttpHeaders();
        request.getHeaderNames().asIterator()
                .forEachRemaining(headerName -> headers.add(headerName, request.getHeader(headerName)));

        byte[] body = request.getInputStream().readAllBytes();
        HttpEntity<byte[]> httpEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    targetUrl,
                    method,
                    httpEntity,
                    byte[].class);

            HttpHeaders responseHeaders = new HttpHeaders();
            response.getHeaders().forEach(responseHeaders::addAll);

            return new ResponseEntity<>(
                    response.getBody(),
                    responseHeaders,
                    response.getStatusCode());

        } catch (HttpClientErrorException e) {
            log.warn(e.getResponseBodyAsString());
            return ResponseEntity
                    .status(e.getStatusCode())
                    .headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsByteArray());
        } catch (Exception e) {
            log.warn(e.getMessage());
            return ResponseEntity
                    .internalServerError()
                    .body(("{\"error\":\"Internal Server Error\"}").getBytes());
        }
    }

    private String normalizePath(String path) {
        if (path.endsWith("/") && path.length() > 1) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    @GetMapping("/health")
    public String health() {
        return restTemplate.getForObject(monolithUrl + "/health", String.class);
    }

}
