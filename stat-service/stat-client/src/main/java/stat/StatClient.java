package stat;

import client.BaseClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.EndpointHitDto;

import java.util.*;

import java.util.List;

@Service
@Slf4j
public class StatClient extends BaseClient {
    public StatClient(@Value("${server.url}") String statServerUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(statServerUrl))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
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
}