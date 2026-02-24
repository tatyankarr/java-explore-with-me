package ru.practicum.ewm.main.request.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParticipationRequestDto {
    private Long id;
    private String created;
    private Long event;
    private Long requester;
    private String status;
}
