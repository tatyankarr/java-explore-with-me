package ru.practicum.ewm.main.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.ewm.main.event.model.UserStateAction;
import ru.practicum.ewm.main.location.dto.LocationDto;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventUserRequest {
    @Size(min = 20, max = 2000)
    private String annotation;
    private Long category;
    @Size(min = 20, max = 7000)
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String eventDate;
    private LocationDto location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private UserStateAction stateAction;
    @Size(min = 3, max = 120)
    private String title;
}
