package ru.practicum.ewm.main.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewCommentDto {

    @NotBlank(message = "не должно быть пустым")
    @Size(min = 1, max = 2000)
    private String text;
}
