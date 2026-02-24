package ru.practicum.ewm.main.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewCategoryDto {
    @NotBlank
    @Size(min = 1, max = 50)
    private String name;
}
