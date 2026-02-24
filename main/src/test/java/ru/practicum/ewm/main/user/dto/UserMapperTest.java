package ru.practicum.ewm.main.user.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.ewm.main.user.User;
import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void shouldConvertNewUserRequestToUser() {
        NewUserRequest request = NewUserRequest.builder()
                .email("user@example.com")
                .name("Иван Иванов")
                .build();

        User user = UserMapper.toUser(request);

        assertNull(user.getId());
        assertEquals(request.getEmail(), user.getEmail());
        assertEquals(request.getName(), user.getName());
    }

    @Test
    void shouldConvertUserToUserDto() {
        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .name("Иван Иванов")
                .build();

        UserDto dto = UserMapper.toUserDto(user);

        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getEmail(), dto.getEmail());
        assertEquals(user.getName(), dto.getName());
    }

    @Test
    void shouldConvertUserToUserShortDto() {
        User user = User.builder()
                .id(1L)
                .name("Иван Иванов")
                .email("user@example.com")
                .build();

        UserShortDto dto = UserMapper.toUserShortDto(user);

        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getName(), dto.getName());
        assertNotEquals(user.getEmail(), dto.getName());
    }
}