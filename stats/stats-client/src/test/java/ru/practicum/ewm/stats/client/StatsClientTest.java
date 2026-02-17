package ru.practicum.ewm.stats.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.ewm.stats.dto.EndpointHitDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StatsClientTest {

    private RestTemplate restTemplate;
    private StatsClient client;

    private final String serverUrl = "http://localhost:9090";

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        client = new StatsClient(serverUrl);

        try {
            var field = StatsClient.class.getDeclaredField("rest");
            field.setAccessible(true);
            field.set(client, restTemplate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldSaveHitSuccessfully() {
        EndpointHitDto hit = EndpointHitDto.builder()
                .app("test-app")
                .uri("/test")
                .ip("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();

        when(restTemplate.postForEntity(
                eq(serverUrl + "/hit"),
                eq(hit),
                eq(Object.class)
        )).thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());

        ResponseEntity<Object> response = client.saveHit(hit);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(restTemplate).postForEntity(serverUrl + "/hit", hit, Object.class);
    }

    @Test
    void shouldHandleErrorWhenSavingHit() {
        EndpointHitDto hit = EndpointHitDto.builder()
                .app("test-app")
                .uri("/test")
                .ip("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();

        when(restTemplate.postForEntity(
                anyString(),
                any(),
                any()
        )).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        ResponseEntity<Object> response = client.saveHit(hit);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldGetStatsSuccessfully() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 0, 0);
        List<String> uris = List.of("/uri1", "/uri2");
        boolean unique = false;

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("[]");

        when(restTemplate.getForEntity(
                anyString(),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        ResponseEntity<Object> response = client.getStats(start, end, uris, unique);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("[]");

        verify(restTemplate, times(1)).getForEntity(anyString(), eq(Object.class));
    }

    @Test
    void shouldGetStatsWithoutUris() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 0, 0);
        boolean unique = true;

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().body("[]");

        when(restTemplate.getForEntity(
                anyString(),
                eq(Object.class)
        )).thenReturn(expectedResponse);

        ResponseEntity<Object> response = client.getStats(start, end, null, unique);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("[]");

        verify(restTemplate, times(1)).getForEntity(anyString(), eq(Object.class));
    }

    @Test
    void shouldHandleErrorWhenGettingStats() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 2, 0, 0);

        when(restTemplate.getForEntity(
                anyString(),
                any()
        )).thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        ResponseEntity<Object> response = client.getStats(start, end, null, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}