package ru.practicum.ewm.main.stats;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

public interface ExternalStatsService {

    void logHit(HttpServletRequest request);

    Map<String, Long> getViews(List<String> uris);
}
