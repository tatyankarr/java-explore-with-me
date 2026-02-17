package ru.practicum.ewm.stats.server;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final StatsRepository repository;

    public void save(EndpointHitDto hit) {
        EndpointHit entity = EndpointHitMapper.toEntity(hit);
        repository.save(entity);
    }

    public List<ViewStats> getStats(LocalDateTime start,
                                    LocalDateTime end,
                                    List<String> uris,
                                    boolean unique) {

        if (unique) {
            return repository.findStatsUnique(start, end, uris);
        }
        return repository.findStats(start, end, uris);
    }
}

