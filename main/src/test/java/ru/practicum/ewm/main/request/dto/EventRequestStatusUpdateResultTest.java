package ru.practicum.ewm.main.request.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.ewm.main.request.model.RequestStatus;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class EventRequestStatusUpdateResultTest {

    @Test
    void shouldCreateWithBuilderAllFields() {
        ParticipationRequestDto confirmed1 = ParticipationRequestDto.builder()
                .id(1L)
                .status(RequestStatus.CONFIRMED.name())
                .build();

        ParticipationRequestDto rejected1 = ParticipationRequestDto.builder()
                .id(2L)
                .status(RequestStatus.REJECTED.name())
                .build();

        EventRequestStatusUpdateResult result = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(List.of(confirmed1))
                .rejectedRequests(List.of(rejected1))
                .build();

        assertEquals(1, result.getConfirmedRequests().size());
        assertEquals(1, result.getRejectedRequests().size());
        assertEquals(RequestStatus.CONFIRMED.name(), result.getConfirmedRequests().get(0).getStatus());
    }

    @Test
    void shouldCreateWithEmptyLists() {
        EventRequestStatusUpdateResult result = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(List.of())
                .rejectedRequests(List.of())
                .build();

        assertTrue(result.getConfirmedRequests().isEmpty());
        assertTrue(result.getRejectedRequests().isEmpty());
    }

    @Test
    void shouldWorkEqualsAndHashCode() {
        ParticipationRequestDto dto1 = ParticipationRequestDto.builder().id(1L).build();

        EventRequestStatusUpdateResult result1 = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(List.of(dto1))
                .build();

        EventRequestStatusUpdateResult result2 = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(List.of(dto1))
                .build();

        EventRequestStatusUpdateResult result3 = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(List.of())
                .build();

        assertEquals(result1, result2);
        assertEquals(result1.hashCode(), result2.hashCode());
        assertNotEquals(result1, result3);
    }
}