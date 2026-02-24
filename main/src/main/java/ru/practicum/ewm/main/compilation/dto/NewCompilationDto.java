package ru.practicum.ewm.main.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewCompilationDto {
    private List<Long> events;
    private Boolean pinned = false;
    @NotBlank
    @Size(min = 1, max = 50)
    private String title;
}
