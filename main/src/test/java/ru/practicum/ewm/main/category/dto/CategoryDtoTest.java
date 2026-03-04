package ru.practicum.ewm.main.category.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CategoryDtoTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateValidCategoryDto() {
        CategoryDto dto = CategoryDto.builder()
                .id(1L)
                .name("Концерты")
                .build();

        Set<ConstraintViolation<CategoryDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenNameIsBlank() {
        CategoryDto dto = CategoryDto.builder()
                .id(1L)
                .name("")
                .build();

        Set<ConstraintViolation<CategoryDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void shouldFailValidationWhenNameIsTooLong() {
        CategoryDto dto = CategoryDto.builder()
                .id(1L)
                .name("a".repeat(51))
                .build();

        Set<ConstraintViolation<CategoryDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
    }

    @Test
    void shouldFailValidationWhenNameIsOnlySpaces() {
        CategoryDto dto = CategoryDto.builder()
                .id(1L)
                .name("   ")
                .build();

        Set<ConstraintViolation<CategoryDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }
}