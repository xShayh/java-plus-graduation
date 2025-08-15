package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.events.EventFullDto;
import ru.practicum.dto.events.EventState;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.RequestDto;
import ru.practicum.dto.request.RequestStatus;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.exceptions.InvalidDataException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.Request;
import ru.practicum.repository.RequestRepository;

import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserClient userClient;
    private final EventClient eventClient;

    @Override
    @Transactional(readOnly = true)
    public List<RequestDto> getRequests(Long userId) {
        userClient.getById(userId);
        List<Request> requests = requestRepository.findAllByRequester_Id(userId);
        return RequestMapper.INSTANCE.mapListRequests(requests);
    }

    @Override
    @Transactional
    public RequestDto createRequest(Long userId, Long eventId) {
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

        return RequestMapper.INSTANCE.mapToRequestDto(request);
    }

    @Override
    @Transactional
    public RequestDto cancelRequest(Long userId, Long requestId) {
        userClient.getById(userId);

        Request request = getRequestById(requestId);

        if (!request.getRequesterId().equals(userId)) {
            throw new InvalidDataException("Другой пользователь не может отменить запрос");
        }

        request.setStatus(RequestStatus.CANCELED);
        requestRepository.save(request);

        return RequestMapper.INSTANCE.mapToRequestDto(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestDto> getRequestByUserOfEvent(Long userId, Long eventId) {
        userClient.getById(userId);
        List<Request> requests = requestRepository.findAllByRequester_IdAndEvent_id(userId, eventId);
        return requests.stream()
                .map(RequestMapper.INSTANCE::mapToRequestDto)
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

        List<Request> allForEvent = requestRepository.findAllByEvent_id(eventId);

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

        long alreadyConfirmed = requestRepository.findAllByEvent_id(eventId).stream()
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
                    .map(RequestMapper.INSTANCE::toParticipationRequestDto)
                    .collect(Collectors.toList()));
        } else if (requestStatusUpdateRequest.getStatus() == RequestStatus.REJECTED) {
            result.setRejectedRequests(requestsToUpdate.stream()
                    .map(RequestMapper.INSTANCE::toParticipationRequestDto)
                    .collect(Collectors.toList()));
        }

        return result;
    }

    private void checkRequest(Long requesterId, EventFullDto event) {
        if (requestRepository.existsByRequesterAndEvent(requesterId, event.getId())) {
            throw new InvalidParameterException("Нельзя создать повторный запрос");
        }

        UserShortDto initiator = event.getInitiator();
        if (initiator != null && initiator.getId().equals(requesterId)) {
            throw new InvalidParameterException("Инициатор события не может добавить запрос на участие в своём событии");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new InvalidParameterException("Нельзя участвовать в неопубликованных событиях");
        }

        long confirmedCount = requestRepository.findAllByEvent_id(event.getId()).stream()
                .filter(r -> RequestStatus.CONFIRMED.equals(r.getStatus()))
                .count();

        if (event.getParticipantLimit() != 0 && confirmedCount >= event.getParticipantLimit()) {
            throw new InvalidParameterException("У события достигнут лимит запросов на участие");
        }
    }

    private Request getRequestById(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запроса с id " + requestId + " не существует"));
    }
}