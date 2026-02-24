package ru.practicum.ewm.main.category.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NewCategoryDtoTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateValidNewCategoryDto() {
        NewCategoryDto dto = NewCategoryDto.builder()
                .name("Концерты")
                .build();

        Set<ConstraintViolation<NewCategoryDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenNameIsBlank() {
        NewCategoryDto dto = NewCategoryDto.builder()
                .name("")
                .build();

        Set<ConstraintViolation<NewCategoryDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void shouldFailValidationWhenNameIsTooLong() {
        NewCategoryDto dto = NewCategoryDto.builder()
                .name("a".repeat(51))
                .build();

        Set<ConstraintViolation<NewCategoryDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }
}
