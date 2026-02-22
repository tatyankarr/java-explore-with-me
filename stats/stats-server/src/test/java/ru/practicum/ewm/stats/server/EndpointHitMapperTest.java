package ru.practicum.ewm.stats.server;

import org.junit.jupiter.api.Test;
import ru.practicum.ewm.stats.dto.EndpointHitDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EndpointHitMapperTest {

    @Test
    void shouldMapDtoToEntity() {
        LocalDateTime timestamp = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        EndpointHitDto dto = EndpointHitDto.builder()
                .id(1L)
                .app("test-app")
                .uri("/test")
                .ip("127.0.0.1")
                .timestamp(timestamp)
                .build();

        EndpointHit entity = EndpointHitMapper.toEntity(dto);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getApp()).isEqualTo("test-app");
        assertThat(entity.getUri()).isEqualTo("/test");
        assertThat(entity.getIp()).isEqualTo("127.0.0.1");
        assertThat(entity.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void shouldMapNullIdCorrectly() {
        EndpointHitDto dto = EndpointHitDto.builder()
                .app("test-app")
                .uri("/test")
                .ip("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();

        EndpointHit entity = EndpointHitMapper.toEntity(dto);

        assertThat(entity.getId()).isNull();
    }
}