package ru.practicum.ewm.main.event.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.ewm.main.category.Category;
import ru.practicum.ewm.main.category.CategoryRepository;
import ru.practicum.ewm.main.event.EventRepository;
import ru.practicum.ewm.main.event.dto.EventFullDto;
import ru.practicum.ewm.main.event.dto.EventShortDto;
import ru.practicum.ewm.main.event.dto.NewEventDto;
import ru.practicum.ewm.main.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.event.model.EventState;
import ru.practicum.ewm.main.event.model.UserStateAction;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.location.Location;
import ru.practicum.ewm.main.location.LocationRepository;
import ru.practicum.ewm.main.location.dto.LocationDto;
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
class EventServiceImplPrivateTest {

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

    private User user;
    private Category category;
    private Location location;
    private Event event;
    private NewEventDto newEventDto;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        user = User.builder()
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
                .initiator(user)
                .location(location)
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .title("Заголовок события")
                .state(EventState.PENDING)
                .createdOn(now)
                .build();

        LocationDto locationDto = LocationDto.builder()
                .lat(55.75f)
                .lon(37.62f)
                .build();

        newEventDto = NewEventDto.builder()
                .annotation("Аннотация события для тестирования")
                .category(1L)
                .description("Полное описание события для тестирования")
                .eventDate(now.plusDays(10).format(Constants.FORMATTER))
                .location(locationDto)
                .paid(true)
                .participantLimit(100)
                .requestModeration(true)
                .title("Заголовок события")
                .build();
    }

    @Test
    void addEvent_ShouldCreateEvent_WhenValid() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(locationRepository.save(any(Location.class))).thenReturn(location);
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        EventFullDto result = eventService.addEvent(1L, newEventDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Заголовок события", result.getTitle());
        assertEquals(EventState.PENDING.name(), result.getState());
        verify(userRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).findById(1L);
        verify(locationRepository, times(1)).save(any(Location.class));
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void addEvent_ShouldThrowNotFoundException_WhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> eventService.addEvent(999L, newEventDto));

        assertEquals("User not found", exception.getMessage());
        verify(categoryRepository, never()).findById(any());
        verify(locationRepository, never()).save(any());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void addEvent_ShouldThrowNotFoundException_WhenCategoryNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        NewEventDto dtoWithInvalidCategory = NewEventDto.builder()
                .annotation("Аннотация")
                .category(999L)
                .description("Описание")
                .eventDate(now.plusDays(10).format(Constants.FORMATTER))
                .location(new LocationDto(55.75f, 37.62f))
                .title("Заголовок")
                .build();

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> eventService.addEvent(1L, dtoWithInvalidCategory));

        assertEquals("Category not found", exception.getMessage());
        verify(locationRepository, never()).save(any());
        verify(eventRepository, never()).save(any());
    }

    /*@Test
    void addEvent_ShouldThrowConflictException_WhenEventDateIsTooSoon() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        NewEventDto dtoWithSoonDate = NewEventDto.builder()
                .annotation("Аннотация")
                .category(1L)
                .description("Описание")
                .eventDate(now.plusHours(1).format(Constants.FORMATTER))
                .location(new LocationDto(55.75f, 37.62f))
                .title("Заголовок")
                .build();

        ConflictException exception = assertThrows(ConflictException.class,
                () -> eventService.addEvent(1L, dtoWithSoonDate));

        assertEquals("Event date must be at least 2 hours later", exception.getMessage());
        verify(locationRepository, never()).save(any());
        verify(eventRepository, never()).save(any());
    }*/

    @Test
    void addEvent_ShouldUseDefaultValues_WhenNotProvided() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(locationRepository.save(any(Location.class))).thenReturn(location);
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        NewEventDto dtoWithDefaults = NewEventDto.builder()
                .annotation("Аннотация события для тестирования")
                .category(1L)
                .description("Описание события")
                .eventDate(now.plusDays(10).format(Constants.FORMATTER))
                .location(new LocationDto(55.75f, 37.62f))
                .title("Заголовок")
                .build();

        EventFullDto result = eventService.addEvent(1L, dtoWithDefaults);

        assertNotNull(result);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void getEventsByUser_ShouldReturnEvents_WhenExists() {
        List<Event> events = List.of(event);
        PageRequest pageable = PageRequest.of(0, 10);

        when(eventRepository.findAllByInitiatorId(eq(1L), any(PageRequest.class)))
                .thenReturn(events);
        when(requestRepository.countByEventIdAndStatus(anyLong(), eq(RequestStatus.CONFIRMED)))
                .thenReturn(5L);

        List<EventShortDto> result = eventService.getEventsByUser(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(5L, result.get(0).getConfirmedRequests());
        verify(eventRepository, times(1)).findAllByInitiatorId(eq(1L), any(PageRequest.class));
    }

    @Test
    void getEventsByUser_ShouldReturnEmptyList_WhenNoEvents() {
        when(eventRepository.findAllByInitiatorId(eq(1L), any(PageRequest.class)))
                .thenReturn(List.of());

        List<EventShortDto> result = eventService.getEventsByUser(1L, 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getEventsByUser_ShouldCalculatePageCorrectly() {
        when(eventRepository.findAllByInitiatorId(eq(1L), eq(PageRequest.of(2, 10))))
                .thenReturn(List.of());

        eventService.getEventsByUser(1L, 20, 10);

        verify(eventRepository, times(1)).findAllByInitiatorId(eq(1L), eq(PageRequest.of(2, 10)));
    }

    @Test
    void getEventByIdByUser_ShouldReturnEvent_WhenExists() {
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(3L);

        EventFullDto result = eventService.getEventByIdByUser(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(3L, result.getConfirmedRequests());
        verify(eventRepository, times(1)).findByIdAndInitiatorId(1L, 1L);
    }

    @Test
    void getEventByIdByUser_ShouldThrowNotFoundException_WhenEventNotFound() {
        when(eventRepository.findByIdAndInitiatorId(999L, 1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> eventService.getEventByIdByUser(1L, 999L));

        assertEquals("Event not found", exception.getMessage());
    }

    @Test
    void getEventByIdByUser_ShouldThrowNotFoundException_WhenUserNotOwner() {
        when(eventRepository.findByIdAndInitiatorId(1L, 2L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> eventService.getEventByIdByUser(2L, 1L));

        assertEquals("Event not found", exception.getMessage());
    }

    @Test
    void updateEventByUser_ShouldUpdateAllFields_WhenValid() {
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(2L);

        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .annotation("Новая аннотация")
                .description("Новое описание")
                .title("Новый заголовок")
                .paid(true)
                .participantLimit(50)
                .requestModeration(false)
                .build();

        EventFullDto result = eventService.updateEventByUser(1L, 1L, request);

        assertNotNull(result);
        assertEquals("Новая аннотация", result.getAnnotation());
        assertEquals("Новое описание", result.getDescription());
        assertEquals("Новый заголовок", result.getTitle());
        assertTrue(result.getPaid());
        assertEquals(50, result.getParticipantLimit());
        assertFalse(result.getRequestModeration());
    }

    @Test
    void updateEventByUser_ShouldSendToReview_WhenValid() {
        event.setState(EventState.CANCELED);
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(0L);

        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .stateAction(UserStateAction.SEND_TO_REVIEW)
                .build();

        EventFullDto result = eventService.updateEventByUser(1L, 1L, request);

        assertNotNull(result);
        assertEquals(EventState.PENDING.name(), result.getState());
    }

    @Test
    void updateEventByUser_ShouldCancelReview_WhenValid() {
        event.setState(EventState.PENDING);
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(0L);

        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .stateAction(UserStateAction.CANCEL_REVIEW)
                .build();

        EventFullDto result = eventService.updateEventByUser(1L, 1L, request);

        assertNotNull(result);
        assertEquals(EventState.CANCELED.name(), result.getState());
    }

    @Test
    void updateEventByUser_ShouldThrowNotFoundException_WhenEventNotFound() {
        when(eventRepository.findByIdAndInitiatorId(999L, 1L)).thenReturn(Optional.empty());

        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .annotation("Новая аннотация")
                .build();

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> eventService.updateEventByUser(1L, 999L, request));

        assertEquals("Event not found", exception.getMessage());
    }

    @Test
    void updateEventByUser_ShouldThrowConflictException_WhenEditingPublishedEvent() {
        event.setState(EventState.PUBLISHED);
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));

        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .annotation("Новая аннотация")
                .build();

        ConflictException exception = assertThrows(ConflictException.class,
                () -> eventService.updateEventByUser(1L, 1L, request));

        assertEquals("Cannot edit published event", exception.getMessage());
    }

    /*@Test
    void updateEventByUser_ShouldThrowConflictException_WhenEventDateIsTooSoon() {
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));

        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .eventDate(now.plusHours(1).format(Constants.FORMATTER))
                .build();

        ConflictException exception = assertThrows(ConflictException.class,
                () -> eventService.updateEventByUser(1L, 1L, request));

        assertEquals("Event date must be at least 2 hours later", exception.getMessage());
    }*/

    @Test
    void updateEventByUser_ShouldThrowConflictException_WhenSendingPublishedToReview() {
        event.setState(EventState.PUBLISHED);
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));

        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .stateAction(UserStateAction.SEND_TO_REVIEW)
                .build();

        ConflictException exception = assertThrows(ConflictException.class,
                () -> eventService.updateEventByUser(1L, 1L, request));

        assertEquals("Cannot edit published event", exception.getMessage());
    }

    @Test
    void updateEventByUser_ShouldThrowConflictException_WhenCancelingNonPendingEvent() {
        event.setState(EventState.CANCELED);
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));

        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .stateAction(UserStateAction.CANCEL_REVIEW)
                .build();

        ConflictException exception = assertThrows(ConflictException.class,
                () -> eventService.updateEventByUser(1L, 1L, request));

        assertEquals("Only pending events can be canceled", exception.getMessage());
    }

    @Test
    void updateEventByUser_ShouldUpdateCategory_WhenProvided() {
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(0L);

        Category newCategory = Category.builder().id(2L).name("Выставки").build();
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));

        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .category(2L)
                .build();

        EventFullDto result = eventService.updateEventByUser(1L, 1L, request);

        assertNotNull(result);
        verify(categoryRepository, times(1)).findById(2L);
    }

    @Test
    void updateEventByUser_ShouldThrowNotFoundException_WhenCategoryNotFound() {
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .category(999L)
                .build();

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> eventService.updateEventByUser(1L, 1L, request));

        assertEquals("Category not found", exception.getMessage());
    }

    @Test
    void updateEventByUser_ShouldReturnWithConfirmedRequests() {
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));
        when(requestRepository.countByEventIdAndStatus(1L, RequestStatus.CONFIRMED)).thenReturn(7L);

        UpdateEventUserRequest request = UpdateEventUserRequest.builder()
                .annotation("Новая аннотация")
                .build();

        EventFullDto result = eventService.updateEventByUser(1L, 1L, request);

        assertNotNull(result);
        assertEquals(7L, result.getConfirmedRequests());
    }
}