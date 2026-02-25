package ru.practicum.ewm.main.stats;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.practicum.ewm.stats.client.StatsClient;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalStatsServiceImplTest {

    @Mock
    private StatsClient statsClient;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ExternalStatsServiceImpl externalStatsService;

    @Test
    void logHit_ShouldSendHitToStatsClient() {
        when(request.getRequestURI()).thenReturn("/events/1");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        externalStatsService.logHit(request);

        verify(statsClient, times(1)).saveHit(any(EndpointHitDto.class));
    }

    @Test
    void logHit_ShouldCreateCorrectHit() {
        when(request.getRequestURI()).thenReturn("/events/1");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        externalStatsService.logHit(request);

        verify(statsClient, times(1)).saveHit(argThat(hit ->
                hit.getApp().equals("ewm-main-service") &&
                        hit.getUri().equals("/events/1") &&
                        hit.getIp().equals("127.0.0.1") &&
                        hit.getTimestamp() != null
        ));
    }

    @Test
    void logHit_ShouldHandleDifferentUris() {
        when(request.getRequestURI()).thenReturn("/events/2");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        externalStatsService.logHit(request);

        verify(statsClient, times(1)).saveHit(argThat(hit ->
                hit.getUri().equals("/events/2") &&
                        hit.getIp().equals("192.168.1.1")
        ));
    }

    @Test
    void getViews_ShouldReturnViewsMap_WhenStatsExist() {
        List<ViewStats> viewStatsList = List.of(
                new ViewStats("ewm-main-service", "/events/1", 10L),
                new ViewStats("ewm-main-service", "/events/2", 5L)
        );
        ResponseEntity<List<ViewStats>> response = ResponseEntity.ok(viewStatsList);

        when(statsClient.getStats(any(LocalDateTime.class), any(LocalDateTime.class),
                eq(List.of("/events/1", "/events/2")), eq(true)))
                .thenReturn(response);

        Map<String, Long> result = externalStatsService.getViews(List.of("/events/1", "/events/2"));

        assertEquals(2, result.size());
        assertEquals(10L, result.get("/events/1"));
        assertEquals(5L, result.get("/events/2"));
    }

    @Test
    void getViews_ShouldReturnEmptyMap_WhenResponseBodyIsNull() {
        ResponseEntity<List<ViewStats>> response = ResponseEntity.ok(null);

        when(statsClient.getStats(any(LocalDateTime.class), any(LocalDateTime.class),
                anyList(), eq(true)))
                .thenReturn(response);

        Map<String, Long> result = externalStatsService.getViews(List.of("/events/1"));

        assertTrue(result.isEmpty());
    }

    @Test
    void getViews_ShouldReturnEmptyMap_WhenStatsListIsEmpty() {
        List<ViewStats> viewStatsList = List.of();
        ResponseEntity<List<ViewStats>> response = ResponseEntity.ok(viewStatsList);

        when(statsClient.getStats(any(LocalDateTime.class), any(LocalDateTime.class),
                anyList(), eq(true)))
                .thenReturn(response);

        Map<String, Long> result = externalStatsService.getViews(List.of("/events/1"));

        assertTrue(result.isEmpty());
    }

    @Test
    void getViews_ShouldHandleNullUris() {
        List<ViewStats> viewStatsList = List.of();
        ResponseEntity<List<ViewStats>> response = ResponseEntity.ok(viewStatsList);

        when(statsClient.getStats(any(LocalDateTime.class), any(LocalDateTime.class),
                isNull(), eq(true)))
                .thenReturn(response);

        Map<String, Long> result = externalStatsService.getViews(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void getViews_ShouldUseCorrectTimeRange() {
        List<ViewStats> viewStatsList = List.of();
        ResponseEntity<List<ViewStats>> response = ResponseEntity.ok(viewStatsList);

        when(statsClient.getStats(any(LocalDateTime.class), any(LocalDateTime.class),
                anyList(), eq(true)))
                .thenReturn(response);

        externalStatsService.getViews(List.of("/events/1"));

        verify(statsClient, times(1)).getStats(
                eq(LocalDateTime.of(2000, 1, 1, 0, 0)),
                any(LocalDateTime.class),
                eq(List.of("/events/1")),
                eq(true)
        );
    }

    @Test
    void getViews_ShouldHandleMultipleUris() {
        List<ViewStats> viewStatsList = List.of(
                new ViewStats("ewm-main-service", "/events/1", 10L),
                new ViewStats("ewm-main-service", "/events/2", 5L),
                new ViewStats("ewm-main-service", "/events/3", 7L)
        );
        ResponseEntity<List<ViewStats>> response = ResponseEntity.ok(viewStatsList);

        when(statsClient.getStats(any(LocalDateTime.class), any(LocalDateTime.class),
                eq(List.of("/events/1", "/events/2", "/events/3")), eq(true)))
                .thenReturn(response);

        Map<String, Long> result = externalStatsService.getViews(List.of("/events/1", "/events/2", "/events/3"));

        assertEquals(3, result.size());
        assertEquals(10L, result.get("/events/1"));
        assertEquals(5L, result.get("/events/2"));
        assertEquals(7L, result.get("/events/3"));
    }

    @Test
    void getViews_ShouldHandleError_AndReturnEmptyMap() {
        when(statsClient.getStats(any(LocalDateTime.class), any(LocalDateTime.class),
                anyList(), eq(true)))
                .thenThrow(new RuntimeException("Stats service error"));

        Map<String, Long> result = externalStatsService.getViews(List.of("/events/1"));

        assertTrue(result.isEmpty());
        verify(statsClient, times(1)).getStats(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyList(),
                eq(true)
        );
    }
}