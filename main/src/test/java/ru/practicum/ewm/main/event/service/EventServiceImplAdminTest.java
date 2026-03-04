package ru.practicum.ewm.main.event.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.ewm.main.category.Category;
import ru.practicum.ewm.main.category.CategoryRepository;
import ru.practicum.ewm.main.event.EventRepository;
import ru.practicum.ewm.main.event.dto.EventFullDto;
import ru.practicum.ewm.main.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.main.event.model.AdminStateAction;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.event.model.EventState;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.location.Location;
import ru.practicum.ewm.main.location.LocationRepository;
import ru.practicum.ewm.main.request.RequestRepository;
import ru.practicum.ewm.main.request.model.RequestStatus;
import ru.practicum.ewm.main.stats.ExternalStatsService;
import ru.practicum.ewm.main.user.User;
import ru.practicum.ewm.main.user.UserRepository;
import ru.practicum.ewm.main.util.Constants;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplAdminTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private ExternalStatsService statsService;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event event;
    private Category category;
    private User initiator;
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

        event = Event.builder()
                .id(1L)
                .annotation("Аннотация события")
                .description("Описание события")
                .eventDate(now.plusDays(10))
                .category(category)
                .initiator(initiator)
                .location(location)
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .title("Заголовок события")
                .state(EventState.PENDING)
                .createdOn(now)
                .publishedOn(null)
                .build();
    }

    @Test
    void getEventsAdmin_ShouldReturnEvents_WhenAllParametersProvided() {
        List<Event> events = List.of(event);
        Page<Event> page = new PageImpl<>(events);

        when(eventRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(page);
        when(requestRepository.countByEventIdAndStatus(anyLong(), eq(RequestStatus.CONFIRMED)))
                .thenReturn(5L);

        List<EventFullDto> result = eventService.getEventsAdmin(
                List.of(1L),
                List.of("PENDING"),
                List.of(1L),
                now.format(Constants.FORMATTER),
                now.plusDays(20).format(Constants.FORMATTER),
                0,
                10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(5L, result.get(0).getConfirmedRequests());
        verify(eventRepository, times(1)).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void getEventsAdmin_ShouldReturnEvents_WhenOnlyPaginationProvided() {
        List<Event> events = List.of(event);
        Page<Event> page = new PageImpl<>(events);

        when(eventRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(page);
        when(requestRepository.countByEventIdAndStatus(anyLong(), eq(RequestStatus.CONFIRMED)))
                .thenReturn(0L);

        List<EventFullDto> result = eventService.getEventsAdmin(
                null, null, null, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(eventRepository, times(1)).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void getEventsAdmin_ShouldReturnEmptyList_WhenNoEvents() {
        Page<Event> emptyPage = new PageImpl<>(List.of());

        when(eventRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(emptyPage);

        List<EventFullDto> result = eventService.getEventsAdmin(
                null, null, null, null, null, 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getEventsAdmin_ShouldCalculatePageCorrectly() {
        Page<Event> emptyPage = new PageImpl<>(List.of());

        when(eventRepository.findAll(any(Specification.class), eq(PageRequest.of(2, 10))))
                .thenReturn(emptyPage);

        eventService.getEventsAdmin(null, null, null, null, null, 20, 10);

        verify(eventRepository, times(1)).findAll(any(Specification.class), eq(PageRequest.of(2, 10)));
    }

    @Test
    void getEventsAdmin_ShouldCallRepositoryWithCorrectSpecification_WhenAllFiltersProvided() {
        Page<Event> emptyPage = new PageImpl<>(List.of());
        when(eventRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(emptyPage);

        String rangeStart = now.format(Constants.FORMATTER);
        String rangeEnd = now.plusDays(30).format(Constants.FORMATTER);

        eventService.getEventsAdmin(
                List.of(1L, 2L),
                List.of("PENDING", "PUBLISHED"),
                List.of(1L, 2L),
                rangeStart,
                rangeEnd,
                0,
                10);

        verify(eventRepository, times(1)).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void updateEventAdmin_ShouldPublishEvent_WhenValid() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(0L);

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .stateAction(AdminStateAction.PUBLISH_EVENT)
                .build();

        EventFullDto result = eventService.updateEventAdmin(1L, request);

        assertNotNull(result);
        assertEquals(EventState.PUBLISHED.name(), result.getState());
        assertNotNull(result.getPublishedOn());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateEventAdmin_ShouldRejectEvent_WhenValid() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(0L);

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .stateAction(AdminStateAction.REJECT_EVENT)
                .build();

        EventFullDto result = eventService.updateEventAdmin(1L, request);

        assertNotNull(result);
        assertEquals(EventState.CANCELED.name(), result.getState());
    }

    @Test
    void updateEventAdmin_ShouldThrowNotFoundException_WhenEventNotFound() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .stateAction(AdminStateAction.PUBLISH_EVENT)
                .build();

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> eventService.updateEventAdmin(999L, request));

        assertEquals("Event not found", exception.getMessage());
    }

    @Test
    void updateEventAdmin_ShouldThrowConflictException_WhenPublishingNonPendingEvent() {
        event.setState(EventState.PUBLISHED);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .stateAction(AdminStateAction.PUBLISH_EVENT)
                .build();

        ConflictException exception = assertThrows(ConflictException.class,
                () -> eventService.updateEventAdmin(1L, request));

        assertEquals("Only pending events can be published", exception.getMessage());
    }

    @Test
    void updateEventAdmin_ShouldThrowConflictException_WhenRejectingPublishedEvent() {
        event.setState(EventState.PUBLISHED);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .stateAction(AdminStateAction.REJECT_EVENT)
                .build();

        ConflictException exception = assertThrows(ConflictException.class,
                () -> eventService.updateEventAdmin(1L, request));

        assertEquals("Cannot reject published event", exception.getMessage());
    }

    @Test
    void updateEventAdmin_ShouldUpdateAllFields_WhenProvided() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(0L);

        Category newCategory = Category.builder().id(2L).name("Выставки").build();
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));

        String newEventDate = now.plusDays(20).format(Constants.FORMATTER);

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .annotation("Новая аннотация")
                .description("Новое описание")
                .eventDate(newEventDate)
                .paid(true)
                .participantLimit(50)
                .requestModeration(false)
                .title("Новый заголовок")
                .category(2L)
                .build();

        EventFullDto result = eventService.updateEventAdmin(1L, request);

        assertNotNull(result);
        verify(categoryRepository, times(1)).findById(2L);
    }

    @Test
    void updateEventAdmin_ShouldUpdateOnlyAnnotation_WhenOnlyAnnotationProvided() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(0L);

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .annotation("Новая аннотация")
                .build();

        EventFullDto result = eventService.updateEventAdmin(1L, request);

        assertNotNull(result);
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    void updateEventAdmin_ShouldUpdateOnlyDescription_WhenOnlyDescriptionProvided() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(0L);

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .description("Новое описание")
                .build();

        EventFullDto result = eventService.updateEventAdmin(1L, request);

        assertNotNull(result);
    }

    @Test
    void updateEventAdmin_ShouldUpdateOnlyTitle_WhenOnlyTitleProvided() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(0L);

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .title("Новый заголовок")
                .build();

        EventFullDto result = eventService.updateEventAdmin(1L, request);

        assertNotNull(result);
    }

    @Test
    void updateEventAdmin_ShouldUpdateOnlyPaid_WhenOnlyPaidProvided() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(0L);

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .paid(true)
                .build();

        EventFullDto result = eventService.updateEventAdmin(1L, request);

        assertNotNull(result);
    }

    @Test
    void updateEventAdmin_ShouldUpdateOnlyParticipantLimit_WhenOnlyParticipantLimitProvided() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(0L);

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .participantLimit(50)
                .build();

        EventFullDto result = eventService.updateEventAdmin(1L, request);

        assertNotNull(result);
    }

    @Test
    void updateEventAdmin_ShouldUpdateOnlyRequestModeration_WhenOnlyRequestModerationProvided() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(0L);

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .requestModeration(false)
                .build();

        EventFullDto result = eventService.updateEventAdmin(1L, request);

        assertNotNull(result);
    }

    @Test
    void updateEventAdmin_ShouldUpdateOnlyEventDate_WhenOnlyEventDateProvided() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(0L);

        String newEventDate = now.plusDays(15).format(Constants.FORMATTER);
        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .eventDate(newEventDate)
                .build();

        EventFullDto result = eventService.updateEventAdmin(1L, request);

        assertNotNull(result);
    }

    @Test
    void updateEventAdmin_ShouldUpdateOnlyCategory_WhenOnlyCategoryProvided() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(0L);

        Category newCategory = Category.builder().id(2L).name("Выставки").build();
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .category(2L)
                .build();

        EventFullDto result = eventService.updateEventAdmin(1L, request);

        assertNotNull(result);
        verify(categoryRepository, times(1)).findById(2L);
    }

    @Test
    void updateEventAdmin_ShouldThrowNotFoundException_WhenCategoryNotFound() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .category(999L)
                .build();

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> eventService.updateEventAdmin(1L, request));

        assertEquals("Category not found", exception.getMessage());
    }

    @Test
    void updateEventAdmin_ShouldReturnWithConfirmedRequests() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(10L);

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .annotation("Новая аннотация")
                .build();

        EventFullDto result = eventService.updateEventAdmin(1L, request);

        assertNotNull(result);
        assertEquals(10L, result.getConfirmedRequests());
        verify(requestRepository, times(1)).countByEventIdAndStatus(1L, RequestStatus.CONFIRMED);
    }
}