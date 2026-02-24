package ru.practicum.ewm.main.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserShortDto {
    @NotNull
    private Long id;
    @NotBlank
    private String name;
}