package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.events.model.Event;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.events.util.EventState;
import ru.practicum.exceptions.InvalidDataException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public List<RequestDto> getRequests(Integer userId) {
        getUser(userId);
        return RequestMapper.INSTANCE.mapListRequests(requestRepository.findAllByRequester_Id(userId));
    }

    @Transactional
    @Override
    public RequestDto createRequest(Integer userId, Integer eventId) {
        Event event = getEvent(eventId);
        User user = getUser(userId);
        checkRequest(user, event);
        Request request = Request.builder()
                .requester(user)
                .created(LocalDateTime.now())
                .status(!event.getRequestModeration()
                        || event.getParticipantLimit() == 0
                        ? RequestStatus.CONFIRMED : RequestStatus.PENDING)
                .event(event)
                .build();
        request = requestRepository.save(request);
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
        return RequestMapper.INSTANCE.mapToRequestDto(request);
    }

    @Transactional
    @Override
    public RequestDto cancelRequest(Integer userId, Integer requestId) {
        getUser(userId);
        Request request = getRequest(requestId);
        if (!request.getRequester().getId().equals(userId))
            throw new InvalidDataException("Другой пользователь не может отменить запрос");
        request.setStatus(RequestStatus.CANCELED);
        requestRepository.save(request);
        Event event = getEvent(request.getEvent().getId());
        event.setConfirmedRequests(event.getConfirmedRequests() - 1);
        eventRepository.save(event);
        return RequestMapper.INSTANCE.mapToRequestDto(request);
    }

    @Override
    public List<RequestDto> getRequestByUserOfEvent(Integer userId, Integer eventId) {
        return requestRepository.findAllByRequester_IdAndEvent_id(userId, eventId).stream()
                .map(RequestMapper.INSTANCE::mapToRequestDto)
                .toList();
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateRequests(Integer userId, Integer eventId, EventRequestStatusUpdateRequest requestStatusUpdateRequest) {
        Event event = getEvent(eventId);
        getUser(userId);

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            return result;
        }
        List<Request> requests = requestRepository.findAllByRequester_IdAndEvent_id(userId, eventId);
        List<Request> requestsToUpdate = requests.stream()
                .filter(request -> requestStatusUpdateRequest.getRequestIds().contains(request.getId()))
                .toList();

        if (requestsToUpdate.stream().anyMatch(request -> request.getStatus().equals(RequestStatus.CONFIRMED)
                && requestStatusUpdateRequest.getStatus().equals(RequestStatus.REJECTED))) {
            throw new InvalidParameterException("Request already confirmed");
        }
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit().equals(event.getConfirmedRequests()))
            throw new InvalidParameterException("Exceeding the limit of participants");

        for (Request request : requestsToUpdate) {
            request.setStatus(RequestStatus.valueOf(requestStatusUpdateRequest.getStatus().toString()));
        }
        requestRepository.saveAll(requestsToUpdate);
        if (requestStatusUpdateRequest.getStatus().equals(RequestStatus.CONFIRMED)) {
            event.setConfirmedRequests(event.getConfirmedRequests() + requestsToUpdate.size());
        }
        eventRepository.save(event);
        if (requestStatusUpdateRequest.getStatus().equals(RequestStatus.CONFIRMED)) {
            result.setConfirmedRequests(requestsToUpdate.stream()
                    .map(RequestMapper.INSTANCE::toParticipationRequestDto)
                    .toList());
        }
        if (requestStatusUpdateRequest.getStatus().equals(RequestStatus.REJECTED)) {
            result.setRejectedRequests(requestsToUpdate.stream()
                    .map(RequestMapper.INSTANCE::toParticipationRequestDto)
                    .toList());
        }
        return result;
    }

    private void checkRequest(User requester, Event event) {
        if (requestRepository.existsByRequesterAndEvent(requester, event))
            throw new InvalidParameterException("Нельзя создать повторный запрос");
        if (event.getInitiator().getId().equals(requester.getId()))
            throw new InvalidParameterException("Инициатор события не может добавить запрос на участие в своём событии");
        if (!event.getState().equals(EventState.PUBLISHED))
            throw new InvalidParameterException("Нельзя участвовать в неопубликованных событиях");
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit().equals(event.getConfirmedRequests()))
            throw new InvalidParameterException("У события достигнут лимит запросов на участие");
    }

    private Event getEvent(Integer eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty())
            throw new NotFoundException("События с id " + eventId.toString() + " не существует");
        return event.get();
    }

    private User getUser(Integer userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty())
            throw new NotFoundException("Пользователя с id " + userId.toString() + " не существует");
        return user.get();
    }

    private Request getRequest(Integer requestId) {
        Optional<Request> request = requestRepository.findById(requestId);
        if (request.isEmpty())
            throw new NotFoundException("Запроса с id " + requestId.toString() + " не существует");
        return request.get();
    }
}