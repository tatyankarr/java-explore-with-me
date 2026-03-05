package ru.practicum.ewm.main.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.main.comment.dto.CommentDto;
import ru.practicum.ewm.main.comment.dto.NewCommentDto;
import ru.practicum.ewm.main.comment.service.CommentService;
import ru.practicum.ewm.main.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    private CommentDto commentDto;
    private NewCommentDto newCommentDto;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    void setUp() {
        UserShortDto author = UserShortDto.builder()
                .id(1L)
                .name("Test Author")
                .build();

        commentDto = CommentDto.builder()
                .id(1L)
                .text("Test comment")
                .author(author)
                .created(LocalDateTime.now().format(formatter))
                .edited(null)
                .build();

        newCommentDto = new NewCommentDto();
        newCommentDto.setText("New comment");
    }

    @Test
    void addComment_ShouldReturnCreatedComment() throws Exception {
        when(commentService.addComment(anyLong(), anyLong(), any(NewCommentDto.class)))
                .thenReturn(commentDto);

        mockMvc.perform(post("/users/1/events/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCommentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is("Test comment")))
                .andExpect(jsonPath("$.author.id", is(1)))
                .andExpect(jsonPath("$.author.name", is("Test Author")))
                .andExpect(jsonPath("$.created", notNullValue()))
                .andExpect(jsonPath("$.edited", nullValue()));

        verify(commentService, times(1)).addComment(1L, 1L, newCommentDto);
    }

    @Test
    void addComment_WithInvalidText_ShouldReturnBadRequest() throws Exception {
        NewCommentDto invalidDto = new NewCommentDto();
        invalidDto.setText("");

        mockMvc.perform(post("/users/1/events/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(commentService, never()).addComment(anyLong(), anyLong(), any(NewCommentDto.class));
    }

    @Test
    void updateComment_ShouldReturnUpdatedComment() throws Exception {
        when(commentService.updateComment(anyLong(), anyLong(), any(NewCommentDto.class)))
                .thenReturn(commentDto);

        mockMvc.perform(patch("/users/1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCommentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is("Test comment")));

        verify(commentService, times(1)).updateComment(1L, 1L, newCommentDto);
    }

    @Test
    void deleteCommentByUser_ShouldReturnNoContent() throws Exception {
        doNothing().when(commentService).deleteCommentByUser(anyLong(), anyLong());

        mockMvc.perform(delete("/users/1/comments/1"))
                .andExpect(status().isOk());

        verify(commentService, times(1)).deleteCommentByUser(1L, 1L);
    }

    @Test
    void deleteCommentByAdmin_ShouldReturnNoContent() throws Exception {
        doNothing().when(commentService).deleteCommentByAdmin(anyLong());

        mockMvc.perform(delete("/admin/comments/1"))
                .andExpect(status().isOk());

        verify(commentService, times(1)).deleteCommentByAdmin(1L);
    }

    @Test
    void getCommentsByEvent_ShouldReturnListOfComments() throws Exception {
        List<CommentDto> comments = List.of(commentDto);
        when(commentService.getCommentsByEvent(anyLong(), anyInt(), anyInt()))
                .thenReturn(comments);

        mockMvc.perform(get("/events/1/comments")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].text", is("Test comment")));

        verify(commentService, times(1)).getCommentsByEvent(1L, 0, 10);
    }

    @Test
    void getCommentsByEvent_WithDefaultParams_ShouldUseDefaultValues() throws Exception {
        List<CommentDto> comments = List.of(commentDto);
        when(commentService.getCommentsByEvent(anyLong(), anyInt(), anyInt()))
                .thenReturn(comments);

        mockMvc.perform(get("/events/1/comments"))
                .andExpect(status().isOk());

        verify(commentService, times(1)).getCommentsByEvent(1L, 0, 10);
    }
}