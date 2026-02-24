package ru.practicum.ewm.main.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.event.EventRepository;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.event.model.EventState;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.request.RequestRepository;
import ru.practicum.ewm.main.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.main.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.main.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.main.request.dto.RequestMapper;
import ru.practicum.ewm.main.request.model.ParticipationRequest;
import ru.practicum.ewm.main.request.model.RequestStatus;
import ru.practicum.ewm.main.user.User;
import ru.practicum.ewm.main.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {

        return requestRepository.findAllByRequesterId(userId)
                .stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Cannot request unpublished event");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Cannot request participation in your own event");
        }

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Request already exists");
        }

        long confirmedCount =
                requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        if (event.getParticipantLimit() > 0 &&
                confirmedCount >= event.getParticipantLimit()) {
            throw new ConflictException("Participant limit reached");
        }

        RequestStatus status;

        if (event.getRequestModeration()) {
            status = RequestStatus.PENDING;
        } else {
            status = RequestStatus.CONFIRMED;
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .event(event)
                .requester(user)
                .status(status)
                .created(LocalDateTime.now())
                .build();

        return RequestMapper.toParticipationRequestDto(
                requestRepository.save(request));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {

        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new ConflictException("Only requester can cancel request");
        }

        request.setStatus(RequestStatus.CANCELED);

        return RequestMapper.toParticipationRequestDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(Long userId,
                                                              Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Only initiator can view requests");
        }

        return requestRepository.findAllByEventId(eventId)
                .stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId,
                                                              Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Only initiator can update requests");
        }

        List<ParticipationRequest> requests =
                requestRepository.findAllByIdIn(updateRequest.getRequestIds());

        long confirmedCount =
                requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        List<ParticipationRequestDto> confirmed = new java.util.ArrayList<>();
        List<ParticipationRequestDto> rejected = new java.util.ArrayList<>();

        for (ParticipationRequest request : requests) {

            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                continue;
            }

            if (updateRequest.getStatus() == RequestStatus.CONFIRMED) {

                if (event.getParticipantLimit() > 0 &&
                        confirmedCount >= event.getParticipantLimit()) {

                    request.setStatus(RequestStatus.REJECTED);
                    rejected.add(RequestMapper.toParticipationRequestDto(request));

                } else {
                    request.setStatus(RequestStatus.CONFIRMED);
                    confirmedCount++;
                    confirmed.add(RequestMapper.toParticipationRequestDto(request));
                }

            } else {
                request.setStatus(RequestStatus.REJECTED);
                rejected.add(RequestMapper.toParticipationRequestDto(request));
            }
        }

        requestRepository.saveAll(requests);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed)
                .rejectedRequests(rejected)
                .build();
    }
}