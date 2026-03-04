package ru.practicum.ewm.main.request.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.main.request.model.ParticipationRequest;
import ru.practicum.ewm.main.util.Constants;

@UtilityClass
public class RequestMapper {
    public ParticipationRequestDto toParticipationRequestDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated().format(Constants.FORMATTER))
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus().name())
                .build();
    }
}