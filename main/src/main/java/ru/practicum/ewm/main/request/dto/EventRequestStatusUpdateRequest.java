package ru.practicum.ewm.main.request.dto;

import lombok.Data;
import ru.practicum.ewm.main.request.model.RequestStatus;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private RequestStatus status;
}
