package ru.practicum.ewm.stats.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StatsClient {

    private final RestTemplate rest;
    private final String serverUrl;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(String serverUrl) {
        this.serverUrl = serverUrl;
        this.rest = new RestTemplate();
    }

    public ResponseEntity<Object> saveHit(EndpointHitDto hit) {
        try {
            return rest.postForEntity(serverUrl + "/hit", hit, Object.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getResponseBodyAsByteArray());
        }
    }

    public ResponseEntity<List<ViewStats>> getStats(LocalDateTime start,
                                                    LocalDateTime end,
                                                    List<String> uris,
                                                    Boolean unique) {

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(serverUrl + "/stats")
                .queryParam("start", start.format(FORMATTER))
                .queryParam("end", end.format(FORMATTER))
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            builder.queryParam("uris", uris);
        }

        try {
            String url = builder.toUriString();

            return rest.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    new org.springframework.core.ParameterizedTypeReference<List<ViewStats>>() {}
            );

        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }
}
