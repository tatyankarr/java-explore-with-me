package ru.practicum.ewm.main.location.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationDto {
    @NotNull
    private Float lat;
    @NotNull
    private Float lon;
}