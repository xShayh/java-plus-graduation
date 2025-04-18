package ru.practicum.request.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.exceptions.InvalidDataException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.service.RequestService;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

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
    public List<RequestDto> getRequests(Long userId) {
        getUser(userId);
        return RequestMapper.INSTANCE.mapListRequests(requestRepository.findAllByRequester_Id(userId));
    }

    @Transactional
    @Override
    public RequestDto createRequest(Long userId, Long eventId) {
        Event event = getEvent(eventId);
        User user = getUser(userId);
        checkRequest(userId, event);
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
    public RequestDto cancelRequest(Long userId, Long requestId) {
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

    private void checkRequest(Long userId, Event event) {
        if (!requestRepository.findAllByRequester_IdAndEvent_id(userId, event.getId()).isEmpty())
            throw new InvalidDataException("Нельзя создать повторный запрос");
        if (event.getInitiator().getId().equals(userId))
            throw new InvalidDataException("Инициатор события не может добавить запрос на участие в своём событии");
        if (!event.getState().equals(EventState.PUBLISHED))
            throw new InvalidDataException("Нельзя участвовать в неопубликованных событиях");
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit().equals(event.getConfirmedRequests()))
            throw new InvalidDataException("У события достигнут лимит запросов на участие");
    }

    private Event getEvent(Long eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty())
            throw new NotFoundException("События с id " + eventId.toString() + " не существует");
        return event.get();
    }

    private User getUser(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty())
            throw new NotFoundException("Пользователя с id " + userId.toString() + " не существует");
        return user.get();
    }

    private Request getRequest(Long requestId) {
        Optional<Request> request = requestRepository.findById(requestId);
        if (request.isEmpty())
            throw new NotFoundException("Запроса с id " + requestId.toString() + " не существует");
        return request.get();
    }
}