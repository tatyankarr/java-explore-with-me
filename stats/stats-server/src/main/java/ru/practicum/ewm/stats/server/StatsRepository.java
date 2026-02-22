package ru.practicum.ewm.stats.server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    @Query("""
        SELECT new ru.practicum.ewm.stats.dto.ViewStats(
            e.app,
            e.uri,
            COUNT(e)
        )
        FROM EndpointHit e
        WHERE e.timestamp BETWEEN :start AND :end
        AND (:uris IS NULL OR e.uri IN :uris)
        GROUP BY e.app, e.uri
        ORDER BY COUNT(e) DESC
    """)
    List<ViewStats> findStats(LocalDateTime start,
                              LocalDateTime end,
                              List<String> uris);

    @Query("""
        SELECT new ru.practicum.ewm.stats.dto.ViewStats(
            e.app,
            e.uri,
            COUNT(DISTINCT e.ip)
        )
        FROM EndpointHit e
        WHERE e.timestamp BETWEEN :start AND :end
        AND (:uris IS NULL OR e.uri IN :uris)
        GROUP BY e.app, e.uri
        ORDER BY COUNT(DISTINCT e.ip) DESC
    """)
    List<ViewStats> findStatsUnique(LocalDateTime start,
                                    LocalDateTime end,
                                    List<String> uris);
}

