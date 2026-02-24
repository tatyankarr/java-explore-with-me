package ru.practicum.ewm.main.user.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class NewUserRequestTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateValidNewUserRequest() {
        NewUserRequest request = NewUserRequest.builder()
                .email("user@example.com")
                .name("Иван Иванов")
                .build();

        Set<ConstraintViolation<NewUserRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenEmailIsInvalid() {
        NewUserRequest request = NewUserRequest.builder()
                .email("invalid-email")
                .name("Иван Иванов")
                .build();

        Set<ConstraintViolation<NewUserRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void shouldFailValidationWhenNameIsTooShort() {
        NewUserRequest request = NewUserRequest.builder()
                .email("user@example.com")
                .name("И")
                .build();

        Set<ConstraintViolation<NewUserRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }
}