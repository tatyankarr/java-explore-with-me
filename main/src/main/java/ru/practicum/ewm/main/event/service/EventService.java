package ru.practicum.ewm.main.event.service;

import ru.practicum.ewm.main.event.dto.*;

import java.util.List;

public interface EventService {

    List<EventFullDto> getEventsAdmin(List<Long> users,
                                      List<String> states,
                                      List<Long> categories,
                                      String rangeStart,
                                      String rangeEnd,
                                      Integer from,
                                      Integer size);

    EventFullDto updateEventAdmin(Long eventId,
                                  UpdateEventAdminRequest updateRequest);

    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getEventsByUser(Long userId,
                                        Integer from,
                                        Integer size);

    EventFullDto getEventByIdByUser(Long userId, Long eventId);

    EventFullDto updateEventByUser(Long userId,
                                   Long eventId,
                                   UpdateEventUserRequest updateRequest);

    List<EventShortDto> getEventsPublic(String text,
                                        List<Long> categories,
                                        Boolean paid,
                                        String rangeStart,
                                        String rangeEnd,
                                        Boolean onlyAvailable,
                                        String sort,
                                        Integer from,
                                        Integer size);

    EventFullDto getEventPublic(Long id);
}