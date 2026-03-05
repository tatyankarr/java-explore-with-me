package ru.practicum.ewm.main.comment.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.ewm.main.comment.Comment;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.user.User;
import ru.practicum.ewm.main.util.Constants;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentMapperTest {

    private Comment comment;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        User author = User.builder()
                .id(1L)
                .name("Test Author")
                .build();

        Event event = Event.builder()
                .id(1L)
                .build();

        comment = Comment.builder()
                .id(1L)
                .text("Test comment text")
                .author(author)
                .event(event)
                .created(now)
                .edited(null)
                .build();
    }

    @Test
    void toCommentDto_ShouldMapAllFieldsCorrectly() {
        CommentDto dto = CommentMapper.toCommentDto(comment);

        assertNotNull(dto);
        assertEquals(comment.getId(), dto.getId());
        assertEquals(comment.getText(), dto.getText());
        assertNotNull(dto.getAuthor());
        assertEquals(comment.getAuthor().getId(), dto.getAuthor().getId());
        assertEquals(comment.getAuthor().getName(), dto.getAuthor().getName());
        assertEquals(now.format(Constants.FORMATTER), dto.getCreated());
        assertNull(dto.getEdited());
    }

    @Test
    void toCommentDto_WhenCommentHasEdited_ShouldMapEditedField() {
        LocalDateTime edited = LocalDateTime.now().plusHours(1);
        comment.setEdited(edited);

        CommentDto dto = CommentMapper.toCommentDto(comment);

        assertNotNull(dto);
        assertEquals(edited.format(Constants.FORMATTER), dto.getEdited());
    }
}