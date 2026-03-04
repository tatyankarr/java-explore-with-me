package ru.practicum.ewm.main.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import jakarta.validation.ConstraintViolationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ErrorHandlerTest {

    @InjectMocks
    private ErrorHandler errorHandler;

    private NotFoundException notFoundException;
    private ConflictException conflictException;
    private DataIntegrityViolationException dataIntegrityException;
    private BadRequestException badRequestException;
    private ConstraintViolationException constraintViolationException;
    private MethodArgumentNotValidException methodArgumentNotValidException;
    private MissingServletRequestParameterException missingParameterException;
    private Throwable throwable;

    @BeforeEach
    void setUp() {
        notFoundException = new NotFoundException("Category not found");
        conflictException = new ConflictException("Email must be unique");
        dataIntegrityException = new DataIntegrityViolationException("Duplicate key");
        badRequestException = new BadRequestException("Invalid request");
        constraintViolationException = mock(ConstraintViolationException.class);
        methodArgumentNotValidException = mock(MethodArgumentNotValidException.class);
        missingParameterException = new MissingServletRequestParameterException("param", "String");
        throwable = new RuntimeException("Unexpected error");
    }

    @Test
    void handleNotFound_ShouldReturnNotFound() {
        ApiError response = errorHandler.handleNotFound(notFoundException);

        assertEquals("NOT_FOUND", response.getStatus());
        assertEquals("The required object was not found.", response.getReason());
        assertEquals("Category not found", response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void handleConflict_ShouldReturnConflict() {
        ApiError response = errorHandler.handleConflict(conflictException);

        assertEquals("CONFLICT", response.getStatus());
        assertEquals("For the requested operation the conditions are not met.", response.getReason());
        assertEquals("Email must be unique", response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void handleDataIntegrity_ShouldReturnConflict() {
        ApiError response = errorHandler.handleDataIntegrity(dataIntegrityException);

        assertEquals("CONFLICT", response.getStatus());
        assertEquals("Integrity constraint has been violated.", response.getReason());
        assertEquals("Duplicate key", response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void handleBadRequest_WithBadRequestException_ShouldReturnBadRequest() {
        ApiError response = errorHandler.handleBadRequest(badRequestException);

        assertEquals("BAD_REQUEST", response.getStatus());
        assertEquals("Incorrectly made request.", response.getReason());
        assertEquals("Invalid request", response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void handleBadRequest_WithConstraintViolationException_ShouldReturnBadRequest() {
        when(constraintViolationException.getMessage()).thenReturn("Validation failed");

        ApiError response = errorHandler.handleBadRequest(constraintViolationException);

        assertEquals("BAD_REQUEST", response.getStatus());
        assertEquals("Incorrectly made request.", response.getReason());
        assertEquals("Validation failed", response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void handleBadRequest_WithMethodArgumentNotValidException_ShouldReturnBadRequest() {
        when(methodArgumentNotValidException.getMessage()).thenReturn("Argument not valid");

        ApiError response = errorHandler.handleBadRequest(methodArgumentNotValidException);

        assertEquals("BAD_REQUEST", response.getStatus());
        assertEquals("Incorrectly made request.", response.getReason());
        assertEquals("Argument not valid", response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void handleBadRequest_WithMissingServletRequestParameterException_ShouldReturnBadRequest() {
        ApiError response = errorHandler.handleBadRequest(missingParameterException);

        assertEquals("BAD_REQUEST", response.getStatus());
        assertEquals("Incorrectly made request.", response.getReason());
        assertTrue(response.getMessage().contains("param"));
        assertNotNull(response.getTimestamp());
    }

    @Test
    void handleThrowable_ShouldReturnInternalServerError() {
        ApiError response = errorHandler.handleThrowable(throwable);

        assertEquals("INTERNAL_SERVER_ERROR", response.getStatus());
        assertEquals("Error occurred", response.getReason());
        assertEquals("Unexpected error", response.getMessage());
        assertNotNull(response.getErrors());
        assertFalse(response.getErrors().isEmpty());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void handleThrowable_ShouldIncludeStackTrace() {
        ApiError response = errorHandler.handleThrowable(throwable);

        assertNotNull(response.getErrors());
        assertTrue(response.getErrors().size() > 0);
    }
}