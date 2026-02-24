package ru.practicum.ewm.main.compilation.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateCompilationRequestTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateValidUpdateCompilationRequest() {
        UpdateCompilationRequest request = UpdateCompilationRequest.builder()
                .title("Обновленный заголовок")
                .pinned(false)
                .events(List.of(1L, 2L))
                .build();

        Set<?> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Должен провалить валидацию при слишком длинном заголовке")
    void shouldFailValidationWhenTitleIsTooLong() {
        UpdateCompilationRequest request = UpdateCompilationRequest.builder()
                .title("a".repeat(51))
                .build();

        Set<?> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }
}
