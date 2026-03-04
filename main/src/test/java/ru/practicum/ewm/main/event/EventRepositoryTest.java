package ru.practicum.ewm.main.event;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.main.event.model.Event;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventRepositoryTest {

    @Test
    void findAllByInitiatorId_ShouldReturnEvents() {
        EventRepository repository = mock(EventRepository.class);
        Pageable pageable = mock(Pageable.class);
        List<Event> expected = List.of(mock(Event.class), mock(Event.class));

        when(repository.findAllByInitiatorId(1L, pageable)).thenReturn(expected);

        List<Event> result = repository.findAllByInitiatorId(1L, pageable);

        assertEquals(2, result.size());
        verify(repository, times(1)).findAllByInitiatorId(1L, pageable);
    }

    @Test
    void findAllByInitiatorId_ShouldReturnEmptyList_WhenNoEvents() {
        EventRepository repository = mock(EventRepository.class);
        Pageable pageable = mock(Pageable.class);

        when(repository.findAllByInitiatorId(999L, pageable)).thenReturn(List.of());

        List<Event> result = repository.findAllByInitiatorId(999L, pageable);

        assertTrue(result.isEmpty());
        verify(repository, times(1)).findAllByInitiatorId(999L, pageable);
    }

    @Test
    void findByIdAndInitiatorId_ShouldReturnEvent() {
        EventRepository repository = mock(EventRepository.class);
        Event event = mock(Event.class);

        when(repository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));

        Optional<Event> result = repository.findByIdAndInitiatorId(1L, 1L);

        assertTrue(result.isPresent());
        assertEquals(event, result.get());
        verify(repository, times(1)).findByIdAndInitiatorId(1L, 1L);
    }

    @Test
    void findByIdAndInitiatorId_ShouldReturnEmpty_WhenNotFound() {
        EventRepository repository = mock(EventRepository.class);

        when(repository.findByIdAndInitiatorId(999L, 1L)).thenReturn(Optional.empty());

        Optional<Event> result = repository.findByIdAndInitiatorId(999L, 1L);

        assertFalse(result.isPresent());
        verify(repository, times(1)).findByIdAndInitiatorId(999L, 1L);
    }

    @Test
    void existsByCategoryId_ShouldReturnTrue_WhenExists() {
        EventRepository repository = mock(EventRepository.class);

        when(repository.existsByCategoryId(1L)).thenReturn(true);

        boolean result = repository.existsByCategoryId(1L);

        assertTrue(result);
        verify(repository, times(1)).existsByCategoryId(1L);
    }

    @Test
    void existsByCategoryId_ShouldReturnFalse_WhenNotExists() {
        EventRepository repository = mock(EventRepository.class);

        when(repository.existsByCategoryId(999L)).thenReturn(false);

        boolean result = repository.existsByCategoryId(999L);

        assertFalse(result);
        verify(repository, times(1)).existsByCategoryId(999L);
    }

    @Test
    void findAllByIdIn_ShouldReturnEvents() {
        EventRepository repository = mock(EventRepository.class);
        List<Event> expected = List.of(mock(Event.class), mock(Event.class));

        when(repository.findAllByIdIn(List.of(1L, 2L))).thenReturn(expected);

        List<Event> result = repository.findAllByIdIn(List.of(1L, 2L));

        assertEquals(2, result.size());
        verify(repository, times(1)).findAllByIdIn(List.of(1L, 2L));
    }

    @Test
    void findAllByIdIn_ShouldReturnEmptyList_WhenNoIds() {
        EventRepository repository = mock(EventRepository.class);

        when(repository.findAllByIdIn(List.of())).thenReturn(List.of());

        List<Event> result = repository.findAllByIdIn(List.of());

        assertTrue(result.isEmpty());
        verify(repository, times(1)).findAllByIdIn(List.of());
    }

    @Test
    void findAllByIdIn_ShouldReturnEmptyList_WhenIdsNotFound() {
        EventRepository repository = mock(EventRepository.class);

        when(repository.findAllByIdIn(List.of(999L, 1000L))).thenReturn(List.of());

        List<Event> result = repository.findAllByIdIn(List.of(999L, 1000L));

        assertTrue(result.isEmpty());
        verify(repository, times(1)).findAllByIdIn(List.of(999L, 1000L));
    }

    @Test
    void save_ShouldReturnSavedEvent() {
        EventRepository repository = mock(EventRepository.class);
        Event event = mock(Event.class);
        Event saved = mock(Event.class);

        when(repository.save(event)).thenReturn(saved);

        Event result = repository.save(event);

        assertEquals(saved, result);
        verify(repository, times(1)).save(event);
    }

    @Test
    void findById_ShouldReturnEvent() {
        EventRepository repository = mock(EventRepository.class);
        Event event = mock(Event.class);

        when(repository.findById(1L)).thenReturn(Optional.of(event));

        Optional<Event> result = repository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(event, result.get());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotFound() {
        EventRepository repository = mock(EventRepository.class);

        when(repository.findById(999L)).thenReturn(Optional.empty());

        Optional<Event> result = repository.findById(999L);

        assertFalse(result.isPresent());
        verify(repository, times(1)).findById(999L);
    }

    @Test
    void deleteById_ShouldDeleteEvent() {
        EventRepository repository = mock(EventRepository.class);

        repository.deleteById(1L);

        verify(repository, times(1)).deleteById(1L);
    }
}