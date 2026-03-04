package ru.practicum.ewm.main.compilation.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.ewm.main.event.dto.EventShortDto;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CompilationDtoTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateValidCompilationDto() {
        CompilationDto dto = CompilationDto.builder()
                .id(1L)
                .pinned(true)
                .title("Летние события")
                .events(List.of(new EventShortDto()))
                .build();

        Set<?> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }
}
