package ru.practicum.ewm.main.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.main.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.main.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.main.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.main.request.model.RequestStatus;
import ru.practicum.ewm.main.request.service.RequestService;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RequestController.class)
class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestService requestService;

    private ParticipationRequestDto requestDto;
    private EventRequestStatusUpdateResult updateResult;

    @BeforeEach
    void setUp() {
        requestDto = ParticipationRequestDto.builder()
                .id(1L)
                .event(1L)
                .requester(2L)
                .status(RequestStatus.PENDING.name())
                .created("2024-01-01 12:00:00")
                .build();

        updateResult = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(List.of(requestDto))
                .rejectedRequests(List.of())
                .build();
    }

    @Test
    void getUserRequests_ShouldReturnRequests() throws Exception {
        when(requestService.getUserRequests(2L)).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/users/2/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(requestService, times(1)).getUserRequests(2L);
    }

    @Test
    void getUserRequests_ShouldReturnEmptyList_WhenNoRequests() throws Exception {
        when(requestService.getUserRequests(2L)).thenReturn(List.of());

        mockMvc.perform(get("/users/2/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void addRequest_ShouldCreateRequest() throws Exception {
        when(requestService.addParticipationRequest(eq(2L), eq(1L))).thenReturn(requestDto);

        mockMvc.perform(post("/users/2/requests")
                        .param("eventId", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(requestService, times(1)).addParticipationRequest(2L, 1L);
    }

    @Test
    void addRequest_ShouldReturnBadRequest_WhenEventIdNotProvided() throws Exception {
        mockMvc.perform(post("/users/2/requests"))
                .andExpect(status().isBadRequest());

        verify(requestService, never()).addParticipationRequest(anyLong(), anyLong());
    }

    @Test
    void cancelRequest_ShouldCancelRequest() throws Exception {
        requestDto.setStatus(RequestStatus.CANCELED.name());
        when(requestService.cancelRequest(2L, 1L)).thenReturn(requestDto);

        mockMvc.perform(patch("/users/2/requests/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CANCELED"));

        verify(requestService, times(1)).cancelRequest(2L, 1L);
    }

    @Test
    void getEventParticipants_ShouldReturnRequests() throws Exception {
        when(requestService.getEventParticipants(1L, 1L)).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/users/1/events/1/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(requestService, times(1)).getEventParticipants(1L, 1L);
    }

    @Test
    void changeRequestStatus_ShouldUpdateRequests() throws Exception {
        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(1L, 2L));
        updateRequest.setStatus(RequestStatus.CONFIRMED);

        when(requestService.changeRequestStatus(eq(1L), eq(1L), any(EventRequestStatusUpdateRequest.class)))
                .thenReturn(updateResult);

        mockMvc.perform(patch("/users/1/events/1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedRequests", hasSize(1)))
                .andExpect(jsonPath("$.rejectedRequests", hasSize(0)));

        verify(requestService, times(1)).changeRequestStatus(eq(1L), eq(1L), any());
    }

    @Test
    void changeRequestStatus_ShouldReturnBadRequest_WhenInvalidBody() throws Exception {
        mockMvc.perform(patch("/users/1/events/1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }
}
