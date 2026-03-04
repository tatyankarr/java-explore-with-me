package ru.practicum.ewm.main.exception;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApiErrorTest {

    @Test
    void shouldCreateApiErrorWithBuilder() {
        ApiError error = ApiError.builder()
                .status("NOT_FOUND")
                .reason("The required object was not found.")
                .message("Category not found")
                .timestamp("2024-01-01 12:00:00")
                .build();

        assertEquals("NOT_FOUND", error.getStatus());
        assertEquals("The required object was not found.", error.getReason());
        assertEquals("Category not found", error.getMessage());
        assertEquals("2024-01-01 12:00:00", error.getTimestamp());
        assertNull(error.getErrors());
    }

    @Test
    void shouldCreateApiErrorWithAllArgsConstructor() {
        ApiError error = new ApiError(
                List.of("error1", "error2"),
                "message",
                "reason",
                "INTERNAL_SERVER_ERROR",
                "2024-01-01 12:00:00"
        );

        assertEquals(2, error.getErrors().size());
        assertEquals("message", error.getMessage());
        assertEquals("reason", error.getReason());
        assertEquals("INTERNAL_SERVER_ERROR", error.getStatus());
        assertEquals("2024-01-01 12:00:00", error.getTimestamp());
    }

    @Test
    void shouldWorkEqualsAndHashCode() {
        ApiError error1 = ApiError.builder().status("NOT_FOUND").build();
        ApiError error2 = ApiError.builder().status("NOT_FOUND").build();
        ApiError error3 = ApiError.builder().status("BAD_REQUEST").build();

        assertEquals(error1, error2);
        assertEquals(error1.hashCode(), error2.hashCode());
        assertNotEquals(error1, error3);
    }
}