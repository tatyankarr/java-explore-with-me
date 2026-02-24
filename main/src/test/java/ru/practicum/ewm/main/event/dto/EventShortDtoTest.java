package ru.practicum.ewm.main.event.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.ewm.main.category.dto.CategoryDto;
import ru.practicum.ewm.main.user.dto.UserShortDto;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventShortDtoTest {
    private Validator validator;
    private EventShortDto eventShortDto;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        eventShortDto = EventShortDto.builder()
                .id(1L)
                .annotation("Краткое описание события")
                .category(CategoryDto.builder().id(1L).name("Концерты").build())
                .confirmedRequests(5L)
                .eventDate("2024-12-31 18:00:00")
                .initiator(UserShortDto.builder().id(1L).name("Иван Иванов").build())
                .paid(true)
                .title("Заголовок события")
                .views(50L)
                .build();
    }

    @Test
    void shouldCreateValidEventShortDto() {
        Set<ConstraintViolation<EventShortDto>> violations = validator.validate(eventShortDto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldWorkGetters() {
        assertEquals(1L, eventShortDto.getId());
        assertEquals("Краткое описание события", eventShortDto.getAnnotation());
        assertEquals("Концерты", eventShortDto.getCategory().getName());
        assertEquals(5L, eventShortDto.getConfirmedRequests());
        assertEquals("2024-12-31 18:00:00", eventShortDto.getEventDate());
        assertEquals("Иван Иванов", eventShortDto.getInitiator().getName());
        assertTrue(eventShortDto.getPaid());
        assertEquals("Заголовок события", eventShortDto.getTitle());
        assertEquals(50L, eventShortDto.getViews());
    }
}
