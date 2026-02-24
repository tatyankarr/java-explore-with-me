package ru.practicum.ewm.main.request.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.ewm.main.request.model.RequestStatus;
import ru.practicum.ewm.main.util.Constants;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ParticipationRequestDtoTest {

    @Test
    void shouldCreateWithBuilderAllFields() {
        LocalDateTime now = LocalDateTime.now();
        String created = now.format(Constants.FORMATTER);

        ParticipationRequestDto dto = ParticipationRequestDto.builder()
                .id(1L)
                .created(created)
                .event(10L)
                .requester(20L)
                .status(RequestStatus.PENDING.name())
                .build();

        assertEquals(1L, dto.getId());
        assertEquals(created, dto.getCreated());
        assertEquals(10L, dto.getEvent());
        assertEquals(20L, dto.getRequester());
        assertEquals(RequestStatus.PENDING.name(), dto.getStatus());
    }

    @Test
    void shouldCreateWithAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        String created = now.format(Constants.FORMATTER);

        ParticipationRequestDto dto = new ParticipationRequestDto(
                1L,
                created,
                10L,
                20L,
                RequestStatus.CONFIRMED.name()
        );

        assertEquals(1L, dto.getId());
        assertEquals(created, dto.getCreated());
        assertEquals(10L, dto.getEvent());
        assertEquals(20L, dto.getRequester());
    }

    @Test
    void shouldWorkEqualsAndHashCode() {
        String created = LocalDateTime.now().format(Constants.FORMATTER);

        ParticipationRequestDto dto1 = ParticipationRequestDto.builder()
                .id(1L)
                .created(created)
                .event(10L)
                .requester(20L)
                .status(RequestStatus.PENDING.name())
                .build();

        ParticipationRequestDto dto2 = ParticipationRequestDto.builder()
                .id(1L)
                .created(created)
                .event(10L)
                .requester(20L)
                .status(RequestStatus.PENDING.name())
                .build();

        ParticipationRequestDto dto3 = ParticipationRequestDto.builder()
                .id(2L)
                .build();

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
    }
}