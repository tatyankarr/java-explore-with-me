package ru.practicum.ewm.main.compilation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.ewm.main.category.Category;
import ru.practicum.ewm.main.compilation.Compilation;
import ru.practicum.ewm.main.compilation.CompilationRepository;
import ru.practicum.ewm.main.compilation.dto.CompilationDto;
import ru.practicum.ewm.main.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.main.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.main.compilation.service.CompilationServiceImpl;
import ru.practicum.ewm.main.event.EventRepository;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.user.User;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompilationServiceImplTest {

    @Mock
    private CompilationRepository compilationRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private CompilationServiceImpl compilationService;

    private NewCompilationDto newCompilationDto;
    private Compilation compilation;
    private Event event1;
    private Event event2;
    private Set<Event> events;
    private Category category;
    private User initiator;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        initiator = User.builder()
                .id(1L)
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build();

        event1 = Event.builder()
                .id(1L)
                .title("Событие 1")
                .annotation("Аннотация 1")
                .description("Описание 1")
                .eventDate(now.plusDays(10))
                .category(category)
                .initiator(initiator)
                .paid(false)
                .build();

        event2 = Event.builder()
                .id(2L)
                .title("Событие 2")
                .annotation("Аннотация 2")
                .description("Описание 2")
                .eventDate(now.plusDays(20))
                .category(category)
                .initiator(initiator)
                .paid(true)
                .build();

        events = new HashSet<>(Set.of(event1, event2));

        compilation = Compilation.builder()
                .id(1L)
                .title("Летние события")
                .pinned(true)
                .events(events)
                .build();

        newCompilationDto = NewCompilationDto.builder()
                .title("Летние события")
                .pinned(true)
                .events(List.of(1L, 2L))
                .build();
    }

    @Test
    void saveCompilation_ShouldSaveCompilationWithEvents() {
        when(eventRepository.findAllByIdIn(List.of(1L, 2L))).thenReturn(List.of(event1, event2));
        when(compilationRepository.save(any(Compilation.class))).thenReturn(compilation);

        CompilationDto result = compilationService.saveCompilation(newCompilationDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Летние события", result.getTitle());
        assertTrue(result.getPinned());
        assertEquals(2, result.getEvents().size());
        verify(eventRepository, times(1)).findAllByIdIn(List.of(1L, 2L));
        verify(compilationRepository, times(1)).save(any(Compilation.class));
    }

    @Test
    void saveCompilation_ShouldSaveCompilationWithoutEvents() {
        NewCompilationDto dtoWithoutEvents = NewCompilationDto.builder()
                .title("Летние события")
                .pinned(true)
                .build();

        compilation.setEvents(new HashSet<>());

        when(compilationRepository.save(any(Compilation.class))).thenReturn(compilation);

        CompilationDto result = compilationService.saveCompilation(dtoWithoutEvents);

        assertNotNull(result);
        assertEquals("Летние события", result.getTitle());
        assertTrue(result.getPinned());
        assertTrue(result.getEvents().isEmpty());
        verify(eventRepository, never()).findAllByIdIn(any());
    }

    @Test
    void saveCompilation_ShouldSaveCompilationWithNullPinned() {
        NewCompilationDto dtoWithNullPinned = NewCompilationDto.builder()
                .title("Летние события")
                .events(List.of(1L, 2L))
                .build();

        compilation.setPinned(null);

        when(eventRepository.findAllByIdIn(List.of(1L, 2L))).thenReturn(List.of(event1, event2));
        when(compilationRepository.save(any(Compilation.class))).thenReturn(compilation);

        CompilationDto result = compilationService.saveCompilation(dtoWithNullPinned);

        assertNotNull(result);
        assertEquals("Летние события", result.getTitle());
        assertNull(result.getPinned());
        assertEquals(2, result.getEvents().size());
    }

    @Test
    void updateCompilation_ShouldUpdateAllFields() {
        UpdateCompilationRequest updateRequest = UpdateCompilationRequest.builder()
                .title("Обновленный заголовок")
                .pinned(false)
                .events(List.of(3L))
                .build();

        Event event3 = Event.builder()
                .id(3L)
                .title("Событие 3")
                .annotation("Аннотация 3")
                .description("Описание 3")
                .eventDate(now.plusDays(30))
                .category(category)
                .initiator(initiator)
                .paid(false)
                .build();

        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));
        when(eventRepository.findAllByIdIn(List.of(3L))).thenReturn(List.of(event3));

        CompilationDto result = compilationService.updateCompilation(1L, updateRequest);

        assertEquals("Обновленный заголовок", result.getTitle());
        assertFalse(result.getPinned());
        assertEquals(1, result.getEvents().size());
        verify(compilationRepository, never()).save(any());
    }

    @Test
    void updateCompilation_ShouldUpdateOnlyTitle() {
        UpdateCompilationRequest updateRequest = UpdateCompilationRequest.builder()
                .title("Обновленный заголовок")
                .build();

        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));

        CompilationDto result = compilationService.updateCompilation(1L, updateRequest);

        assertEquals("Обновленный заголовок", result.getTitle());
        assertTrue(result.getPinned());
        assertEquals(2, result.getEvents().size());
        verify(eventRepository, never()).findAllByIdIn(any());
    }

    @Test
    void updateCompilation_ShouldUpdateOnlyPinned() {
        UpdateCompilationRequest updateRequest = UpdateCompilationRequest.builder()
                .pinned(false)
                .build();

        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));

        CompilationDto result = compilationService.updateCompilation(1L, updateRequest);

        assertEquals("Летние события", result.getTitle());
        assertFalse(result.getPinned());
        assertEquals(2, result.getEvents().size());
        verify(eventRepository, never()).findAllByIdIn(any());
    }

    @Test
    void updateCompilation_ShouldUpdateOnlyEvents() {
        UpdateCompilationRequest updateRequest = UpdateCompilationRequest.builder()
                .events(List.of(1L))
                .build();

        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));
        when(eventRepository.findAllByIdIn(List.of(1L))).thenReturn(List.of(event1));

        CompilationDto result = compilationService.updateCompilation(1L, updateRequest);

        assertEquals("Летние события", result.getTitle());
        assertTrue(result.getPinned());
        assertEquals(1, result.getEvents().size());
    }

    @Test
    void updateCompilation_ShouldThrowNotFoundException_WhenCompilationNotFound() {
        when(compilationRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> compilationService.updateCompilation(999L, new UpdateCompilationRequest()));

        assertEquals("Compilation not found", exception.getMessage());
    }

    @Test
    void deleteCompilation_ShouldDeleteCompilation() {
        when(compilationRepository.existsById(1L)).thenReturn(true);

        compilationService.deleteCompilation(1L);

        verify(compilationRepository, times(1)).existsById(1L);
        verify(compilationRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCompilation_ShouldThrowNotFoundException_WhenCompilationNotFound() {
        when(compilationRepository.existsById(999L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> compilationService.deleteCompilation(999L));

        assertEquals("Compilation not found", exception.getMessage());
        verify(compilationRepository, never()).deleteById(any());
    }

    @Test
    void getCompilations_ShouldReturnAllCompilations_WhenPinnedIsNull() {
        List<Compilation> compilations = List.of(compilation);
        when(compilationRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(compilations));

        List<CompilationDto> result = compilationService.getCompilations(null, 0, 10);

        assertEquals(1, result.size());
        assertEquals("Летние события", result.get(0).getTitle());
        verify(compilationRepository, never()).findAllByPinned(any(), any());
    }

    @Test
    void getCompilations_ShouldReturnPinnedCompilations_WhenPinnedIsTrue() {
        List<Compilation> compilations = List.of(compilation);
        when(compilationRepository.findAllByPinned(eq(true), any(PageRequest.class)))
                .thenReturn(compilations);

        List<CompilationDto> result = compilationService.getCompilations(true, 0, 10);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getPinned());
        verify(compilationRepository, times(1)).findAllByPinned(eq(true), any(PageRequest.class));
    }

    @Test
    void getCompilations_ShouldReturnUnpinnedCompilations_WhenPinnedIsFalse() {
        compilation.setPinned(false);
        List<Compilation> compilations = List.of(compilation);
        when(compilationRepository.findAllByPinned(eq(false), any(PageRequest.class)))
                .thenReturn(compilations);

        List<CompilationDto> result = compilationService.getCompilations(false, 0, 10);

        assertEquals(1, result.size());
        assertFalse(result.get(0).getPinned());
        verify(compilationRepository, times(1)).findAllByPinned(eq(false), any(PageRequest.class));
    }

    @Test
    void getCompilations_ShouldReturnEmptyList_WhenNoCompilations() {
        when(compilationRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        List<CompilationDto> result = compilationService.getCompilations(null, 0, 10);

        assertTrue(result.isEmpty());
    }

    @Test
    void getCompilations_ShouldCalculatePageCorrectly() {
        PageImpl<Compilation> emptyPage = new PageImpl<>(List.of());

        when(compilationRepository.findAll(PageRequest.of(2, 10))).thenReturn(emptyPage);
        compilationService.getCompilations(null, 20, 10);
        verify(compilationRepository, times(1)).findAll(PageRequest.of(2, 10));

        when(compilationRepository.findAllByPinned(eq(true), eq(PageRequest.of(1, 5)))).thenReturn(List.of());
        compilationService.getCompilations(true, 5, 5);
        verify(compilationRepository, times(1)).findAllByPinned(eq(true), eq(PageRequest.of(1, 5)));
    }

    @Test
    void getCompilation_ShouldReturnCompilation() {
        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));

        CompilationDto result = compilationService.getCompilation(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Летние события", result.getTitle());
        assertEquals(2, result.getEvents().size());
    }

    @Test
    void getCompilation_ShouldThrowNotFoundException_WhenCompilationNotFound() {
        when(compilationRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> compilationService.getCompilation(999L));

        assertEquals("Compilation not found", exception.getMessage());
    }

    @Test
    void getCompilation_ShouldReturnCompilationWithEmptyEvents() {
        compilation.setEvents(new HashSet<>());
        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));

        CompilationDto result = compilationService.getCompilation(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertTrue(result.getEvents().isEmpty());
    }
}