package ru.practicum.ewm.main.compilation.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NewCompilationDtoTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateValidNewCompilationDto() {
        NewCompilationDto dto = NewCompilationDto.builder()
                .title("Летние события")
                .pinned(true)
                .events(List.of(1L, 2L))
                .build();

        Set<ConstraintViolation<NewCompilationDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldSetDefaultPinnedValue() {
        NewCompilationDto dto = NewCompilationDto.builder()
                .title("Летние события")
                .build();

        assertNull(dto.getPinned(), "Поле pinned должно быть null при создании через builder без указания значения");
    }

    @Test
    void shouldFailValidationWhenTitleIsBlank() {
        NewCompilationDto dto = NewCompilationDto.builder()
                .title("")
                .build();

        Set<ConstraintViolation<NewCompilationDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @Test
    void shouldFailValidationWhenTitleIsTooLong() {
        NewCompilationDto dto = NewCompilationDto.builder()
                .title("a".repeat(51))
                .build();

        Set<ConstraintViolation<NewCompilationDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @Test
    void shouldFailValidationWhenTitleIsOnlySpaces() {
        NewCompilationDto dto = NewCompilationDto.builder()
                .title("   ")
                .build();

        Set<ConstraintViolation<NewCompilationDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @Test
    void shouldCreateValidNewCompilationDtoWithoutEvents() {
        NewCompilationDto dto = NewCompilationDto.builder()
                .title("Летние события")
                .pinned(true)
                .build();

        Set<ConstraintViolation<NewCompilationDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
        assertNull(dto.getEvents());
    }
}
