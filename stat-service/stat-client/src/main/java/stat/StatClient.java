package stat;

import client.BaseClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

@Service
public class StatClient extends BaseClient {
    @Autowired
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

    public ResponseEntity<Object> getStats(ViewStatsDto statsRequestDto) {
        return get("/stat", statsRequestDto);
    }
}