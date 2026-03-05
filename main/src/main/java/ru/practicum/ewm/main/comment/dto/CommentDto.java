package ru.practicum.ewm.main.comment.dto;

import lombok.*;
import ru.practicum.ewm.main.user.dto.UserShortDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private String text;
    private UserShortDto author;
    private String created;
    private String edited;
}