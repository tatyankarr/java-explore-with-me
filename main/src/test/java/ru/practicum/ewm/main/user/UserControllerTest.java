package ru.practicum.ewm.main.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.main.exception.ErrorHandler;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.user.dto.NewUserRequest;
import ru.practicum.ewm.main.user.dto.UserDto;
import ru.practicum.ewm.main.user.service.UserService;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(ErrorHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private NewUserRequest newUserRequest;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        newUserRequest = NewUserRequest.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build();
    }

    @Test
    void registerUser_ShouldReturnCreatedUser() throws Exception {
        when(userService.registerUser(any(NewUserRequest.class))).thenReturn(userDto);

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Иван Иванов"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"));

        verify(userService, times(1)).registerUser(any(NewUserRequest.class));
    }

    @Test
    void registerUser_ShouldReturnBadRequest_WhenNameIsBlank() throws Exception {
        NewUserRequest invalidRequest = NewUserRequest.builder()
                .name("")
                .email("ivan@example.com")
                .build();

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any());
    }

    @Test
    void registerUser_ShouldReturnBadRequest_WhenNameIsTooShort() throws Exception {
        NewUserRequest invalidRequest = NewUserRequest.builder()
                .name("И")
                .email("ivan@example.com")
                .build();

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any());
    }

    @Test
    void registerUser_ShouldReturnBadRequest_WhenNameIsTooLong() throws Exception {
        NewUserRequest invalidRequest = NewUserRequest.builder()
                .name("a".repeat(251))
                .email("ivan@example.com")
                .build();

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any());
    }

    @Test
    void registerUser_ShouldReturnBadRequest_WhenEmailIsBlank() throws Exception {
        NewUserRequest invalidRequest = NewUserRequest.builder()
                .name("Иван Иванов")
                .email("")
                .build();

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any());
    }

    @Test
    void registerUser_ShouldReturnBadRequest_WhenEmailIsInvalid() throws Exception {
        NewUserRequest invalidRequest = NewUserRequest.builder()
                .name("Иван Иванов")
                .email("invalid-email")
                .build();

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any());
    }

    @Test
    void registerUser_ShouldReturnBadRequest_WhenEmailIsTooShort() throws Exception {
        NewUserRequest invalidRequest = NewUserRequest.builder()
                .name("Иван Иванов")
                .email("a@b.c")
                .build();

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any());
    }

    @Test
    void registerUser_ShouldReturnBadRequest_WhenEmailIsTooLong() throws Exception {
        NewUserRequest invalidRequest = NewUserRequest.builder()
                .name("Иван Иванов")
                .email("a".repeat(250) + "@example.com")
                .build();

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerUser(any());
    }

    @Test
    void registerUser_ShouldReturnConflict_WhenEmailExists() throws Exception {
        when(userService.registerUser(any(NewUserRequest.class)))
                .thenThrow(new ru.practicum.ewm.main.exception.ConflictException("Email must be unique"));

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Email must be unique"));
    }

    @Test
    void getUsers_ShouldReturnUsers_WhenIdsProvided() throws Exception {
        List<UserDto> users = List.of(userDto);
        when(userService.getUsers(List.of(1L, 2L), 0, 10)).thenReturn(users);

        mockMvc.perform(get("/admin/users")
                        .param("ids", "1", "2")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Иван Иванов"));

        verify(userService, times(1)).getUsers(List.of(1L, 2L), 0, 10);
    }

    @Test
    void getUsers_ShouldReturnUsers_WhenNoIdsProvided() throws Exception {
        List<UserDto> users = List.of(userDto);
        when(userService.getUsers(null, 0, 10)).thenReturn(users);

        mockMvc.perform(get("/admin/users")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(userService, times(1)).getUsers(null, 0, 10);
    }

    @Test
    void getUsers_ShouldUseDefaultValues() throws Exception {
        List<UserDto> users = List.of(userDto);
        when(userService.getUsers(null, 0, 10)).thenReturn(users);

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(userService, times(1)).getUsers(null, 0, 10);
    }

    @Test
    void getUsers_ShouldReturnEmptyList_WhenNoUsers() throws Exception {
        when(userService.getUsers(null, 0, 10)).thenReturn(List.of());

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getUsers_ShouldReturnBadRequest_WhenFromIsNegative() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
    }

    @Test
    void getUsers_ShouldReturnBadRequest_WhenSizeIsZero() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
    }

    @Test
    void getUsers_ShouldReturnBadRequest_WhenSizeIsNegative() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .param("from", "0")
                        .param("size", "-5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
    }

    @Test
    void delete_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/admin/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).delete(1L);
    }

    @Test
    void delete_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        doThrow(new NotFoundException("User not found"))
                .when(userService).delete(999L);

        mockMvc.perform(delete("/admin/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}