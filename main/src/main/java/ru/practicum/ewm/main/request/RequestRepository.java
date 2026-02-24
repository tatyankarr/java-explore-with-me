package ru.practicum.ewm.main.request;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.main.request.model.ParticipationRequest;
import ru.practicum.ewm.main.request.model.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findAllByRequesterId(Long userId);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    boolean existsByEventIdAndRequesterId(Long eventId, Long userId);

    Long countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<ParticipationRequest> findAllByIdIn(List<Long> ids);
}
