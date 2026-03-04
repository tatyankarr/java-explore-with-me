package ru.practicum.ewm.main.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.ewm.main.event.EventRepository;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.event.model.EventState;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.main.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.main.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.main.request.model.ParticipationRequest;
import ru.practicum.ewm.main.request.model.RequestStatus;
import ru.practicum.ewm.main.request.service.RequestServiceImpl;
import ru.practicum.ewm.main.user.User;
import ru.practicum.ewm.main.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RequestServiceImpl requestService;

    private User user;
    private User initiator;
    private Event event;
    private ParticipationRequest request;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        user = User.builder()
                .id(2L)
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build();

        initiator = User.builder()
                .id(1L)
                .name("Петр Петров")
                .email("petr@example.com")
                .build();

        event = Event.builder()
                .id(1L)
                .title("Событие")
                .initiator(initiator)
                .state(EventState.PUBLISHED)
                .participantLimit(10)
                .requestModeration(true)
                .build();

        request = ParticipationRequest.builder()
                .id(1L)
                .event(event)
                .requester(user)
                .status(RequestStatus.PENDING)
                .created(now)
                .build();
    }

    @Test
    void getUserRequests_ShouldReturnRequests() {
        when(requestRepository.findAllByRequesterId(2L)).thenReturn(List.of(request));

        List<ParticipationRequestDto> result = requestService.getUserRequests(2L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(requestRepository, times(1)).findAllByRequesterId(2L);
    }

    @Test
    void getUserRequests_ShouldReturnEmptyList_WhenNoRequests() {
        when(requestRepository.findAllByRequesterId(2L)).thenReturn(List.of());

        List<ParticipationRequestDto> result = requestService.getUserRequests(2L);

        assertTrue(result.isEmpty());
    }

    @Test
    void addParticipationRequest_ShouldCreateConfirmedRequest_WhenModerationFalse() {
        event.setRequestModeration(false);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(requestRepository.existsByEventIdAndRequesterId(1L, 2L)).thenReturn(false);
        when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(request);

        ParticipationRequestDto result = requestService.addParticipationRequest(2L, 1L);

        assertNotNull(result);
        assertEquals(RequestStatus.PENDING.name(), result.getStatus());
    }

    @Test
    void addParticipationRequest_ShouldThrowNotFoundException_WhenEventNotFound() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> requestService.addParticipationRequest(2L, 999L));

        assertEquals("Event not found", exception.getMessage());
    }

    @Test
    void addParticipationRequest_ShouldThrowNotFoundException_WhenUserNotFound() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> requestService.addParticipationRequest(999L, 1L));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void addParticipationRequest_ShouldThrowConflictException_WhenEventNotPublished() {
        event.setState(EventState.PENDING);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> requestService.addParticipationRequest(2L, 1L));

        assertEquals("Cannot request unpublished event", exception.getMessage());
    }

    @Test
    void addParticipationRequest_ShouldThrowConflictException_WhenRequesterIsInitiator() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.of(initiator));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> requestService.addParticipationRequest(1L, 1L));

        assertEquals("Cannot request participation in your own event", exception.getMessage());
    }

    @Test
    void addParticipationRequest_ShouldThrowConflictException_WhenRequestAlreadyExists() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(requestRepository.existsByEventIdAndRequesterId(1L, 2L)).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> requestService.addParticipationRequest(2L, 1L));

        assertEquals("Request already exists", exception.getMessage());
    }

    @Test
    void addParticipationRequest_ShouldThrowConflictException_WhenParticipantLimitReached() {
        event.setRequestModeration(false);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(requestRepository.existsByEventIdAndRequesterId(1L, 2L)).thenReturn(false);
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(10L);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> requestService.addParticipationRequest(2L, 1L));

        assertEquals("Participant limit reached", exception.getMessage());
        verify(requestRepository, never()).save(any(ParticipationRequest.class));
    }

    @Test
    void addParticipationRequest_ShouldAllowRequest_WhenParticipantLimitIsZero() {
        event.setParticipantLimit(0);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(requestRepository.existsByEventIdAndRequesterId(1L, 2L)).thenReturn(false);
        when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(request);

        ParticipationRequestDto result = requestService.addParticipationRequest(2L, 1L);

        assertNotNull(result);
        verify(requestRepository, times(1)).save(any(ParticipationRequest.class));
    }

    @Test
    void cancelRequest_ShouldCancelRequest_WhenValid() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        ParticipationRequestDto result = requestService.cancelRequest(2L, 1L);

        assertNotNull(result);
        assertEquals(RequestStatus.CANCELED.name(), result.getStatus());
    }

    @Test
    void cancelRequest_ShouldThrowNotFoundException_WhenRequestNotFound() {
        when(requestRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> requestService.cancelRequest(2L, 999L));

        assertEquals("Request not found", exception.getMessage());
    }

    @Test
    void cancelRequest_ShouldThrowConflictException_WhenUserNotRequester() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> requestService.cancelRequest(1L, 1L));

        assertEquals("Only requester can cancel request", exception.getMessage());
    }

    @Test
    void getEventParticipants_ShouldReturnRequests_WhenValid() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.findAllByEventId(1L)).thenReturn(List.of(request));

        List<ParticipationRequestDto> result = requestService.getEventParticipants(1L, 1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(requestRepository, times(1)).findAllByEventId(1L);
    }

    @Test
    void getEventParticipants_ShouldThrowNotFoundException_WhenEventNotFound() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> requestService.getEventParticipants(1L, 999L));

        assertEquals("Event not found", exception.getMessage());
    }

    @Test
    void getEventParticipants_ShouldThrowConflictException_WhenUserNotInitiator() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> requestService.getEventParticipants(2L, 1L));

        assertEquals("Only initiator can view requests", exception.getMessage());
    }

    @Test
    void changeRequestStatus_ShouldConfirmRequests_WhenValid() {
        ParticipationRequest request2 = ParticipationRequest.builder()
                .id(2L)
                .event(event)
                .requester(User.builder().id(3L).build())
                .status(RequestStatus.PENDING)
                .created(now.plusMinutes(1))
                .build();

        List<ParticipationRequest> requests = new ArrayList<>(List.of(request, request2));

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.findAllByIdIn(List.of(1L, 2L))).thenReturn(requests);
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(0L);

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(1L, 2L));
        updateRequest.setStatus(RequestStatus.CONFIRMED);

        EventRequestStatusUpdateResult result = requestService.changeRequestStatus(1L, 1L, updateRequest);

        assertEquals(2, result.getConfirmedRequests().size());
        assertEquals(0, result.getRejectedRequests().size());
        verify(requestRepository, times(1)).saveAll(anyList());
    }

    @Test
    void changeRequestStatus_ShouldRejectAllRequests_WhenStatusRejected() {
        ParticipationRequest request2 = ParticipationRequest.builder()
                .id(2L)
                .event(event)
                .requester(User.builder().id(3L).build())
                .status(RequestStatus.PENDING)
                .created(now)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.findAllByIdIn(List.of(1L, 2L))).thenReturn(List.of(request, request2));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(0L);

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(1L, 2L));
        updateRequest.setStatus(RequestStatus.REJECTED);

        EventRequestStatusUpdateResult result = requestService.changeRequestStatus(1L, 1L, updateRequest);

        assertEquals(0, result.getConfirmedRequests().size());
        assertEquals(2, result.getRejectedRequests().size());
    }

    @Test
    void changeRequestStatus_ShouldThrowException_WhenRequestNotPending() {
        request.setStatus(RequestStatus.CONFIRMED);
        ParticipationRequest request2 = ParticipationRequest.builder()
                .id(2L)
                .event(event)
                .requester(User.builder().id(3L).build())
                .status(RequestStatus.PENDING)
                .created(now.plusMinutes(1))
                .build();

        List<ParticipationRequest> requests = new ArrayList<>(List.of(request, request2));

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.findAllByIdIn(List.of(1L, 2L))).thenReturn(requests);

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(1L, 2L));
        updateRequest.setStatus(RequestStatus.CONFIRMED);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> requestService.changeRequestStatus(1L, 1L, updateRequest));

        assertEquals("Request must be in PENDING state", exception.getMessage());
    }

    @Test
    void changeRequestStatus_ShouldThrowNotFoundException_WhenEventNotFound() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> requestService.changeRequestStatus(1L, 999L, updateRequest));

        assertEquals("Event not found", exception.getMessage());
    }

    @Test
    void changeRequestStatus_ShouldThrowConflictException_WhenUserNotInitiator() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();

        ConflictException exception = assertThrows(ConflictException.class,
                () -> requestService.changeRequestStatus(2L, 1L, updateRequest));

        assertEquals("Only initiator can update requests", exception.getMessage());
    }

    @Test
    void changeRequestStatus_ShouldHandleEmptyRequestList() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.findAllByIdIn(List.of())).thenReturn(new ArrayList<>());

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of());
        updateRequest.setStatus(RequestStatus.CONFIRMED);

        EventRequestStatusUpdateResult result = requestService.changeRequestStatus(1L, 1L, updateRequest);

        assertEquals(0, result.getConfirmedRequests().size());
        assertEquals(0, result.getRejectedRequests().size());
        verify(requestRepository, times(1)).saveAll(anyList());
    }

    @Test
    void changeRequestStatus_ShouldPartiallyConfirm_WhenLimitReachedDuringProcessing() {
        ParticipationRequest request2 = ParticipationRequest.builder()
                .id(2L)
                .event(event)
                .requester(User.builder().id(3L).build())
                .status(RequestStatus.PENDING)
                .created(now.plusMinutes(1))
                .build();

        ParticipationRequest request3 = ParticipationRequest.builder()
                .id(3L)
                .event(event)
                .requester(User.builder().id(4L).build())
                .status(RequestStatus.PENDING)
                .created(now.plusMinutes(2))
                .build();

        List<ParticipationRequest> requests = new ArrayList<>(List.of(request, request2, request3));

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.findAllByIdIn(List.of(1L, 2L, 3L)))
                .thenReturn(requests);
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(8L);

        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(1L, 2L, 3L));
        updateRequest.setStatus(RequestStatus.CONFIRMED);

        EventRequestStatusUpdateResult result = requestService.changeRequestStatus(1L, 1L, updateRequest);

        assertEquals(2, result.getConfirmedRequests().size());
        assertEquals(1, result.getRejectedRequests().size());
    }
}