package ru.practicum.ewm.main.comment.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NewCommentDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void newCommentDto_WhenValidText_ShouldPassValidation() {
        NewCommentDto dto = new NewCommentDto();
        dto.setText("Valid comment text");

        Set<ConstraintViolation<NewCommentDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void newCommentDto_WhenTextIsNull_ShouldFailValidation() {
        NewCommentDto dto = new NewCommentDto();
        dto.setText(null);

        Set<ConstraintViolation<NewCommentDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("не должно быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void newCommentDto_WhenTextIsEmpty_ShouldFailValidation() {
        NewCommentDto dto = new NewCommentDto();
        dto.setText("");

        Set<ConstraintViolation<NewCommentDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Expected validation violations for empty text");
    }

    @Test
    void newCommentDto_WhenTextIsBlank_ShouldFailValidation() {
        NewCommentDto dto = new NewCommentDto();
        dto.setText("   ");

        Set<ConstraintViolation<NewCommentDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Expected validation violations for blank text");
    }

    @Test
    void newCommentDto_WhenTextIsTooShort_ShouldFailValidation() {
        NewCommentDto dto = new NewCommentDto();
        dto.setText("");

        Set<ConstraintViolation<NewCommentDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("size must be between 1 and 2000") ||
                        v.getMessage().contains("не должно быть пустым")));
    }

    @Test
    void newCommentDto_WhenTextIsTooLong_ShouldFailValidation() {
        NewCommentDto dto = new NewCommentDto();
        dto.setText("a".repeat(2001));

        Set<ConstraintViolation<NewCommentDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("2000"));
    }

    @Test
    void newCommentDto_WhenTextIsExactlyMaxLength_ShouldPassValidation() {
        NewCommentDto dto = new NewCommentDto();
        dto.setText("a".repeat(2000));

        Set<ConstraintViolation<NewCommentDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void newCommentDto_WhenTextIsExactlyMinLength_ShouldPassValidation() {
        NewCommentDto dto = new NewCommentDto();
        dto.setText("a");

        Set<ConstraintViolation<NewCommentDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void newCommentDto_EqualsAndHashCode_ShouldWorkCorrectly() {
        NewCommentDto dto1 = new NewCommentDto();
        dto1.setText("Test text");

        NewCommentDto dto2 = new NewCommentDto();
        dto2.setText("Test text");

        NewCommentDto dto3 = new NewCommentDto();
        dto3.setText("Different text");

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void newCommentDto_ToString_ShouldContainTextField() {
        NewCommentDto dto = new NewCommentDto();
        dto.setText("Test comment");

        String toString = dto.toString();
        assertTrue(toString.contains("text=Test comment"));
    }
}