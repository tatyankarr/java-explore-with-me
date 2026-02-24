package ru.practicum.ewm.main.event.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.ewm.main.category.Category;
import ru.practicum.ewm.main.event.EventRepository;
import ru.practicum.ewm.main.event.dto.EventFullDto;
import ru.practicum.ewm.main.event.dto.EventShortDto;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.event.model.EventState;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.location.Location;
import ru.practicum.ewm.main.request.RequestRepository;
import ru.practicum.ewm.main.request.model.RequestStatus;
import ru.practicum.ewm.main.stats.ExternalStatsService;
import ru.practicum.ewm.main.user.User;
import ru.practicum.ewm.main.util.Constants;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplPublicTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private ExternalStatsService statsService;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event event1;
    private Event event2;
    private Event event3;
    private User initiator;
    private Category category;
    private Location location;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        initiator = User.builder()
                .id(1L)
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build();

        category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        location = Location.builder()
                .id(1L)
                .lat(55.75f)
                .lon(37.62f)
                .build();

        event1 = Event.builder()
                .id(1L)
                .annotation("Рок концерт в Москве")
                .description("Лучший рок концерт года")
                .eventDate(now.plusDays(10))
                .category(category)
                .initiator(initiator)
                .location(location)
                .paid(true)
                .participantLimit(100)
                .requestModeration(true)
                .title("Рок фестиваль")
                .state(EventState.PUBLISHED)
                .createdOn(now)
                .build();

        event2 = Event.builder()
                .id(2L)
                .annotation("Классическая музыка в филармонии")
                .description("Концерт симфонического оркестра")
                .eventDate(now.plusDays(20))
                .category(category)
                .initiator(initiator)
                .location(location)
                .paid(false)
                .participantLimit(50)
                .requestModeration(true)
                .title("Вечер классики")
                .state(EventState.PUBLISHED)
                .createdOn(now)
                .build();

        event3 = Event.builder()
                .id(3L)
                .annotation("Джазовый вечер")
                .description("Импровизации лучших джазменов")
                .eventDate(now.plusDays(5))
                .category(category)
                .initiator(initiator)
                .location(location)
                .paid(true)
                .participantLimit(0)
                .requestModeration(false)
                .title("Jazz Night")
                .state(EventState.PUBLISHED)
                .createdOn(now)
                .build();
    }

    @Test
    void getEventsPublic_ShouldReturnEvents_WhenNoFilters() {
        List<Event> events = List.of(event1, event2, event3);
        when(eventRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(events));
        when(requestRepository.countByEventIdAndStatus(anyLong(), eq(RequestStatus.CONFIRMED)))
                .thenReturn(0L);
        when(statsService.getViews(anyList())).thenReturn(Map.of(
                "/events/1", 10L,
                "/events/2", 5L,
                "/events/3", 15L
        ));

        List<EventShortDto> result = eventService.getEventsPublic(
                null, null, null, null, null, false, null, 0, 10);

        assertNotNull(result);
        assertEquals(3, result.size());
        verify(eventRepository, times(1)).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void getEventsPublic_ShouldFilterByText() {
        List<Event> events = List.of(event1);
        when(eventRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(events));
        when(requestRepository.countByEventIdAndStatus(anyLong(), eq(RequestStatus.CONFIRMED)))
                .thenReturn(0L);
        when(statsService.getViews(anyList())).thenReturn(Map.of("/events/1", 10L));

        List<EventShortDto> result = eventService.getEventsPublic(
                "рок", null, null, null, null, false, null, 0, 10);

        assertEquals(1, result.size());
        assertEquals("Рок фестиваль", result.get(0).getTitle());
    }

    @Test
    void getEventsPublic_ShouldFilterByCategories() {
        List<Event> events = List.of(event1, event2);
        when(eventRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(events));
        when(requestRepository.countByEventIdAndStatus(anyLong(), eq(RequestStatus.CONFIRMED)))
                .thenReturn(0L);
        when(statsService.getViews(anyList())).thenReturn(Map.of(
                "/events/1", 10L,
                "/events/2", 5L
        ));

        List<EventShortDto> result = eventService.getEventsPublic(
                null, List.of(1L), null, null, null, false, null, 0, 10);

        assertEquals(2, result.size());
    }

    @Test
    void getEventsPublic_ShouldFilterByPaid() {
        List<Event> events = List.of(event1, event3);
        when(eventRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(events));
        when(requestRepository.countByEventIdAndStatus(anyLong(), eq(RequestStatus.CONFIRMED)))
                .thenReturn(0L);
        when(statsService.getViews(anyList())).thenReturn(Map.of(
                "/events/1", 10L,
                "/events/3", 15L
        ));

        List<EventShortDto> result = eventService.getEventsPublic(
                null, null, true, null, null, false, null, 0, 10);

        assertEquals(2, result.size());
        assertTrue(result.get(0).getPaid());
        assertTrue(result.get(1).getPaid());
    }

    @Test
    void getEventsPublic_ShouldFilterByDateRange() {
        List<Event> events = List.of(event3, event1);
        String rangeStart = now.plusDays(4).format(Constants.FORMATTER);
        String rangeEnd = now.plusDays(15).format(Constants.FORMATTER);

        when(eventRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(events));
        when(requestRepository.countByEventIdAndStatus(anyLong(), eq(RequestStatus.CONFIRMED)))
                .thenReturn(0L);
        when(statsService.getViews(anyList())).thenReturn(Map.of(
                "/events/1", 10L,
                "/events/3", 15L
        ));

        List<EventShortDto> result = eventService.getEventsPublic(
                null, null, null, rangeStart, rangeEnd, false, null, 0, 10);

        assertEquals(2, result.size());
    }

    @Test
    void getEventsPublic_ShouldFilterOnlyAvailable() {
        when(requestRepository.countByEventIdAndStatus(eq(2L), eq(RequestStatus.CONFIRMED)))
                .thenReturn(50L);
        when(requestRepository.countByEventIdAndStatus(eq(1L), eq(RequestStatus.CONFIRMED)))
                .thenReturn(50L);
        when(requestRepository.countByEventIdAndStatus(eq(3L), eq(RequestStatus.CONFIRMED)))
                .thenReturn(0L);

        List<Event> allEvents = List.of(event1, event2, event3);
        when(eventRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(allEvents));
        when(statsService.getViews(anyList())).thenReturn(Map.of(
                "/events/1", 10L,
                "/events/2", 5L,
                "/events/3", 15L
        ));

        List<EventShortDto> result = eventService.getEventsPublic(
                null, null, null, null, null, true, null, 0, 10);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(e -> e.getId().equals(1L)));
        assertTrue(result.stream().anyMatch(e -> e.getId().equals(3L)));
        assertFalse(result.stream().anyMatch(e -> e.getId().equals(2L)));
    }

    @Test
    void getEventsPublic_ShouldSortByEventDate() {
        List<Event> eventsSortedByDate = List.of(event2, event1, event3);

        when(eventRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(eventsSortedByDate));

        when(requestRepository.countByEventIdAndStatus(anyLong(), eq(RequestStatus.CONFIRMED)))
                .thenReturn(0L);
        when(statsService.getViews(anyList())).thenReturn(Map.of(
                "/events/1", 10L,
                "/events/2", 5L,
                "/events/3", 15L
        ));

        List<EventShortDto> result = eventService.getEventsPublic(
                null, null, null, null, null, false, "EVENT_DATE", 0, 10);

        assertEquals(3, result.size());

        LocalDateTime date0 = LocalDateTime.parse(result.get(0).getEventDate(), Constants.FORMATTER);
        LocalDateTime date1 = LocalDateTime.parse(result.get(1).getEventDate(), Constants.FORMATTER);
        LocalDateTime date2 = LocalDateTime.parse(result.get(2).getEventDate(), Constants.FORMATTER);

        assertTrue(date0.isAfter(date1) || date0.isEqual(date1));
        assertTrue(date1.isAfter(date2) || date1.isEqual(date2));
    }

    @Test
    void getEventsPublic_ShouldSortByViews() {
        List<Event> events = List.of(event1, event2, event3);
        when(eventRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(events));
        when(requestRepository.countByEventIdAndStatus(anyLong(), eq(RequestStatus.CONFIRMED)))
                .thenReturn(0L);
        when(statsService.getViews(anyList())).thenReturn(Map.of(
                "/events/1", 10L,
                "/events/2", 5L,
                "/events/3", 15L
        ));

        List<EventShortDto> result = eventService.getEventsPublic(
                null, null, null, null, null, false, "VIEWS", 0, 10);

        assertEquals(3, result.size());
        assertEquals(15L, result.get(0).getViews());
        assertEquals(10L, result.get(1).getViews());
        assertEquals(5L, result.get(2).getViews());
    }

    @Test
    void getEventsPublic_ShouldApplyPagination() {
        List<Event> events = List.of(event1, event2, event3);
        when(eventRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(events));
        when(requestRepository.countByEventIdAndStatus(anyLong(), eq(RequestStatus.CONFIRMED)))
                .thenReturn(0L);
        when(statsService.getViews(anyList())).thenReturn(Map.of(
                "/events/1", 10L,
                "/events/2", 5L,
                "/events/3", 15L
        ));

        List<EventShortDto> page1 = eventService.getEventsPublic(
                null, null, null, null, null, false, null, 0, 2);
        assertEquals(2, page1.size());

        List<EventShortDto> page2 = eventService.getEventsPublic(
                null, null, null, null, null, false, null, 2, 2);
        assertEquals(1, page2.size());

        List<EventShortDto> page3 = eventService.getEventsPublic(
                null, null, null, null, null, false, null, 10, 2);
        assertEquals(0, page3.size());
    }

    @Test
    void getEventsPublic_ShouldReturnEmptyList_WhenNoEvents() {
        when(eventRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        List<EventShortDto> result = eventService.getEventsPublic(
                null, null, null, null, null, false, null, 0, 10);

        assertTrue(result.isEmpty());
    }

    @Test
    void getEventsPublic_ShouldHandleNullRangeStartAndEnd() {
        List<Event> events = List.of(event1, event2, event3);
        when(eventRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(events));
        when(requestRepository.countByEventIdAndStatus(anyLong(), eq(RequestStatus.CONFIRMED)))
                .thenReturn(0L);
        when(statsService.getViews(anyList())).thenReturn(Map.of());

        List<EventShortDto> result = eventService.getEventsPublic(
                null, null, null, null, null, false, null, 0, 10);

        assertEquals(3, result.size());
    }

    @Test
    void getEventPublic_ShouldReturnEvent_WhenPublished() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event1));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(5L);
        when(statsService.getViews(List.of("/events/1"))).thenReturn(Map.of("/events/1", 100L));

        EventFullDto result = eventService.getEventPublic(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(EventState.PUBLISHED.name(), result.getState());
        assertEquals(5L, result.getConfirmedRequests());
        assertEquals(100L, result.getViews());
    }

    @Test
    void getEventPublic_ShouldThrowNotFoundException_WhenEventNotFound() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> eventService.getEventPublic(999L));

        assertEquals("Event not found", exception.getMessage());
    }

    @Test
    void getEventPublic_ShouldThrowNotFoundException_WhenEventNotPublished() {
        event1.setState(EventState.PENDING);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event1));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> eventService.getEventPublic(1L));

        assertEquals("Event not published", exception.getMessage());
    }

    @Test
    void getEventPublic_ShouldReturnEvent_WhenEventIsPublishedAndHasNoViews() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event1));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(5L);
        when(statsService.getViews(List.of("/events/1"))).thenReturn(Map.of());

        EventFullDto result = eventService.getEventPublic(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(0L, result.getViews());
    }

    @Test
    void getEventPublic_ShouldReturnEvent_WhenEventHasNoConfirmedRequests() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event1));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(0L);
        when(statsService.getViews(List.of("/events/1"))).thenReturn(Map.of("/events/1", 50L));

        EventFullDto result = eventService.getEventPublic(1L);

        assertNotNull(result);
        assertEquals(0L, result.getConfirmedRequests());
        assertEquals(50L, result.getViews());
    }

    @Test
    void getEventPublic_ShouldCallStatsServiceWithCorrectUri() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event1));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(0L);
        when(statsService.getViews(List.of("/events/1"))).thenReturn(Map.of("/events/1", 10L));

        eventService.getEventPublic(1L);

        verify(statsService, times(1)).getViews(List.of("/events/1"));
    }
}