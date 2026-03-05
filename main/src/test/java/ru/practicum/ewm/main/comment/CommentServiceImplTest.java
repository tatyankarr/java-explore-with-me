package ru.practicum.ewm.main.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.main.comment.dto.CommentDto;
import ru.practicum.ewm.main.comment.dto.NewCommentDto;
import ru.practicum.ewm.main.comment.service.CommentServiceImpl;
import ru.practicum.ewm.main.event.EventRepository;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.event.model.EventState;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.user.User;
import ru.practicum.ewm.main.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User user;
    private Event event;
    private Comment comment;
    private NewCommentDto newCommentDto;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        event = Event.builder()
                .id(1L)
                .title("Test Event")
                .state(EventState.PUBLISHED)
                .build();

        comment = Comment.builder()
                .id(1L)
                .text("Test comment text")
                .event(event)
                .author(user)
                .created(now)
                .edited(null)
                .build();

        newCommentDto = new NewCommentDto();
        newCommentDto.setText("New comment text");
    }

    @Test
    void addComment_ShouldSuccessfullyAddComment() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = commentService.addComment(1L, 1L, newCommentDto);

        assertNotNull(result);
        assertEquals(comment.getId(), result.getId());
        assertEquals(comment.getText(), result.getText());
        assertEquals(user.getId(), result.getAuthor().getId());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void addComment_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> commentService.addComment(999L, 1L, newCommentDto));

        assertEquals("User not found", exception.getMessage());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void addComment_WhenEventNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(eventRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> commentService.addComment(1L, 999L, newCommentDto));

        assertEquals("Event not found", exception.getMessage());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void addComment_WhenEventNotPublished_ShouldThrowConflictException() {
        event.setState(EventState.PENDING);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> commentService.addComment(1L, 1L, newCommentDto));

        assertEquals("Cannot comment unpublished event", exception.getMessage());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void updateComment_ShouldSuccessfullyUpdateComment() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(commentRepository.findByIdAndAuthorId(anyLong(), anyLong()))
                .thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = commentService.updateComment(1L, 1L, newCommentDto);

        assertNotNull(result);
        assertEquals(newCommentDto.getText(), comment.getText());
        assertNotNull(comment.getEdited());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void updateComment_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> commentService.updateComment(999L, 1L, newCommentDto));

        assertEquals("User with id=999 was not found", exception.getMessage());
        verify(commentRepository, never()).findByIdAndAuthorId(anyLong(), anyLong());
    }

    @Test
    void updateComment_WhenCommentNotFound_ShouldThrowNotFoundException() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(commentRepository.findByIdAndAuthorId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> commentService.updateComment(1L, 999L, newCommentDto));

        assertEquals("Comment not found or you are not the author", exception.getMessage());
    }

    @Test
    void updateComment_WhenUserIsNotAuthor_ShouldThrowNotFoundException() {
        User anotherUser = User.builder().id(2L).build();
        comment.setAuthor(anotherUser);

        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(commentRepository.findByIdAndAuthorId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> commentService.updateComment(1L, 1L, newCommentDto));

        assertEquals("Comment not found or you are not the author", exception.getMessage());
    }

    @Test
    void deleteCommentByUser_ShouldSuccessfullyDeleteComment() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(commentRepository.findByIdAndAuthorId(anyLong(), anyLong()))
                .thenReturn(Optional.of(comment));

        commentService.deleteCommentByUser(1L, 1L);

        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void deleteCommentByUser_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> commentService.deleteCommentByUser(999L, 1L));

        assertEquals("User with id=999 was not found", exception.getMessage());
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    void deleteCommentByUser_WhenCommentNotFound_ShouldThrowNotFoundException() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        when(commentRepository.findByIdAndAuthorId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> commentService.deleteCommentByUser(1L, 999L));

        assertEquals("Comment not found or you are not the author", exception.getMessage());
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    void deleteCommentByAdmin_ShouldSuccessfullyDeleteComment() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));

        commentService.deleteCommentByAdmin(1L);

        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void deleteCommentByAdmin_WhenCommentNotFound_ShouldThrowNotFoundException() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> commentService.deleteCommentByAdmin(999L));

        assertEquals("Comment not found", exception.getMessage());
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    void getCommentsByEvent_ShouldReturnListOfComments() {
        PageImpl<Comment> commentPage = new PageImpl<>(List.of(comment));
        when(eventRepository.existsById(anyLong())).thenReturn(true);
        when(commentRepository.findAllByEventId(anyLong(), any(Pageable.class)))
                .thenReturn(commentPage);

        List<CommentDto> result = commentService.getCommentsByEvent(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(comment.getId(), result.get(0).getId());
        assertEquals(comment.getText(), result.get(0).getText());
    }

    @Test
    void getCommentsByEvent_WhenEventNotFound_ShouldThrowNotFoundException() {
        when(eventRepository.existsById(anyLong())).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> commentService.getCommentsByEvent(999L, 0, 10));

        assertEquals("Event not found", exception.getMessage());
        verify(commentRepository, never()).findAllByEventId(anyLong(), any(Pageable.class));
    }

    @Test
    void getCommentsByEvent_ShouldReturnEmptyListWhenNoComments() {
        PageImpl<Comment> emptyPage = new PageImpl<>(List.of());
        when(eventRepository.existsById(anyLong())).thenReturn(true);
        when(commentRepository.findAllByEventId(anyLong(), any(Pageable.class)))
                .thenReturn(emptyPage);

        List<CommentDto> result = commentService.getCommentsByEvent(1L, 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}