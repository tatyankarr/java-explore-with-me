package ru.practicum.ewm.main.user.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserDtoTest {

    @Test
    void shouldCreateUserDtoWithBuilder() {
        UserDto dto = UserDto.builder()
                .id(1L)
                .email("user@example.com")
                .name("Иван Иванов")
                .build();

        assertEquals(1L, dto.getId());
        assertEquals("user@example.com", dto.getEmail());
        assertEquals("Иван Иванов", dto.getName());
    }

    @Test
    void shouldCreateUserDtoWithAllArgsConstructor() {
        UserDto dto = new UserDto(1L, "user@example.com", "Иван Иванов");

        assertEquals(1L, dto.getId());
        assertEquals("user@example.com", dto.getEmail());
        assertEquals("Иван Иванов", dto.getName());
    }

    @Test
    void shouldWorkEqualsAndHashCode() {
        UserDto dto1 = new UserDto(1L, "user@example.com", "Иван");
        UserDto dto2 = new UserDto(1L, "user@example.com", "Иван");
        UserDto dto3 = new UserDto(2L, "other@example.com", "Петр");

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
    }
}