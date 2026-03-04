package ru.practicum.ewm.main.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.user.dto.NewUserRequest;
import ru.practicum.ewm.main.user.dto.UserDto;
import ru.practicum.ewm.main.user.service.UserServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private NewUserRequest newUserRequest;
    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        newUserRequest = NewUserRequest.builder()
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build();

        user = User.builder()
                .id(1L)
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
    void registerUser_ShouldRegisterUser_WhenValid() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.registerUser(newUserRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Иван Иванов", result.getName());
        assertEquals("ivan@example.com", result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_ShouldThrowConflictException_WhenEmailExists() {
        when(userRepository.save(any(User.class))).thenThrow(DataIntegrityViolationException.class);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.registerUser(newUserRequest));

        assertEquals("Email must be unique", exception.getMessage());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void getUsers_ShouldReturnUsersByIds_WhenIdsProvided() {
        when(userRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(user));

        List<UserDto> result = userService.getUsers(List.of(1L, 2L), 0, 10);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(userRepository, times(1)).findAllById(List.of(1L, 2L));
        verify(userRepository, never()).findAll(any(PageRequest.class));
    }

    @Test
    void getUsers_ShouldReturnAllUsers_WhenIdsNotProvided() {
        List<User> users = List.of(user);
        when(userRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(users));

        List<UserDto> result = userService.getUsers(null, 0, 10);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(userRepository, times(1)).findAll(any(PageRequest.class));
        verify(userRepository, never()).findAllById(any());
    }

    @Test
    void getUsers_ShouldReturnAllUsers_WhenIdsEmpty() {
        List<User> users = List.of(user);
        when(userRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(users));

        List<UserDto> result = userService.getUsers(List.of(), 0, 10);

        assertEquals(1, result.size());
        verify(userRepository, times(1)).findAll(any(PageRequest.class));
        verify(userRepository, never()).findAllById(any());
    }

    @Test
    void getUsers_ShouldReturnEmptyList_WhenNoUsers() {
        when(userRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        List<UserDto> result = userService.getUsers(null, 0, 10);

        assertTrue(result.isEmpty());
    }

    @Test
    void getUsers_ShouldCalculatePageCorrectly() {
        when(userRepository.findAll(PageRequest.of(2, 10)))
                .thenReturn(new PageImpl<>(List.of()));

        userService.getUsers(null, 20, 10);

        verify(userRepository, times(1)).findAll(PageRequest.of(2, 10));
    }

    @Test
    void delete_ShouldDeleteUser_WhenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_ShouldThrowNotFoundException_WhenUserNotFound() {
        when(userRepository.existsById(999L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.delete(999L));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).deleteById(anyLong());
    }
}