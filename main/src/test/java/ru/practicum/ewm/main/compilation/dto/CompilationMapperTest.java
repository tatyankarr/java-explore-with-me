package ru.practicum.ewm.main.compilation.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.ewm.main.compilation.Compilation;
import ru.practicum.ewm.main.event.dto.EventShortDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompilationMapperTest {
    @Test
    void shouldConvertNewCompilationDtoToCompilation() {
        NewCompilationDto dto = NewCompilationDto.builder()
                .title("Летние события")
                .pinned(true)
                .build();

        Compilation compilation = CompilationMapper.toCompilation(dto);

        assertNotNull(compilation);
        assertNull(compilation.getId());
        assertEquals("Летние события", compilation.getTitle());
        assertTrue(compilation.getPinned());
        assertNull(compilation.getEvents());
    }

    @Test
    void shouldConvertCompilationToCompilationDto() {
        Compilation compilation = Compilation.builder()
                .id(1L)
                .title("Летние события")
                .pinned(true)
                .build();
        List<EventShortDto> events = List.of(new EventShortDto());

        CompilationDto dto = CompilationMapper.toCompilationDto(compilation, events);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Летние события", dto.getTitle());
        assertTrue(dto.getPinned());
        assertEquals(events, dto.getEvents());
    }
}
