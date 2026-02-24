package ru.practicum.ewm.main.request;

import org.junit.jupiter.api.Test;
import ru.practicum.ewm.main.request.model.ParticipationRequest;
import ru.practicum.ewm.main.request.model.RequestStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestRepositoryTest {

    @Test
    void findAllByRequesterId_ShouldReturnRequests() {
        RequestRepository repository = mock(RequestRepository.class);
        List<ParticipationRequest> expected = List.of(mock(ParticipationRequest.class));

        when(repository.findAllByRequesterId(1L)).thenReturn(expected);

        List<ParticipationRequest> result = repository.findAllByRequesterId(1L);

        assertEquals(1, result.size());
        verify(repository, times(1)).findAllByRequesterId(1L);
    }

    @Test
    void findAllByEventId_ShouldReturnRequests() {
        RequestRepository repository = mock(RequestRepository.class);
        List<ParticipationRequest> expected = List.of(mock(ParticipationRequest.class));

        when(repository.findAllByEventId(1L)).thenReturn(expected);

        List<ParticipationRequest> result = repository.findAllByEventId(1L);

        assertEquals(1, result.size());
        verify(repository, times(1)).findAllByEventId(1L);
    }

    @Test
    void existsByEventIdAndRequesterId_ShouldReturnTrue_WhenExists() {
        RequestRepository repository = mock(RequestRepository.class);

        when(repository.existsByEventIdAndRequesterId(1L, 1L)).thenReturn(true);

        boolean result = repository.existsByEventIdAndRequesterId(1L, 1L);

        assertTrue(result);
        verify(repository, times(1)).existsByEventIdAndRequesterId(1L, 1L);
    }

    @Test
    void countByEventIdAndStatus_ShouldReturnCount() {
        RequestRepository repository = mock(RequestRepository.class);

        when(repository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(5L);

        Long result = repository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED);

        assertEquals(5L, result);
        verify(repository, times(1)).countByEventIdAndStatus(1L, RequestStatus.CONFIRMED);
    }

    @Test
    void findAllByIdIn_ShouldReturnRequests() {
        RequestRepository repository = mock(RequestRepository.class);
        List<ParticipationRequest> expected = List.of(mock(ParticipationRequest.class));

        when(repository.findAllByIdIn(List.of(1L, 2L))).thenReturn(expected);

        List<ParticipationRequest> result = repository.findAllByIdIn(List.of(1L, 2L));

        assertEquals(1, result.size());
        verify(repository, times(1)).findAllByIdIn(List.of(1L, 2L));
    }
}