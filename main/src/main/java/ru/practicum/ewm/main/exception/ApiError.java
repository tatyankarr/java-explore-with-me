package ru.practicum.ewm.main.exception;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiError {
    private List<String> errors;
    private String message;
    private String reason;
    private String status;
    private String timestamp;
}
