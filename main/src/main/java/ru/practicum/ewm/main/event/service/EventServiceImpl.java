package ru.practicum.ewm.main.event.service;

import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.category.Category;
import ru.practicum.ewm.main.category.CategoryRepository;
import ru.practicum.ewm.main.event.EventRepository;
import ru.practicum.ewm.main.event.dto.*;
import ru.practicum.ewm.main.event.model.*;
import ru.practicum.ewm.main.exception.BadRequestException;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.location.LocationRepository;
import ru.practicum.ewm.main.location.dto.LocationMapper;
import ru.practicum.ewm.main.request.RequestRepository;
import ru.practicum.ewm.main.request.model.RequestStatus;
import ru.practicum.ewm.main.stats.ExternalStatsService;
import ru.practicum.ewm.main.user.User;
import ru.practicum.ewm.main.user.UserRepository;
import ru.practicum.ewm.main.util.Constants;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final ExternalStatsService statsService;


    @Override
    public List<EventFullDto> getEventsAdmin(List<Long> users,
                                             List<String> states,
                                             List<Long> categories,
                                             String rangeStart,
                                             String rangeEnd,
                                             Integer from,
                                             Integer size) {

        Pageable pageable = PageRequest.of(from / size, size);

        Specification<Event> spec = buildAdminSpecification(users, states, categories,
                rangeStart, rangeEnd);

        Page<Event> page = eventRepository.findAll(spec, pageable);

        return page.stream()
                .map(e -> EventMapper.toEventFullDto(
                        e,
                        getConfirmedRequests(e.getId()),
                        0L))
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateEventAdmin(Long eventId,
                                         UpdateEventAdminRequest dto) {

        Event event = getEventOrThrow(eventId);

        if (dto.getStateAction() != null) {

            if (dto.getStateAction() == AdminStateAction.PUBLISH_EVENT) {

                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Only pending events can be published");
                }

                if (dto.getEventDate() != null) {
                    LocalDateTime newDate = LocalDateTime.parse(
                            dto.getEventDate(), Constants.FORMATTER);

                    if (newDate.isBefore(LocalDateTime.now().plusHours(1))) {
                        throw new ConflictException("Event date too soon");
                    }
                }

                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }

            if (dto.getStateAction() == AdminStateAction.REJECT_EVENT) {

                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Cannot reject published event");
                }

                event.setState(EventState.CANCELED);
            }
        }

        updateEventFields(event, dto);

        return EventMapper.toEventFullDto(
                event,
                getConfirmedRequests(eventId),
                0L);
    }

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto dto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Category category = categoryRepository.findById(dto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        LocalDateTime eventDate =
                LocalDateTime.parse(dto.getEventDate(), Constants.FORMATTER);

        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Event date must be at least 2 hours later");
        }

        Event event = EventMapper.toEvent(dto);

        event.setInitiator(user);
        event.setCategory(category);
        event.setLocation(locationRepository.save(
                LocationMapper.toLocation(dto.getLocation())));
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());

        Event saved = eventRepository.save(event);

        return EventMapper.toEventFullDto(saved, 0L, 0L);
    }

    @Override
    public List<EventShortDto> getEventsByUser(Long userId,
                                               Integer from,
                                               Integer size) {

        Pageable pageable = PageRequest.of(from / size, size);

        return eventRepository.findAllByInitiatorId(userId, pageable)
                .stream()
                .map(e -> EventMapper.toEventShortDto(
                        e,
                        getConfirmedRequests(e.getId()),
                        0L))
                .toList();
    }

    @Override
    public EventFullDto getEventByIdByUser(Long userId, Long eventId) {

        Event event = eventRepository
                .findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        return EventMapper.toEventFullDto(
                event,
                getConfirmedRequests(eventId),
                0L);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId,
                                          Long eventId,
                                          UpdateEventUserRequest dto) {

        Event event = eventRepository
                .findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Cannot edit published event");
        }

        if (dto.getEventDate() != null) {
            LocalDateTime newDate = LocalDateTime.parse(
                    dto.getEventDate(), Constants.FORMATTER);

            if (newDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new BadRequestException("Event date must be at least 2 hours later");
            }
        }

        updateEventFields(event, dto);

        if (dto.getStateAction() != null) {

            if (dto.getStateAction() == UserStateAction.SEND_TO_REVIEW) {

                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Published event cannot be sent to review");
                }

                event.setState(EventState.PENDING);
            }

            if (dto.getStateAction() == UserStateAction.CANCEL_REVIEW) {

                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Only pending events can be canceled");
                }

                event.setState(EventState.CANCELED);
            }
        }

        return EventMapper.toEventFullDto(
                event,
                getConfirmedRequests(eventId),
                0L);
    }

    // ================= PUBLIC =================

    @Override
    public List<EventShortDto> getEventsPublic(String text,
                                               List<Long> categories,
                                               Boolean paid,
                                               String rangeStart,
                                               String rangeEnd,
                                               Boolean onlyAvailable,
                                               String sort,
                                               Integer from,
                                               Integer size) {

        Pageable pageable = PageRequest.of(0, size * 10);

        Specification<Event> spec =
                buildPublicSpecification(text, categories, paid,
                        rangeStart, rangeEnd);

        List<Event> events =
                eventRepository.findAll(spec, pageable).getContent();

        if (Boolean.TRUE.equals(onlyAvailable)) {
            events = events.stream()
                    .filter(event -> {
                        if (event.getParticipantLimit() == 0) {
                            return true;
                        }
                        long confirmed = getConfirmedRequests(event.getId());
                        return confirmed < event.getParticipantLimit();
                    })
                    .toList();
        }

        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .toList();

        Map<String, Long> views = statsService.getViews(uris);

        if ("VIEWS".equals(sort)) {
            events = events.stream()
                    .sorted((e1, e2) -> Long.compare(
                            views.getOrDefault("/events/" + e2.getId(), 0L),
                            views.getOrDefault("/events/" + e1.getId(), 0L)))
                    .toList();
        }

        int start = from;
        int end = Math.min(start + size, events.size());

        if (start > events.size()) {
            return List.of();
        }

        return events.subList(start, end)
                .stream()
                .map(e -> EventMapper.toEventShortDto(
                        e,
                        getConfirmedRequests(e.getId()),
                        views.getOrDefault("/events/" + e.getId(), 0L)))
                .toList();
    }

    @Override
    public EventFullDto getEventPublic(Long id) {

        Event event = getEventOrThrow(id);

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event not published");
        }

        Map<String, Long> views =
                statsService.getViews(List.of("/events/" + id));

        Long eventViews =
                views.getOrDefault("/events/" + id, 0L);

        return EventMapper.toEventFullDto(
                event,
                getConfirmedRequests(id),
                eventViews);
    }

    private Event getEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found"));
    }

    private Long getConfirmedRequests(Long eventId) {
        return requestRepository
                .countByEventIdAndStatus(eventId,
                        RequestStatus.CONFIRMED);
    }

    private Pageable buildPublicPageable(String sort,
                                         Integer from,
                                         Integer size) {

        if ("EVENT_DATE".equals(sort)) {
            return PageRequest.of(from / size, size,
                    Sort.by("eventDate").descending());
        }

        return PageRequest.of(from / size, size);
    }

    private void updateEventFields(Event event,
                                   Object dto) {

        if (dto instanceof UpdateEventAdminRequest admin) {

            if (admin.getAnnotation() != null)
                event.setAnnotation(admin.getAnnotation());

            if (admin.getDescription() != null)
                event.setDescription(admin.getDescription());

            if (admin.getEventDate() != null)
                event.setEventDate(LocalDateTime.parse(
                        admin.getEventDate(), Constants.FORMATTER));

            if (admin.getPaid() != null)
                event.setPaid(admin.getPaid());

            if (admin.getParticipantLimit() != null)
                event.setParticipantLimit(admin.getParticipantLimit());

            if (admin.getRequestModeration() != null)
                event.setRequestModeration(admin.getRequestModeration());

            if (admin.getTitle() != null)
                event.setTitle(admin.getTitle());

            if (admin.getCategory() != null) {
                Category category =
                        categoryRepository.findById(admin.getCategory())
                                .orElseThrow(() ->
                                        new NotFoundException("Category not found"));
                event.setCategory(category);
            }
        }

        if (dto instanceof UpdateEventUserRequest user) {

            if (user.getAnnotation() != null)
                event.setAnnotation(user.getAnnotation());

            if (user.getDescription() != null)
                event.setDescription(user.getDescription());

            if (user.getEventDate() != null)
                event.setEventDate(LocalDateTime.parse(
                        user.getEventDate(), Constants.FORMATTER));

            if (user.getPaid() != null)
                event.setPaid(user.getPaid());

            if (user.getParticipantLimit() != null)
                event.setParticipantLimit(user.getParticipantLimit());

            if (user.getRequestModeration() != null)
                event.setRequestModeration(user.getRequestModeration());

            if (user.getTitle() != null)
                event.setTitle(user.getTitle());

            if (user.getCategory() != null) {
                Category category =
                        categoryRepository.findById(user.getCategory())
                                .orElseThrow(() ->
                                        new NotFoundException("Category not found"));
                event.setCategory(category);
            }
        }
    }

    private Specification<Event> buildAdminSpecification(
            List<Long> users,
            List<String> states,
            List<Long> categories,
            String rangeStart,
            String rangeEnd) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (users != null)
                predicates.add(root.get("initiator").get("id").in(users));

            if (states != null)
                predicates.add(root.get("state").in(
                        states.stream()
                                .map(EventState::valueOf)
                                .toList()));

            if (categories != null)
                predicates.add(root.get("category").get("id").in(categories));

            if (rangeStart != null)
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("eventDate"),
                        LocalDateTime.parse(rangeStart,
                                Constants.FORMATTER)));

            if (rangeEnd != null)
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("eventDate"),
                        LocalDateTime.parse(rangeEnd,
                                Constants.FORMATTER)));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<Event> buildPublicSpecification(
            String text,
            List<Long> categories,
            Boolean paid,
            String rangeStart,
            String rangeEnd) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("state"),
                    EventState.PUBLISHED));

            if (text != null) {
                Predicate annotation = cb.like(
                        cb.lower(root.get("annotation")),
                        "%" + text.toLowerCase() + "%");

                Predicate description = cb.like(
                        cb.lower(root.get("description")),
                        "%" + text.toLowerCase() + "%");

                predicates.add(cb.or(annotation, description));
            }

            if (categories != null)
                predicates.add(root.get("category")
                        .get("id").in(categories));

            if (paid != null)
                predicates.add(cb.equal(root.get("paid"), paid));

            if (rangeStart != null)
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("eventDate"),
                        LocalDateTime.parse(rangeStart,
                                Constants.FORMATTER)));

            if (rangeEnd != null)
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("eventDate"),
                        LocalDateTime.parse(rangeEnd,
                                Constants.FORMATTER)));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}