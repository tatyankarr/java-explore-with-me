package ru.practicum.ewm.main.event.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import ru.practicum.ewm.main.category.Category;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.event.model.EventState;
import ru.practicum.ewm.main.location.Location;
import ru.practicum.ewm.main.user.User;
import ru.practicum.ewm.main.util.Constants;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventMapperTest {
    private Event event;
    private final Long confirmedRequests = 5L;
    private final Long views = 100L;
    private Category category;
    private User initiator;
    private Location location;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        initiator = User.builder()
                .id(1L)
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build();

        location = Location.builder()
                .id(1L)
                .lat(55.75f)
                .lon(37.62f)
                .build();

        event = Event.builder()
                .id(1L)
                .annotation("Тестовое описание события для проверки маппера")
                .category(category)
                .description("Полное описание события для тестирования маппера")
                .eventDate(now.plusDays(30))
                .location(location)
                .initiator(initiator)
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .title("Заголовок события")
                .state(EventState.PENDING)
                .createdOn(now)
                .publishedOn(null)
                .build();
    }

    @Nested
    class ToEventTests {
        @Test
        void shouldConvertNewEventDtoToEvent() {
            String eventDate = now.plusDays(30).format(Constants.FORMATTER);

            NewEventDto dto = NewEventDto.builder()
                    .annotation("Тестовое описание события для проверки")
                    .description("Полное описание события для тестирования")
                    .eventDate(eventDate)
                    .paid(true)
                    .participantLimit(100)
                    .requestModeration(true)
                    .title("Заголовок")
                    .build();

            Event result = EventMapper.toEvent(dto);

            assertNotNull(result);
            assertEquals(dto.getAnnotation(), result.getAnnotation());
            assertEquals(dto.getDescription(), result.getDescription());
            assertEquals(LocalDateTime.parse(dto.getEventDate(), Constants.FORMATTER), result.getEventDate());
            assertEquals(dto.getPaid(), result.getPaid());
            assertEquals(dto.getParticipantLimit(), result.getParticipantLimit());
            assertEquals(dto.getRequestModeration(), result.getRequestModeration());
            assertEquals(dto.getTitle(), result.getTitle());

            assertNull(result.getId());
            assertNull(result.getCategory());
            assertNull(result.getLocation());
            assertNull(result.getInitiator());
            assertNull(result.getState());
            assertNull(result.getCreatedOn());
            assertNull(result.getPublishedOn());
        }

        @Test
        void shouldConvertNewEventDtoWithMinValuesToEvent() {
            String eventDate = now.plusDays(30).format(Constants.FORMATTER);

            NewEventDto dto = NewEventDto.builder()
                    .annotation("Минимальная аннотация")
                    .description("Минимальное описание")
                    .eventDate(eventDate)
                    .title("Мин")
                    .build();

            Event result = EventMapper.toEvent(dto);

            assertNotNull(result);
            assertEquals("Минимальная аннотация", result.getAnnotation());
            assertEquals("Минимальное описание", result.getDescription());
            assertEquals(LocalDateTime.parse(eventDate, Constants.FORMATTER), result.getEventDate());
            assertEquals("Мин", result.getTitle());

            assertNull(result.getPaid());
            assertNull(result.getParticipantLimit());
            assertNull(result.getRequestModeration());
        }
    }
}