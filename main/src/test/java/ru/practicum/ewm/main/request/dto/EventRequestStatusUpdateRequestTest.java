package ru.practicum.ewm.main.request.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.ewm.main.request.model.RequestStatus;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class EventRequestStatusUpdateRequestTest {

    @Test
    void shouldCreateWithNoArgsConstructorAndSetters() {
        EventRequestStatusUpdateRequest request = new EventRequestStatusUpdateRequest();

        List<Long> requestIds = List.of(1L, 2L, 3L);
        request.setRequestIds(requestIds);
        request.setStatus(RequestStatus.CONFIRMED);

        assertEquals(requestIds, request.getRequestIds());
        assertEquals(RequestStatus.CONFIRMED, request.getStatus());
    }

    @Test
    void shouldCreateWithEmptyRequestIds() {
        EventRequestStatusUpdateRequest request = new EventRequestStatusUpdateRequest();
        request.setRequestIds(List.of());
        request.setStatus(RequestStatus.REJECTED);

        assertTrue(request.getRequestIds().isEmpty());
        assertEquals(RequestStatus.REJECTED, request.getStatus());
    }

    @Test
    void shouldWorkEqualsAndHashCode() {
        EventRequestStatusUpdateRequest request1 = new EventRequestStatusUpdateRequest();
        request1.setRequestIds(List.of(1L, 2L));
        request1.setStatus(RequestStatus.PENDING);

        EventRequestStatusUpdateRequest request2 = new EventRequestStatusUpdateRequest();
        request2.setRequestIds(List.of(1L, 2L));
        request2.setStatus(RequestStatus.PENDING);

        EventRequestStatusUpdateRequest request3 = new EventRequestStatusUpdateRequest();
        request3.setRequestIds(List.of(3L, 4L));
        request3.setStatus(RequestStatus.CONFIRMED);

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotEquals(request1, request3);
    }
}
