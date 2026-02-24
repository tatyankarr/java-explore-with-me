package ru.practicum.ewm.main.event.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.ewm.main.category.dto.CategoryDto;
import ru.practicum.ewm.main.location.dto.LocationDto;
import ru.practicum.ewm.main.user.dto.UserShortDto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EventFullDtoTest {
    private Validator validator;
    private EventFullDto eventFullDto;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        eventFullDto = EventFullDto.builder()
                .id(1L)
                .annotation("Тестовое описание события для проверки")
                .category(CategoryDto.builder().id(1L).name("Концерты").build())
                .confirmedRequests(10L)
                .createdOn("2024-01-01 12:00:00")
                .description("Полное описание события для тестирования")
                .eventDate("2024-12-31 18:00:00")
                .initiator(UserShortDto.builder().id(1L).name("Иван Иванов").build())
                .location(LocationDto.builder().lat(55.75f).lon(37.62f).build())
                .paid(false)
                .participantLimit(100)
                .publishedOn("2024-01-01 13:00:00")
                .requestModeration(true)
                .state("PUBLISHED")
                .title("Заголовок события")
                .views(150L)
                .build();
    }

    @Test
    void shouldCreateValidEventFullDto() {
        Set<ConstraintViolation<EventFullDto>> violations = validator.validate(eventFullDto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldWorkBuilderAndGetters() {
        assertEquals(1L, eventFullDto.getId());
        assertEquals("Тестовое описание события для проверки", eventFullDto.getAnnotation());
        assertEquals("Концерты", eventFullDto.getCategory().getName());
        assertEquals(10L, eventFullDto.getConfirmedRequests());
        assertEquals("2024-01-01 12:00:00", eventFullDto.getCreatedOn());
        assertEquals("Полное описание события для тестирования", eventFullDto.getDescription());
        assertEquals("2024-12-31 18:00:00", eventFullDto.getEventDate());
        assertEquals("Иван Иванов", eventFullDto.getInitiator().getName());
        assertEquals(55.75f, eventFullDto.getLocation().getLat());
        assertFalse(eventFullDto.getPaid());
        assertEquals(100, eventFullDto.getParticipantLimit());
        assertEquals("2024-01-01 13:00:00", eventFullDto.getPublishedOn());
        assertTrue(eventFullDto.getRequestModeration());
        assertEquals("PUBLISHED", eventFullDto.getState());
        assertEquals("Заголовок события", eventFullDto.getTitle());
        assertEquals(150L, eventFullDto.getViews());
    }

    @Test
    void shouldCreateEventFullDtoWithNullPublishedOn() {
        EventFullDto dto = EventFullDto.builder()
                .id(1L)
                .annotation("Тестовое описание")
                .category(new CategoryDto())
                .confirmedRequests(0L)
                .createdOn("2024-01-01 12:00:00")
                .description("Описание")
                .eventDate("2024-12-31 18:00:00")
                .initiator(new UserShortDto())
                .location(new LocationDto())
                .paid(false)
                .participantLimit(0)
                .publishedOn(null)
                .requestModeration(true)
                .state("PENDING")
                .title("Заголовок")
                .views(0L)
                .build();

        Set<ConstraintViolation<EventFullDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
        assertNull(dto.getPublishedOn());
    }

    @Test
    void shouldWorkEqualsAndHashCode() {
        EventFullDto dto1 = EventFullDto.builder()
                .id(1L)
                .title("Событие 1")
                .build();

        EventFullDto dto2 = EventFullDto.builder()
                .id(1L)
                .title("Событие 1")
                .build();

        EventFullDto dto3 = EventFullDto.builder()
                .id(2L)
                .title("Событие 2")
                .build();

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void shouldWorkToString() {
        String toString = eventFullDto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("EventFullDto"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("title=Заголовок события"));
    }
}
