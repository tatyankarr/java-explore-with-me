package ru.practicum.ewm.main.comment.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.main.comment.Comment;
import ru.practicum.ewm.main.user.dto.UserMapper;
import ru.practicum.ewm.main.util.Constants;

@UtilityClass
public class CommentMapper {

    public CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .author(UserMapper.toUserShortDto(comment.getAuthor()))
                .created(comment.getCreated().format(Constants.FORMATTER))
                .edited(comment.getEdited() != null ?
                        comment.getEdited().format(Constants.FORMATTER) : null)
                .build();
    }
}