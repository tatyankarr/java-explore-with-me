package ru.practicum.ewm.main.event.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.practicum.ewm.main.location.dto.LocationDto;
import ru.practicum.ewm.main.util.Constants;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NewEventDtoTest {
    private Validator validator;
    private NewEventDto validDto;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        validDto = NewEventDto.builder()
                .annotation("А".repeat(20))
                .category(1L)
                .description("О".repeat(20))
                .eventDate(LocalDateTime.now().plusDays(2).format(Constants.FORMATTER))
                .location(LocationDto.builder().lat(55.75f).lon(37.62f).build())
                .paid(true)
                .participantLimit(100)
                .requestModeration(true)
                .title("Заголовок")
                .build();
    }

    @Nested
    class ValidationTests {

        @Test
        void shouldCreateValidNewEventDtoWithMinValues() {
            NewEventDto dto = NewEventDto.builder()
                    .annotation("А".repeat(20))
                    .category(1L)
                    .description("О".repeat(20))
                    .eventDate(LocalDateTime.now().plusDays(2).format(Constants.FORMATTER))
                    .location(LocationDto.builder().lat(55.75f).lon(37.62f).build())
                    .title("Заг")
                    .build();

            Set<ConstraintViolation<NewEventDto>> violations = validator.validate(dto);
            assertTrue(violations.isEmpty(), "Должен быть валидным с минимальными значениями");
        }

        @Test
        void shouldCreateValidNewEventDtoWithMaxValues() {
            NewEventDto dto = NewEventDto.builder()
                    .annotation("А".repeat(2000))
                    .category(1L)
                    .description("О".repeat(7000))
                    .eventDate(LocalDateTime.now().plusDays(2).format(Constants.FORMATTER))
                    .location(LocationDto.builder().lat(55.75f).lon(37.62f).build())
                    .paid(true)
                    .participantLimit(1000)
                    .requestModeration(true)
                    .title("З".repeat(120))
                    .build();

            Set<ConstraintViolation<NewEventDto>> violations = validator.validate(dto);
            assertTrue(violations.isEmpty(), "Должен быть валидным с максимальными значениями");
        }

        @Test
        void shouldCheckThatDefaultValuesAreNotSetInClass() {
            NewEventDto dto = NewEventDto.builder()
                    .annotation("А".repeat(20))
                    .category(1L)
                    .description("О".repeat(20))
                    .eventDate(LocalDateTime.now().plusDays(2).format(Constants.FORMATTER))
                    .location(LocationDto.builder().lat(55.75f).lon(37.62f).build())
                    .title("Заголовок")
                    .build();

            assertNull(dto.getPaid(), "paid должно быть null, если не указано явно");
            assertNull(dto.getParticipantLimit(), "participantLimit должно быть null, если не указано явно");
            assertNull(dto.getRequestModeration(), "requestModeration должно быть null, если не указано явно");
        }

        @Test
        void shouldSetExplicitlySpecifiedValues() {
            NewEventDto dto = NewEventDto.builder()
                    .annotation("А".repeat(20))
                    .category(1L)
                    .description("О".repeat(20))
                    .eventDate(LocalDateTime.now().plusDays(2).format(Constants.FORMATTER))
                    .location(LocationDto.builder().lat(55.75f).lon(37.62f).build())
                    .paid(false)
                    .participantLimit(50)
                    .requestModeration(false)
                    .title("Заголовок")
                    .build();

            assertFalse(dto.getPaid(), "paid должно быть false");
            assertEquals(50, dto.getParticipantLimit(), "participantLimit должно быть 50");
            assertFalse(dto.getRequestModeration(), "requestModeration должно быть false");
        }

        @Test
        void shouldFailValidationWhenAnnotationIsNull() {
            validDto.setAnnotation(null);
            Set<ConstraintViolation<NewEventDto>> violations = validator.validate(validDto);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("annotation")));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "Короткое"})
        @DisplayName("Должен провалить валидацию при некорректной длине annotation")
        void shouldFailValidationWhenAnnotationHasInvalidLength(String annotation) {
            validDto.setAnnotation(annotation);
            Set<ConstraintViolation<NewEventDto>> violations = validator.validate(validDto);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("annotation")));
        }

        @Test
        @DisplayName("Должен провалить валидацию при слишком длинной annotation")
        void shouldFailValidationWhenAnnotationIsTooLong() {
            validDto.setAnnotation("А".repeat(2001));
            Set<ConstraintViolation<NewEventDto>> violations = validator.validate(validDto);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("annotation")));
        }

        @Test
        @DisplayName("Должен провалить валидацию при null category")
        void shouldFailValidationWhenCategoryIsNull() {
            validDto.setCategory(null);
            Set<ConstraintViolation<NewEventDto>> violations = validator.validate(validDto);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("category")));
        }

        @Test
        @DisplayName("Должен провалить валидацию при null description")
        void shouldFailValidationWhenDescriptionIsNull() {
            validDto.setDescription(null);
            Set<ConstraintViolation<NewEventDto>> violations = validator.validate(validDto);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("description")));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "Короткое описание"})
        @DisplayName("Должен провалить валидацию при некорректной длине description")
        void shouldFailValidationWhenDescriptionHasInvalidLength(String description) {
            validDto.setDescription(description);
            Set<ConstraintViolation<NewEventDto>> violations = validator.validate(validDto);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("description")));
        }

        @Test
        @DisplayName("Должен провалить валидацию при слишком длинном description")
        void shouldFailValidationWhenDescriptionIsTooLong() {
            validDto.setDescription("О".repeat(7001));
            Set<ConstraintViolation<NewEventDto>> violations = validator.validate(validDto);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("description")));
        }

        @Test
        @DisplayName("Должен провалить валидацию при null eventDate")
        void shouldFailValidationWhenEventDateIsNull() {
            validDto.setEventDate(null);
            Set<ConstraintViolation<NewEventDto>> violations = validator.validate(validDto);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("eventDate")));
        }

        @Test
        @DisplayName("Должен провалить валидацию при пустом eventDate")
        void shouldFailValidationWhenEventDateIsEmpty() {
            validDto.setEventDate("");
            Set<ConstraintViolation<NewEventDto>> violations = validator.validate(validDto);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("eventDate")));
        }

        @Test
        @DisplayName("Должен провалить валидацию при null location")
        void shouldFailValidationWhenLocationIsNull() {
            validDto.setLocation(null);
            Set<ConstraintViolation<NewEventDto>> violations = validator.validate(validDto);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("location")));
        }

        @Test
        @DisplayName("Должен провалить валидацию при null title")
        void shouldFailValidationWhenTitleIsNull() {
            validDto.setTitle(null);
            Set<ConstraintViolation<NewEventDto>> violations = validator.validate(validDto);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "За"})
        @DisplayName("Должен провалить валидацию при некорректной длине title")
        void shouldFailValidationWhenTitleHasInvalidLength(String title) {
            validDto.setTitle(title);
            Set<ConstraintViolation<NewEventDto>> violations = validator.validate(validDto);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
        }

        @Test
        void shouldFailValidationWhenTitleIsTooLong() {
            validDto.setTitle("З".repeat(121));
            Set<ConstraintViolation<NewEventDto>> violations = validator.validate(validDto);
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
        }
    }
}
