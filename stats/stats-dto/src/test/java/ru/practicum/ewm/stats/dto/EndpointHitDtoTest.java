package ru.practicum.ewm.stats.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

class EndpointHitDtoTest {

    private ObjectMapper objectMapper;
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        LocalDateTime timestamp = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        EndpointHitDto hit = EndpointHitDto.builder()
                .id(1L)
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.0.1")
                .timestamp(timestamp)
                .build();

        String json = objectMapper.writeValueAsString(hit);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"app\":\"ewm-main-service\"");
        assertThat(json).contains("\"uri\":\"/events/1\"");
        assertThat(json).contains("\"ip\":\"192.168.0.1\"");
        assertThat(json).contains("\"timestamp\":\"2024-01-01 12:00:00\"");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        String jsonContent =
                "{"
                        + "\"id\":1,"
                        + "\"app\":\"ewm-main-service\","
                        + "\"uri\":\"/events/1\","
                        + "\"ip\":\"192.168.0.1\","
                        + "\"timestamp\":\"2024-01-01 12:00:00\""
                        + "}";

        EndpointHitDto hit = objectMapper.readValue(jsonContent, EndpointHitDto.class);

        assertThat(hit.getId()).isEqualTo(1L);
        assertThat(hit.getApp()).isEqualTo("ewm-main-service");
        assertThat(hit.getUri()).isEqualTo("/events/1");
        assertThat(hit.getIp()).isEqualTo("192.168.0.1");
        assertThat(hit.getTimestamp()).isEqualTo(LocalDateTime.of(2024, 1, 1, 12, 0, 0));
    }

    @Test
    void shouldCreateDtoWithBuilder() {
        LocalDateTime timestamp = LocalDateTime.now();
        EndpointHitDto hit = EndpointHitDto.builder()
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
}