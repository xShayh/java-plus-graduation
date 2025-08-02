package stat;

import client.BaseClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.EndpointHitDto;

import java.net.URI;
import java.util.*;

import java.util.List;

@Service
@Slf4j
public class StatClient extends BaseClient {
    private final DiscoveryClient discoveryClient;
    private final RetryTemplate retryTemplate;
    private final String statsServiceId;

    public StatClient(DiscoveryClient discoveryClient,
                      @Value("${discovery.services.stats-server-id}") String statsServiceId,
                      RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(""))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build());

        this.discoveryClient = discoveryClient;
        this.statsServiceId = statsServiceId;

        this.retryTemplate = new RetryTemplate();
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(3000L);
        this.retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        MaxAttemptsRetryPolicy retryPolicy = new MaxAttemptsRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        this.retryTemplate.setRetryPolicy(retryPolicy);
    }

    public ResponseEntity<Object> saveHit(EndpointHitDto hitDto) {
        return post("/hit", hitDto);
    }

    public ResponseEntity<Object> getStats(String start, String end, List<String> uris, Boolean unique) {
        log.info("Start building request for getStats()");
        log.info("Input parameters - Start: {}, End: {}, URIs: {}, Unique: {}", start, end, uris, unique);
        if (start == null || start.trim().isEmpty()) {
            log.info("Start date is null or empty!");
            throw new IllegalArgumentException("Start date cannot be null or empty");
        }
        if (end == null || end.trim().isEmpty()) {
            log.info("End date is null or empty!");
            throw new IllegalArgumentException("End date cannot be null or empty");
        }
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("unique", unique);
        if (uris != null && !uris.isEmpty()) {
            uriBuilder.queryParam("uris", String.join(",", uris));
            log.info("Uris added to the path: {}", uris);
        }
        log.info("Final request URI: {}", uriBuilder.toUriString());
        try {
            ResponseEntity<Object> response = get(uriBuilder.toUriString(), new HashMap<>());
            log.info("Response received: {}", response);
            return response;
        } catch (Exception e) {
            log.info("Error occurred while making the request: {}", e.getMessage(), e);
            throw new RuntimeException("Error during stats request", e);
        }
    }

    private URI makeUri(String path) {
        ServiceInstance instance = retryTemplate.execute(cxt -> getInstance(statsServiceId));
        log.info("Host() = {} Port() = {}", instance.getHost(), instance.getPort());
        return URI.create("http://" + instance.getHost() + ":" + instance.getPort() + path);
    }

    private ServiceInstance getInstance(String serviceId) {
        try {
            return discoveryClient
                    .getInstances(serviceId)
                    .getFirst();
        } catch (Exception exception) {
            throw new RuntimeException(
                    "Error occurred while detecting stats service address: " + serviceId,
                    exception
            );
        }
    }
}