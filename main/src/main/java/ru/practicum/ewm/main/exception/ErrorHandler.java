package ru.practicum.ewm.main.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.main.util.Constants;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(final NotFoundException e) {
        return ApiError.builder()
                .status("NOT_FOUND")
                .reason("The required object was not found.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(Constants.FORMATTER))
                .build();
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(final ConflictException e) {
        return ApiError.builder()
                .status("CONFLICT")
                .reason("For the requested operation the conditions are not met.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(Constants.FORMATTER))
                .build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrity(final DataIntegrityViolationException e) {
        return ApiError.builder()
                .status("CONFLICT")
                .reason("Integrity constraint has been violated.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(Constants.FORMATTER))
                .build();
    }

    @ExceptionHandler({
            BadRequestException.class,
            MethodArgumentNotValidException.class,
            MissingServletRequestParameterException.class,
            ConstraintViolationException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(final Exception e) {
        return ApiError.builder()
                .status("BAD_REQUEST")
                .reason("Incorrectly made request.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(Constants.FORMATTER))
                .build();
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(final Throwable e) {
        List<String> errors = Arrays.stream(e.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.toList());

        return ApiError.builder()
                .status("INTERNAL_SERVER_ERROR")
                .reason("Error occurred")
                .message(e.getMessage())
                .errors(errors)
                .timestamp(LocalDateTime.now().format(Constants.FORMATTER))
                .build();
    }
}