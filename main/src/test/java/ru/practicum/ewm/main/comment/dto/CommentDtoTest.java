package ru.practicum.ewm.main.comment.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.ewm.main.user.dto.UserShortDto;

import static org.junit.jupiter.api.Assertions.*;

class CommentDtoTest {

    @Test
    void commentDto_ShouldCreateWithAllFields() {
        UserShortDto author = UserShortDto.builder()
                .id(1L)
                .name("Test Author")
                .build();

        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("Test comment text")
                .author(author)
                .created("2024-01-01 12:00:00")
                .edited("2024-01-01 13:00:00")
                .build();

        assertNotNull(commentDto);
        assertEquals(1L, commentDto.getId());
        assertEquals("Test comment text", commentDto.getText());
        assertNotNull(commentDto.getAuthor());
        assertEquals(1L, commentDto.getAuthor().getId());
        assertEquals("Test Author", commentDto.getAuthor().getName());
        assertEquals("2024-01-01 12:00:00", commentDto.getCreated());
        assertEquals("2024-01-01 13:00:00", commentDto.getEdited());
    }

    @Test
    void commentDto_ShouldAllowNullEdited() {
        UserShortDto author = UserShortDto.builder()
                .id(1L)
                .name("Test Author")
                .build();

        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("Test comment text")
                .author(author)
                .created("2024-01-01 12:00:00")
                .edited(null)
                .build();

        assertNotNull(commentDto);
        assertNull(commentDto.getEdited());
    }

    @Test
    void commentDto_EqualsAndHashCode_ShouldWorkCorrectly() {
        UserShortDto author1 = UserShortDto.builder().id(1L).name("Author1").build();
        UserShortDto author2 = UserShortDto.builder().id(1L).name("Author1").build();

        CommentDto comment1 = CommentDto.builder()
                .id(1L)
                .text("Text")
                .author(author1)
                .created("2024-01-01 12:00:00")
                .build();

        CommentDto comment2 = CommentDto.builder()
                .id(1L)
                .text("Text")
                .author(author2)
                .created("2024-01-01 12:00:00")
                .build();

        CommentDto comment3 = CommentDto.builder()
                .id(2L)
                .text("Different text")
                .author(author1)
                .created("2024-01-01 12:00:00")
                .build();

        assertEquals(comment1, comment2);
        assertNotEquals(comment1, comment3);
        assertEquals(comment1.hashCode(), comment2.hashCode());
        assertNotEquals(comment1.hashCode(), comment3.hashCode());
    }

    @Test
    void commentDto_ToString_ShouldContainFields() {
        UserShortDto author = UserShortDto.builder().id(1L).name("Test Author").build();
        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("Test text")
                .author(author)
                .created("2024-01-01 12:00:00")
                .build();

        String toString = commentDto.toString();
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("text=Test text"));
        assertTrue(toString.contains("author=UserShortDto(id=1, name=Test Author)"));
        assertTrue(toString.contains("created=2024-01-01 12:00:00"));
    }
}