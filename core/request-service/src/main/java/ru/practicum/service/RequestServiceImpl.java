package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.events.EventFullDto;
import ru.practicum.dto.events.EventState;
import ru.practicum.dto.request.*;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.exceptions.ConflictDataException;
import ru.practicum.exceptions.InvalidDataException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.grpc.stats.action.ActionTypeProto;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.Request;
import ru.practicum.repository.RequestRepository;
import ru.practicum.stat.StatClient;

import java.security.InvalidParameterException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final UserClient userClient;
    private final EventClient eventClient;
    private final StatClient statClient;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequests(Long userId) {
        userClient.getById(userId);
        List<Request> requests = requestRepository.findAllByRequesterId(userId);
        return requestMapper.toParticipationRequestDto(requests);
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        EventFullDto event = eventClient.getById(eventId);
        userClient.getById(userId);

        checkRequest(userId, event);

        RequestStatus status = (!event.getRequestModeration() || event.getParticipantLimit() == 0)
                ? RequestStatus.CONFIRMED
                : RequestStatus.PENDING;

        Request request = Request.builder()
                .requesterId(userId)
                .eventId(eventId)
                .created(LocalDateTime.now())
                .status(status)
                .build();

        request = requestRepository.save(request);

        statClient.registerUserAction(eventId, userId, ActionTypeProto.ACTION_REGISTER, Instant.now());

        return requestMapper.toParticipationRequestDto(request);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        userClient.getById(userId);

        Request request = getRequestById(requestId);

        if (!request.getRequesterId().equals(userId)) {
            throw new InvalidDataException("Другой пользователь не может отменить запрос");
        }

        request.setStatus(RequestStatus.CANCELED);
        requestRepository.save(request);

        return requestMapper.toParticipationRequestDto(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestByUserOfEvent(Long userId, Long eventId) {
        userClient.getById(userId);
        List<Request> requests = requestRepository.findAllByRequesterIdAndEventId(userId, eventId);
        return requests.stream()
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest requestStatusUpdateRequest) {
        userClient.getById(userId);

        EventFullDto event = eventClient.getById(eventId);

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            return result;
        }

        List<Request> allForEvent = requestRepository.findAllByEventId(eventId);

        Set<Long> idsToUpdate = requestStatusUpdateRequest.getRequestIds().stream().collect(Collectors.toSet());
        List<Request> requestsToUpdate = allForEvent.stream()
                .filter(r -> idsToUpdate.contains(r.getId()))
                .collect(Collectors.toList());

        boolean tryingRejectConfirmed = requestsToUpdate.stream()
                .anyMatch(r -> r.getStatus() == RequestStatus.CONFIRMED
                        && requestStatusUpdateRequest.getStatus() == RequestStatus.REJECTED);
        if (tryingRejectConfirmed) {
            throw new InvalidParameterException("Request already confirmed");
        }

        long alreadyConfirmed = requestRepository.findAllByEventId(eventId).stream()
                .filter(r -> RequestStatus.CONFIRMED.equals(r.getStatus()))
                .count();

        long willBeConfirmed = 0;
        if (requestStatusUpdateRequest.getStatus() == RequestStatus.CONFIRMED) {
            willBeConfirmed = requestsToUpdate.stream()
                    .filter(r -> !RequestStatus.CONFIRMED.equals(r.getStatus()))
                    .count();
        }

        if (event.getParticipantLimit() != 0 && (alreadyConfirmed + willBeConfirmed) > event.getParticipantLimit()) {
            throw new InvalidParameterException("Exceeding the limit of participants");
        }

        requestsToUpdate.forEach(r -> r.setStatus(requestStatusUpdateRequest.getStatus()));
        requestRepository.saveAll(requestsToUpdate);

        if (requestStatusUpdateRequest.getStatus() == RequestStatus.CONFIRMED) {
            result.setConfirmedRequests(requestsToUpdate.stream()
                    .map(requestMapper::toParticipationRequestDto)
                    .collect(Collectors.toList()));
        } else if (requestStatusUpdateRequest.getStatus() == RequestStatus.REJECTED) {
            result.setRejectedRequests(requestsToUpdate.stream()
                    .map(requestMapper::toParticipationRequestDto)
                    .collect(Collectors.toList()));
        }

        return result;
    }

    private void checkRequest(Long requesterId, EventFullDto event) {
        if (requestRepository.existsByRequesterIdAndEventId(requesterId, event.getId())) {
            throw new ConflictDataException("Нельзя создать повторный запрос");
        }

        UserShortDto initiator = event.getInitiator();
        if (initiator != null && initiator.getId().equals(requesterId)) {
            throw new ConflictDataException("Инициатор события не может добавить запрос на участие в своём событии");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictDataException("Нельзя участвовать в неопубликованных событиях");
        }

        long confirmedCount = requestRepository.findAllByEventId(event.getId()).stream()
                .filter(r -> RequestStatus.CONFIRMED.equals(r.getStatus()))
                .count();

        if (event.getParticipantLimit() != 0 && confirmedCount >= event.getParticipantLimit()) {
            throw new ConflictDataException("У события достигнут лимит запросов на участие");
        }
    }

    private Request getRequestById(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запроса с id " + requestId + " не существует"));
    }

    @Override
    public List<ParticipationRequestDto> findAllByEventIdAndStatus(Long eventId, RequestStatus status) {
        return requestRepository.findAllByEventIdAndStatus(eventId, status)
                .stream()
                .map(requestMapper::toParticipationRequestDto)
                .toList();
    }

    @Override
    public List<ParticipationRequestDto> getByIds(List<Long> ids) {
        return requestRepository.findAllById(ids)
                .stream()
                .map(requestMapper::toParticipationRequestDto)
                .toList();
    }

    @Override
    public List<RequestCountDto> getConfirmedCount(List<Long> ids) {
        return requestRepository.getParticipationRequestCountConfirmed(ids)
                .stream()
                .map(requestMapper::toParticipationRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public List<ParticipationRequestDto> updateStatus(RequestStatus status, List<Long> ids) {
        List<Request> requests = requestRepository.findAllById(ids);

        if (status == RequestStatus.REJECTED &&
                requests.stream().anyMatch(request -> request.getStatus() == RequestStatus.CONFIRMED)) {
            throw new InvalidDataException("Среди заявок уже есть подтвержденные");
        }

        requests.forEach(request -> request.setStatus(status));
        List<Request> updatedRequests = requestRepository.saveAll(requests);
        return updatedRequests
                .stream()
                .map(requestMapper::toParticipationRequestDto)
                .toList();
    }
}