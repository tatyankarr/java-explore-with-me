package ru.practicum.ewm.main.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.main.category.dto.CategoryDto;
import ru.practicum.ewm.main.event.dto.*;
import ru.practicum.ewm.main.event.service.EventService;
import ru.practicum.ewm.main.location.dto.LocationDto;
import ru.practicum.ewm.main.stats.ExternalStatsService;
import ru.practicum.ewm.main.user.dto.UserShortDto;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @MockBean
    private ExternalStatsService statsService;

    private NewEventDto newEventDto;
    private EventFullDto eventFullDto;
    private EventShortDto eventShortDto;
    private UpdateEventAdminRequest updateAdminRequest;
    private UpdateEventUserRequest updateUserRequest;

    @BeforeEach
    void setUp() {
        CategoryDto categoryDto = CategoryDto.builder()
                .id(1L)
                .name("Концерты")
                .build();

        UserShortDto userShortDto = UserShortDto.builder()
                .id(1L)
                .name("Иван Иванов")
                .build();

        LocationDto locationDto = LocationDto.builder()
                .lat(55.75f)
                .lon(37.62f)
                .build();

        newEventDto = new NewEventDto();
        newEventDto.setAnnotation("Аннотация события для тестирования");
        newEventDto.setCategory(1L);
        newEventDto.setDescription("Полное описание события для тестирования");
        newEventDto.setEventDate("2025-12-31 18:00:00");
        newEventDto.setLocation(locationDto);
        newEventDto.setPaid(true);
        newEventDto.setParticipantLimit(100);
        newEventDto.setRequestModeration(true);
        newEventDto.setTitle("Заголовок события");

        eventFullDto = EventFullDto.builder()
                .id(1L)
                .annotation("Аннотация события")
                .category(categoryDto)
                .confirmedRequests(0L)
                .createdOn("2024-01-01 12:00:00")
                .description("Описание события")
                .eventDate("2025-12-31 18:00:00")
                .initiator(userShortDto)
                .location(locationDto)
                .paid(true)
                .participantLimit(100)
                .requestModeration(true)
                .state("PENDING")
                .title("Заголовок события")
                .views(0L)
                .build();

        eventShortDto = EventShortDto.builder()
                .id(1L)
                .annotation("Аннотация события")
                .category(categoryDto)
                .confirmedRequests(0L)
                .eventDate("2025-12-31 18:00:00")
                .initiator(userShortDto)
                .paid(true)
                .title("Заголовок события")
                .views(0L)
                .build();

        updateAdminRequest = UpdateEventAdminRequest.builder()
                .annotation("Обновленная аннотация")
                .title("Обновленный заголовок")
                .build();

        updateUserRequest = UpdateEventUserRequest.builder()
                .annotation("Обновленная аннотация")
                .title("Обновленный заголовок")
                .build();
    }

    @Test
    void getEventsAdmin_ShouldReturnEvents() throws Exception {
        List<EventFullDto> events = List.of(eventFullDto);
        when(eventService.getEventsAdmin(any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(events);

        mockMvc.perform(get("/admin/events")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Заголовок события"));
    }

    @Test
    void getEventsAdmin_ShouldReturnEmptyList_WhenNoEvents() throws Exception {
        when(eventService.getEventsAdmin(any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/admin/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getEventsAdmin_ShouldAcceptAllParameters() throws Exception {
        when(eventService.getEventsAdmin(any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/admin/events")
                        .param("users", "1", "2")
                        .param("states", "PENDING", "PUBLISHED")
                        .param("categories", "1", "2")
                        .param("rangeStart", "2024-01-01 00:00:00")
                        .param("rangeEnd", "2024-12-31 23:59:59")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(eventService, times(1)).getEventsAdmin(
                eq(List.of(1L, 2L)),
                eq(List.of("PENDING", "PUBLISHED")),
                eq(List.of(1L, 2L)),
                eq("2024-01-01 00:00:00"),
                eq("2024-12-31 23:59:59"),
                eq(0),
                eq(10));
    }

    @Test
    void updateEventAdmin_ShouldReturnUpdatedEvent() throws Exception {
        when(eventService.updateEventAdmin(eq(1L), any(UpdateEventAdminRequest.class)))
                .thenReturn(eventFullDto);

        mockMvc.perform(patch("/admin/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateAdminRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Заголовок события"));
    }

    @Test
    void updateEventAdmin_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        UpdateEventAdminRequest invalidRequest = UpdateEventAdminRequest.builder()
                .annotation("a".repeat(2001))
                .build();

        mockMvc.perform(patch("/admin/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).updateEventAdmin(anyLong(), any());
    }

    @Test
    void addEvent_ShouldReturnCreatedEvent() throws Exception {
        when(eventService.addEvent(eq(1L), any(NewEventDto.class))).thenReturn(eventFullDto);

        mockMvc.perform(post("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Заголовок события"));
    }

    @Test
    void addEvent_ShouldReturnBadRequest_WhenAnnotationIsBlank() throws Exception {
        NewEventDto invalidDto = new NewEventDto();
        invalidDto.setAnnotation("");
        invalidDto.setCategory(1L);
        invalidDto.setDescription("Описание");
        invalidDto.setEventDate("2025-12-31 18:00:00");
        invalidDto.setLocation(new LocationDto(55.75f, 37.62f));
        invalidDto.setTitle("Заголовок");

        mockMvc.perform(post("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).addEvent(anyLong(), any());
    }

    @Test
    void addEvent_ShouldReturnBadRequest_WhenAnnotationIsTooShort() throws Exception {
        NewEventDto invalidDto = new NewEventDto();
        invalidDto.setAnnotation("Короткая");
        invalidDto.setCategory(1L);
        invalidDto.setDescription("Описание");
        invalidDto.setEventDate("2025-12-31 18:00:00");
        invalidDto.setLocation(new LocationDto(55.75f, 37.62f));
        invalidDto.setTitle("Заголовок");

        mockMvc.perform(post("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).addEvent(anyLong(), any());
    }

    @Test
    void addEvent_ShouldReturnBadRequest_WhenDescriptionIsBlank() throws Exception {
        NewEventDto invalidDto = new NewEventDto();
        invalidDto.setAnnotation("Аннотация события для тестирования");
        invalidDto.setCategory(1L);
        invalidDto.setDescription("");
        invalidDto.setEventDate("2025-12-31 18:00:00");
        invalidDto.setLocation(new LocationDto(55.75f, 37.62f));
        invalidDto.setTitle("Заголовок");

        mockMvc.perform(post("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).addEvent(anyLong(), any());
    }

    @Test
    void addEvent_ShouldReturnBadRequest_WhenEventDateIsBlank() throws Exception {
        NewEventDto invalidDto = new NewEventDto();
        invalidDto.setAnnotation("Аннотация события для тестирования");
        invalidDto.setCategory(1L);
        invalidDto.setDescription("Короткое описание");
        invalidDto.setEventDate("2025-12-31 18:00:00");
        invalidDto.setLocation(new LocationDto(55.75f, 37.62f));
        invalidDto.setTitle("Заголовок");

        mockMvc.perform(post("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).addEvent(anyLong(), any());
    }

    @Test
    void addEvent_ShouldReturnBadRequest_WhenLocationIsNull() throws Exception {
        NewEventDto invalidDto = new NewEventDto();
        invalidDto.setAnnotation("Аннотация события для тестирования");
        invalidDto.setCategory(1L);
        invalidDto.setDescription("Описание события");
        invalidDto.setEventDate("");
        invalidDto.setLocation(new LocationDto(55.75f, 37.62f));
        invalidDto.setTitle("Заголовок");

        mockMvc.perform(post("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).addEvent(anyLong(), any());
    }

    @Test
    void addEvent_ShouldReturnBadRequest_WhenTitleIsBlank() throws Exception {
        NewEventDto invalidDto = new NewEventDto();
        invalidDto.setAnnotation("Аннотация события для тестирования");
        invalidDto.setCategory(1L);
        invalidDto.setDescription("Описание события");
        invalidDto.setEventDate("2025-12-31 18:00:00");
        invalidDto.setLocation(null);
        invalidDto.setTitle("Заголовок");

        mockMvc.perform(post("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).addEvent(anyLong(), any());
    }

    @Test
    void addEvent_ShouldReturnBadRequest_WhenTitleIsTooShort() throws Exception {
        NewEventDto invalidDto = new NewEventDto();
        invalidDto.setAnnotation("Аннотация события для тестирования");
        invalidDto.setCategory(1L);
        invalidDto.setDescription("Описание события");
        invalidDto.setEventDate("2025-12-31 18:00:00");
        invalidDto.setLocation(null);
        invalidDto.setTitle("Заголовок");

        mockMvc.perform(post("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).addEvent(anyLong(), any());
    }

    @Test
    void getEventsByUser_ShouldReturnEvents() throws Exception {
        List<EventShortDto> events = List.of(eventShortDto);
        when(eventService.getEventsByUser(eq(1L), anyInt(), anyInt())).thenReturn(events);

        mockMvc.perform(get("/users/1/events")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Заголовок события"));
    }

    @Test
    void getEventsByUser_ShouldReturnEmptyList_WhenNoEvents() throws Exception {
        when(eventService.getEventsByUser(eq(1L), anyInt(), anyInt())).thenReturn(List.of());

        mockMvc.perform(get("/users/1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getEventByIdByUser_ShouldReturnEvent() throws Exception {
        when(eventService.getEventByIdByUser(1L, 1L)).thenReturn(eventFullDto);

        mockMvc.perform(get("/users/1/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Заголовок события"));
    }

    @Test
    void updateEventByUser_ShouldReturnUpdatedEvent() throws Exception {
        when(eventService.updateEventByUser(eq(1L), eq(1L), any(UpdateEventUserRequest.class)))
                .thenReturn(eventFullDto);

        mockMvc.perform(patch("/users/1/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Заголовок события"));
    }

    @Test
    void updateEventByUser_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        UpdateEventUserRequest invalidRequest = UpdateEventUserRequest.builder()
                .annotation("a".repeat(2001))
                .build();

        mockMvc.perform(patch("/users/1/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).updateEventByUser(anyLong(), anyLong(), any());
    }

    @Test
    void getEventsPublic_ShouldReturnEmptyList_WhenNoEvents() throws Exception {
        when(eventService.getEventsPublic(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getEventsPublic_ShouldAcceptAllParameters() throws Exception {
        when(eventService.getEventsPublic(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/events")
                        .param("text", "концерт")
                        .param("categories", "1", "2")
                        .param("paid", "true")
                        .param("rangeStart", "2024-01-01 00:00:00")
                        .param("rangeEnd", "2024-12-31 23:59:59")
                        .param("onlyAvailable", "true")
                        .param("sort", "EVENT_DATE")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(eventService, times(1)).getEventsPublic(
                eq("концерт"),
                eq(List.of(1L, 2L)),
                eq(true),
                eq("2024-01-01 00:00:00"),
                eq("2024-12-31 23:59:59"),
                eq(true),
                eq("EVENT_DATE"),
                eq(0),
                eq(10));
    }

    @Test
    void getEventPublic_ShouldReturnEvent() throws Exception {

        when(eventService.getEventPublic(eq(1L), any(HttpServletRequest.class)))
                .thenReturn(eventFullDto);

        mockMvc.perform(get("/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Заголовок события"));

        verify(eventService, times(1))
                .getEventPublic(eq(1L), any(HttpServletRequest.class));
    }
}
