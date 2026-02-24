package ru.practicum.ewm.main.event.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.main.category.dto.CategoryMapper;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.location.dto.LocationMapper;
import ru.practicum.ewm.main.user.dto.UserMapper;
import ru.practicum.ewm.main.util.Constants;

import java.time.LocalDateTime;

@UtilityClass
public class EventMapper {

    public Event toEvent(NewEventDto dto) {
        return Event.builder()
                .annotation(dto.getAnnotation())
                .description(dto.getDescription())
                .eventDate(LocalDateTime.parse(dto.getEventDate(), Constants.FORMATTER))
                .paid(dto.getPaid())
                .participantLimit(dto.getParticipantLimit())
                .requestModeration(dto.getRequestModeration())
                .title(dto.getTitle())
                .build();
    }

    public EventFullDto toEventFullDto(Event event, Long confirmedRequests, Long views) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .createdOn(event.getCreatedOn().format(Constants.FORMATTER))
                .description(event.getDescription())
                .eventDate(event.getEventDate().format(Constants.FORMATTER))
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .location(LocationMapper.toLocationDto(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn() != null ?
                        event.getPublishedOn().format(Constants.FORMATTER) : null)
                .requestModeration(event.getRequestModeration())
                .state(event.getState().name())
                .title(event.getTitle())
                .views(views)
                .build();
    }

    public EventShortDto toEventShortDto(Event event, Long confirmedRequests, Long views) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .eventDate(event.getEventDate().format(Constants.FORMATTER))
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(views)
                .build();
    }
}
