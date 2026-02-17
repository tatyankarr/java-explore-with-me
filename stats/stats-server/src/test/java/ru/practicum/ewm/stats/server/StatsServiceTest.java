package ru.practicum.ewm.stats.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private StatsRepository repository;

    @InjectMocks
    private StatsService service;

    private final LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
    private final LocalDateTime end = LocalDateTime.of(2024, 1, 2, 0, 0);

    @Test
    void shouldSaveHit() {
        LocalDateTime timestamp = LocalDateTime.now();
        EndpointHitDto dto = EndpointHitDto.builder()
                .app("test-app")
                .uri("/test")
                .ip("127.0.0.1")
                .timestamp(timestamp)
                .build();

        service.save(dto);

        ArgumentCaptor<EndpointHit> captor = ArgumentCaptor.forClass(EndpointHit.class);
        verify(repository).save(captor.capture());

        EndpointHit saved = captor.getValue();
        assertThat(saved.getApp()).isEqualTo("test-app");
        assertThat(saved.getUri()).isEqualTo("/test");
        assertThat(saved.getIp()).isEqualTo("127.0.0.1");
        assertThat(saved.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void shouldGetStatsNonUnique() {
        List<ViewStats> expectedStats = List.of(
                new ViewStats("app1", "/uri1", 3L),
                new ViewStats("app2", "/uri2", 1L)
        );

        when(repository.findStats(start, end, List.of("/uri1", "/uri2")))
                .thenReturn(expectedStats);

        List<ViewStats> result = service.getStats(
                start, end, List.of("/uri1", "/uri2"), false);

        assertThat(result).isEqualTo(expectedStats);
        verify(repository).findStats(start, end, List.of("/uri1", "/uri2"));
    }

    @Test
    void shouldGetStatsUnique() {
        List<ViewStats> expectedStats = List.of(
                new ViewStats("app1", "/uri1", 2L),
                new ViewStats("app2", "/uri2", 1L)
        );

        when(repository.findStatsUnique(start, end, List.of("/uri1", "/uri2")))
                .thenReturn(expectedStats);

        List<ViewStats> result = service.getStats(
                start, end, List.of("/uri1", "/uri2"), true);

        assertThat(result).isEqualTo(expectedStats);
        verify(repository).findStatsUnique(start, end, List.of("/uri1", "/uri2"));
    }

    @Test
    void shouldGetStatsWithNullUris() {
        List<ViewStats> expectedStats = List.of(
                new ViewStats("app1", "/uri1", 3L),
                new ViewStats("app2", "/uri2", 1L)
        );

        when(repository.findStats(start, end, null)).thenReturn(expectedStats);

        List<ViewStats> result = service.getStats(start, end, null, false);

        assertThat(result).isEqualTo(expectedStats);
        verify(repository).findStats(start, end, null);
    }
}