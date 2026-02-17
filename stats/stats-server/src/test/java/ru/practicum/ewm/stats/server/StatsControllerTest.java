package ru.practicum.ewm.stats.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatsController.class)
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StatsService service;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void shouldSaveHit() throws Exception {
        LocalDateTime timestamp = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        EndpointHitDto hit = EndpointHitDto.builder()
                .app("test-app")
                .uri("/test")
                .ip("127.0.0.1")
                .timestamp(timestamp)
                .build();

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hit)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnBadRequestWhenHitInvalid() throws Exception {
        EndpointHitDto invalidHit = EndpointHitDto.builder()
                .app("")
                .uri("/test")
                .ip("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidHit)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetStats() throws Exception {
        String start = "2024-01-01 00:00:00";
        String end = "2024-01-02 00:00:00";

        List<ViewStats> expectedStats = List.of(
                new ViewStats("app1", "/uri1", 3L),
                new ViewStats("app2", "/uri2", 1L)
        );

        when(service.getStats(
                LocalDateTime.parse(start, FORMATTER),
                LocalDateTime.parse(end, FORMATTER),
                List.of("/uri1", "/uri2"),
                false
        )).thenReturn(expectedStats);

        mockMvc.perform(get("/stats")
                        .param("start", start)
                        .param("end", end)
                        .param("uris", "/uri1", "/uri2")
                        .param("unique", "false"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].app").value("app1"))
                .andExpect(jsonPath("$[0].uri").value("/uri1"))
                .andExpect(jsonPath("$[0].hits").value(3))
                .andExpect(jsonPath("$[1].app").value("app2"))
                .andExpect(jsonPath("$[1].uri").value("/uri2"))
                .andExpect(jsonPath("$[1].hits").value(1));
    }

    @Test
    void shouldGetStatsWithUniqueTrue() throws Exception {
        String start = "2024-01-01 00:00:00";
        String end = "2024-01-02 00:00:00";

        List<ViewStats> expectedStats = List.of(
                new ViewStats("app1", "/uri1", 2L)
        );

        when(service.getStats(
                LocalDateTime.parse(start, FORMATTER),
                LocalDateTime.parse(end, FORMATTER),
                null,
                true
        )).thenReturn(expectedStats);

        mockMvc.perform(get("/stats")
                        .param("start", start)
                        .param("end", end)
                        .param("unique", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app").value("app1"))
                .andExpect(jsonPath("$[0].uri").value("/uri1"))
                .andExpect(jsonPath("$[0].hits").value(2));
    }

    @Test
    void shouldReturnBadRequestWhenStartAfterEnd() throws Exception {
        String start = "2024-01-02 00:00:00";
        String end = "2024-01-01 00:00:00";

        mockMvc.perform(get("/stats")
                        .param("start", start)
                        .param("end", end))
                .andExpect(status().isBadRequest());
    }
}