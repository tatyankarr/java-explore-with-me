package ru.practicum.ewm.main.stats;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.client.StatsClient;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExternalStatsServiceImpl implements ExternalStatsService {

    private final StatsClient statsClient;
    private static final String APP = "ewm-main-service";

    @Override
    public void logHit(HttpServletRequest request) {

        EndpointHitDto hit = EndpointHitDto.builder()
                .app(APP)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();

        statsClient.saveHit(hit);
    }

    @Override
    public Map<String, Long> getViews(List<String> uris) {
        try {
            ResponseEntity<List<ViewStats>> response = statsClient.getStats(
                    LocalDateTime.of(2000, 1, 1, 0, 0),
                    LocalDateTime.now(),
                    uris,
                    true
            );

            List<ViewStats> stats = response.getBody();

            if (stats == null || stats.isEmpty()) {
                return Collections.emptyMap();
            }

            return stats.stream()
                    .collect(Collectors.toMap(
                            ViewStats::getUri,
                            ViewStats::getHits
                    ));

        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}