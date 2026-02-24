package ru.practicum.ewm.main.user;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserRepositoryTest {

    @Test
    void findAllByIdIn_ShouldReturnUsers() {
        UserRepository repository = mock(UserRepository.class);
        Pageable pageable = mock(Pageable.class);
        List<User> expected = List.of(mock(User.class), mock(User.class));

        when(repository.findAllByIdIn(List.of(1L, 2L), pageable)).thenReturn(expected);

        List<User> result = repository.findAllByIdIn(List.of(1L, 2L), pageable);

        assertEquals(2, result.size());
        verify(repository, times(1)).findAllByIdIn(List.of(1L, 2L), pageable);
    }

    @Test
    void findAllByIdIn_ShouldReturnEmptyList_WhenNoUsers() {
        UserRepository repository = mock(UserRepository.class);
        Pageable pageable = mock(Pageable.class);

        when(repository.findAllByIdIn(List.of(999L), pageable)).thenReturn(List.of());

        List<User> result = repository.findAllByIdIn(List.of(999L), pageable);

        assertTrue(result.isEmpty());
    }

    @Test
    void save_ShouldReturnSavedUser() {
        UserRepository repository = mock(UserRepository.class);
        User user = mock(User.class);
        User saved = mock(User.class);

        when(repository.save(user)).thenReturn(saved);

        User result = repository.save(user);

        assertEquals(saved, result);
        verify(repository, times(1)).save(user);
    }

    @Test
    void findById_ShouldReturnUser() {
        UserRepository repository = mock(UserRepository.class);
        User user = mock(User.class);

        when(repository.findById(1L)).thenReturn(java.util.Optional.of(user));

        var result = repository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void existsById_ShouldReturnTrue_WhenExists() {
        UserRepository repository = mock(UserRepository.class);

        when(repository.existsById(1L)).thenReturn(true);

        boolean result = repository.existsById(1L);

        assertTrue(result);
        verify(repository, times(1)).existsById(1L);
    }

    @Test
    void deleteById_ShouldDeleteUser() {
        UserRepository repository = mock(UserRepository.class);

        repository.deleteById(1L);

        verify(repository, times(1)).deleteById(1L);
    }
}