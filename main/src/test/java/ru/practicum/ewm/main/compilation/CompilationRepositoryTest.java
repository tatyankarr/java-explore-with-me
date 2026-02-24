package ru.practicum.ewm.main.compilation;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompilationRepositoryTest {

    @Test
    void shouldFindAllByPinned() {
        CompilationRepository repository = mock(CompilationRepository.class);
        Pageable pageable = mock(Pageable.class);

        List<Compilation> expected = List.of(
                Compilation.builder().id(1L).pinned(true).title("Летние события").build(),
                Compilation.builder().id(2L).pinned(true).title("Зимние события").build()
        );

        when(repository.findAllByPinned(true, pageable)).thenReturn(expected);

        List<Compilation> result = repository.findAllByPinned(true, pageable);

        assertEquals(2, result.size());
        assertTrue(result.get(0).getPinned());
        assertTrue(result.get(1).getPinned());
        verify(repository, times(1)).findAllByPinned(true, pageable);
    }

    @Test
    void shouldSaveCompilation() {
        CompilationRepository repository = mock(CompilationRepository.class);
        Compilation compilation = Compilation.builder().title("Летние события").pinned(true).build();
        Compilation saved = Compilation.builder().id(1L).title("Летние события").pinned(true).build();

        when(repository.save(compilation)).thenReturn(saved);

        Compilation result = repository.save(compilation);

        assertEquals(1L, result.getId());
        assertEquals("Летние события", result.getTitle());
        verify(repository, times(1)).save(compilation);
    }

    @Test
    void shouldFindById() {
        CompilationRepository repository = mock(CompilationRepository.class);
        Compilation compilation = Compilation.builder().id(1L).title("Летние события").build();

        when(repository.findById(1L)).thenReturn(java.util.Optional.of(compilation));

        var result = repository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Летние события", result.get().getTitle());
        verify(repository, times(1)).findById(1L);
    }
}