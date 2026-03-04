package ru.practicum.ewm.main.user.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class UserShortDtoTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateValidUserShortDto() {
        UserShortDto dto = UserShortDto.builder()
                .id(1L)
                .name("Иван Иванов")
                .build();

        Set<ConstraintViolation<UserShortDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenIdIsNull() {
        UserShortDto dto = UserShortDto.builder()
                .name("Иван Иванов")
                .build();

        Set<ConstraintViolation<UserShortDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("id")));
    }

    @Test
    void shouldFailValidationWhenNameIsBlank() {
        UserShortDto dto = UserShortDto.builder()
                .id(1L)
                .name("")
                .build();

        Set<ConstraintViolation<UserShortDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }
}