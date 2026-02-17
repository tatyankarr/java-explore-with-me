package ru.practicum.ewm.stats.server;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EndpointHitTest {

    @Test
    void shouldCreateEndpointHitUsingBuilder() {
        LocalDateTime timestamp = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        EndpointHit hit = EndpointHit.builder()
                .id(1L)
                .app("test-app")
                .uri("/test")
                .ip("127.0.0.1")
                .timestamp(timestamp)
                .build();

        assertThat(hit.getId()).isEqualTo(1L);
        assertThat(hit.getApp()).isEqualTo("test-app");
        assertThat(hit.getUri()).isEqualTo("/test");
        assertThat(hit.getIp()).isEqualTo("127.0.0.1");
        assertThat(hit.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void shouldSetAndGetFields() {
        EndpointHit hit = new EndpointHit();

        LocalDateTime timestamp = LocalDateTime.now();
        hit.setId(1L);
        hit.setApp("app");
        hit.setUri("/uri");
        hit.setIp("192.168.0.1");
        hit.setTimestamp(timestamp);

        assertThat(hit.getId()).isEqualTo(1L);
        assertThat(hit.getApp()).isEqualTo("app");
        assertThat(hit.getUri()).isEqualTo("/uri");
        assertThat(hit.getIp()).isEqualTo("192.168.0.1");
        assertThat(hit.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void shouldUseAllArgsConstructor() {
        LocalDateTime timestamp = LocalDateTime.now();
        EndpointHit hit = new EndpointHit(1L, "app", "/uri", "ip", timestamp);

        assertThat(hit.getId()).isEqualTo(1L);
        assertThat(hit.getApp()).isEqualTo("app");
        assertThat(hit.getUri()).isEqualTo("/uri");
        assertThat(hit.getIp()).isEqualTo("ip");
        assertThat(hit.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void shouldUseNoArgsConstructor() {
        EndpointHit hit = new EndpointHit();
        assertThat(hit).isNotNull();
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        LocalDateTime timestamp = LocalDateTime.now();
        EndpointHit hit1 = EndpointHit.builder()
                .id(1L)
                .app("app")
                .uri("/uri")
                .ip("ip")
                .timestamp(timestamp)
                .build();

        EndpointHit hit2 = EndpointHit.builder()
                .id(1L)
                .app("app")
                .uri("/uri")
                .ip("ip")
                .timestamp(timestamp)
                .build();

        assertThat(hit1).isEqualTo(hit2);
        assertThat(hit1.hashCode()).hasSameHashCodeAs(hit2.hashCode());
    }
}