package ru.practicum.ewm.stats.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.ewm.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class StatsRepositoryTest {

    @Autowired
    private StatsRepository repository;

    private final LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
    private final LocalDateTime end = LocalDateTime.of(2024, 1, 2, 0, 0);

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        EndpointHit hit1 = EndpointHit.builder()
                .app("app1")
                .uri("/uri1")
                .ip("192.168.0.1")
                .timestamp(start.plusHours(1))
                .build();

        EndpointHit hit2 = EndpointHit.builder()
                .app("app1")
                .uri("/uri1")
                .ip("192.168.0.1")
                .timestamp(start.plusHours(2))
                .build();

        EndpointHit hit3 = EndpointHit.builder()
                .app("app1")
                .uri("/uri1")
                .ip("192.168.0.2")
                .timestamp(start.plusHours(3))
                .build();

        EndpointHit hit4 = EndpointHit.builder()
                .app("app2")
                .uri("/uri2")
                .ip("192.168.0.3")
                .timestamp(start.plusHours(4))
                .build();

        EndpointHit hit5 = EndpointHit.builder()
                .app("app1")
                .uri("/uri1")
                .ip("192.168.0.4")
                .timestamp(end.plusHours(1))
                .build();

        repository.saveAll(List.of(hit1, hit2, hit3, hit4, hit5));
    }

    @Test
    void shouldFindStatsWithoutUris() {
        List<ViewStats> stats = repository.findStats(start, end, null);

        assertThat(stats).hasSize(2);

        ViewStats stats1 = stats.get(0);
        assertThat(stats1.getApp()).isEqualTo("app1");
        assertThat(stats1.getUri()).isEqualTo("/uri1");
        assertThat(stats1.getHits()).isEqualTo(3);

        ViewStats stats2 = stats.get(1);
        assertThat(stats2.getApp()).isEqualTo("app2");
        assertThat(stats2.getUri()).isEqualTo("/uri2");
        assertThat(stats2.getHits()).isEqualTo(1);
    }

    @Test
    void shouldFindStatsWithUris() {
        List<ViewStats> stats = repository.findStats(start, end, List.of("/uri1"));

        assertThat(stats).hasSize(1);

        ViewStats stats1 = stats.get(0);
        assertThat(stats1.getApp()).isEqualTo("app1");
        assertThat(stats1.getUri()).isEqualTo("/uri1");
        assertThat(stats1.getHits()).isEqualTo(3);
    }

    @Test
    void shouldFindStatsUnique() {
        List<ViewStats> stats = repository.findStatsUnique(start, end, null);

        assertThat(stats).hasSize(2);

        ViewStats stats1 = stats.get(0);
        assertThat(stats1.getApp()).isEqualTo("app1");
        assertThat(stats1.getUri()).isEqualTo("/uri1");
        assertThat(stats1.getHits()).isEqualTo(2);

        ViewStats stats2 = stats.get(1);
        assertThat(stats2.getApp()).isEqualTo("app2");
        assertThat(stats2.getUri()).isEqualTo("/uri2");
        assertThat(stats2.getHits()).isEqualTo(1);
    }

    @Test
    void shouldFindStatsUniqueWithUris() {
        List<ViewStats> stats = repository.findStatsUnique(start, end, List.of("/uri1"));

        assertThat(stats).hasSize(1);

        ViewStats stats1 = stats.get(0);
        assertThat(stats1.getApp()).isEqualTo("app1");
        assertThat(stats1.getUri()).isEqualTo("/uri1");
        assertThat(stats1.getHits()).isEqualTo(2);
    }

    @Test
    void shouldReturnEmptyListWhenNoDataInRange() {
        LocalDateTime futureStart = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime futureEnd = LocalDateTime.of(2025, 1, 2, 0, 0);

        List<ViewStats> stats = repository.findStats(futureStart, futureEnd, null);

        assertThat(stats).isEmpty();
    }
}