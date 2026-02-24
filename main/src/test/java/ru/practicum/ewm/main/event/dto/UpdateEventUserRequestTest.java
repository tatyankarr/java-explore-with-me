package ru.practicum.ewm.main.event.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.ewm.main.event.model.UserStateAction;
import ru.practicum.ewm.main.location.dto.LocationDto;

import static org.junit.jupiter.api.Assertions.*;

class UpdateEventUserRequestTest {
    private UpdateEventUserRequest request;

    @BeforeEach
    void setUp() {
        request = UpdateEventUserRequest.builder()
                .annotation("Обновленная аннотация события")
                .category(2L)
                .description("Обновленное описание события")
                .eventDate("2025-01-01 20:00:00")
                .location(new LocationDto(55.75f, 37.62f))
                .paid(true)
                .participantLimit(200)
                .requestModeration(false)
                .stateAction(UserStateAction.SEND_TO_REVIEW)
                .title("Обновленный заголовок")
                .build();
    }

    @Test
    void shouldCreateUpdateEventUserRequestWithAllFields() {
        assertEquals("Обновленная аннотация события", request.getAnnotation());
        assertEquals(2L, request.getCategory());
        assertEquals("Обновленное описание события", request.getDescription());
        assertEquals("2025-01-01 20:00:00", request.getEventDate());
        assertEquals(55.75f, request.getLocation().getLat());
        assertEquals(37.62f, request.getLocation().getLon());
        assertTrue(request.getPaid());
        assertEquals(200, request.getParticipantLimit());
        assertFalse(request.getRequestModeration());
        assertEquals(UserStateAction.SEND_TO_REVIEW, request.getStateAction());
        assertEquals("Обновленный заголовок", request.getTitle());
    }

    @Test
    void shouldCreateUpdateEventUserRequestWithConstructor() {
        LocationDto location = new LocationDto(55.75f, 37.62f);
        UpdateEventUserRequest dto = new UpdateEventUserRequest(
                "Аннотация",
                1L,
                "Описание",
                "2025-01-01 20:00:00",
                location,
                true,
                100,
                false,
                UserStateAction.CANCEL_REVIEW,
                "Заголовок"
        );

        assertEquals("Аннотация", dto.getAnnotation());
        assertEquals(1L, dto.getCategory());
        assertEquals("Описание", dto.getDescription());
        assertEquals("2025-01-01 20:00:00", dto.getEventDate());
        assertEquals(location, dto.getLocation());
        assertTrue(dto.getPaid());
        assertEquals(100, dto.getParticipantLimit());
        assertFalse(dto.getRequestModeration());
        assertEquals(UserStateAction.CANCEL_REVIEW, dto.getStateAction());
        assertEquals("Заголовок", dto.getTitle());
    }
}
