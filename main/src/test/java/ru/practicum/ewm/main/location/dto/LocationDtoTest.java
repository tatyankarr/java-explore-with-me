package ru.practicum.ewm.main.location.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LocationDtoTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Должен создать валидный LocationDto")
    void shouldCreateValidLocationDto() {
        LocationDto dto = LocationDto.builder()
                .lat(55.75f)
                .lon(37.62f)
                .build();

        Set<?> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Должен провалить валидацию при null координатах")
    void shouldFailValidationWhenCoordinatesAreNull() {
        LocationDto dto = LocationDto.builder()
                .lat(null)
                .lon(null)
                .build();

        Set<?> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals(2, violations.size());
    }
}
