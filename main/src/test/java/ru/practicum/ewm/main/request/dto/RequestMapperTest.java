package ru.practicum.ewm.main.request.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.request.model.ParticipationRequest;
import ru.practicum.ewm.main.request.model.RequestStatus;
import ru.practicum.ewm.main.user.User;
import ru.practicum.ewm.main.util.Constants;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RequestMapperTest {

    private ParticipationRequest request;
    private User requester;
    private Event event;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        requester = User.builder()
                .id(10L)
                .build();

        event = Event.builder()
                .id(20L)
                .build();

        request = ParticipationRequest.builder()
                .id(1L)
                .requester(requester)
                .event(event)
                .created(now)
                .status(RequestStatus.PENDING)
                .build();
    }

    @Test
    void shouldConvertParticipationRequestToDto() {
        ParticipationRequestDto dto = RequestMapper.toParticipationRequestDto(request);

        assertEquals(request.getId(), dto.getId());
        assertEquals(request.getRequester().getId(), dto.getRequester());
        assertEquals(request.getEvent().getId(), dto.getEvent());
        assertEquals(request.getCreated().format(Constants.FORMATTER), dto.getCreated());
        assertEquals(request.getStatus().name(), dto.getStatus());
    }

    @Test
    void shouldConvertRequestWithDifferentStatuses() {
        request.setStatus(RequestStatus.CONFIRMED);
        ParticipationRequestDto dto1 = RequestMapper.toParticipationRequestDto(request);
        assertEquals(RequestStatus.CONFIRMED.name(), dto1.getStatus());

        request.setStatus(RequestStatus.REJECTED);
        ParticipationRequestDto dto2 = RequestMapper.toParticipationRequestDto(request);
        assertEquals(RequestStatus.REJECTED.name(), dto2.getStatus());
    }

    @Test
    void shouldCreateNewDtoObjectEachCall() {
        ParticipationRequestDto dto1 = RequestMapper.toParticipationRequestDto(request);
        ParticipationRequestDto dto2 = RequestMapper.toParticipationRequestDto(request);

        assertNotSame(dto1, dto2);
        assertEquals(dto1, dto2);
    }
}