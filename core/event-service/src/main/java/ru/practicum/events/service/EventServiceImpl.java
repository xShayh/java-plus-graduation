package ru.practicum.events.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.categories.repository.CategoryRepository;
import ru.practicum.categories.model.Category;
import ru.practicum.client.RequestClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.dto.events.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.RequestStatus;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.events.mapper.EventMapper;
import ru.practicum.events.mapper.LocationMapper;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.Like;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.events.repository.LikeRepository;
import ru.practicum.events.repository.LocationRepository;
import ru.practicum.dto.events.AdminEventState;
import ru.practicum.dto.events.EventState;
import ru.practicum.dto.events.StateActionForUser;
import ru.practicum.exceptions.ConflictDataException;
import ru.practicum.exceptions.EventDateValidationException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.util.DateTimeUtil;
import stat.StatClient;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final LikeRepository likeRepository;
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final StatClient statClient;
    private final UserClient userClient;
    private final RequestClient requestClient;

    private static final String START = "2025-01-01 00:00:00";
    private static final String END = "2025-12-31 23:59:59";

    @Override
    public List<EventFullDto> adminGetEvents(EventAdminParams eventParams) {
        Pageable page = PageRequest.of(eventParams.getFrom(), eventParams.getSize());
        if (eventParams.getRangeStart() == null) {
            eventParams.setRangeStart(LocalDateTime.now());
        }
        if (eventParams.getRangeEnd() == null) {
            eventParams.setRangeEnd(LocalDateTime.now().plusYears(1));
        }

        Page<Event> events = eventRepository.findAdminEvents(
                eventParams.getUsers(),
                eventParams.getStates(),
                eventParams.getCategories(),
                eventParams.getRangeStart(),
                eventParams.getRangeEnd(),
                page
        );

        return events.stream()
                .map(event -> {
                    EventFullDto dto = eventMapper.toEventFullDto(event);
                    dto.setInitiator(userClient.getById(event.getInitiatorId()));
                    return dto;
                })
                .toList();
    }

    public EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequestDto updateEventAdminRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));
        if (updateEventAdminRequest.getStateAction() == AdminEventState.PUBLISH_EVENT && event.getState() != EventState.PENDING) {
            throw new DataIntegrityViolationException("Event should be in PENDING state");
        }
        if (updateEventAdminRequest.getStateAction() == AdminEventState.REJECT_EVENT && event.getState() == EventState.PUBLISHED) {
            throw new DataIntegrityViolationException("Event can be rejected only in PENDING state");
        }
        if (updateEventAdminRequest.getStateAction() != null) {
            if (updateEventAdminRequest.getStateAction().equals(AdminEventState.PUBLISH_EVENT)) {
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            if (updateEventAdminRequest.getStateAction().equals(AdminEventState.REJECT_EVENT)) {
                event.setState(EventState.CANCELED);
            }
        }
        Optional.ofNullable(updateEventAdminRequest.getAnnotation()).ifPresent(event::setAnnotation);
        if (updateEventAdminRequest.getCategory() != null) {
            Category category = getCategory(updateEventAdminRequest.getCategory());
            event.setCategory(category);
        }
        Optional.ofNullable(updateEventAdminRequest.getDescription()).ifPresent(event::setDescription);
        Optional.ofNullable(updateEventAdminRequest.getEventDate()).ifPresent(event::setEventDate);
        if (updateEventAdminRequest.getLocation() != null) {
            event.setLocation(locationRepository.save(locationMapper.toLocation(updateEventAdminRequest.getLocation())));
        }
        Optional.ofNullable(updateEventAdminRequest.getPaid()).ifPresent(event::setPaid);
        Optional.ofNullable(updateEventAdminRequest.getParticipantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(updateEventAdminRequest.getRequestModeration()).ifPresent(event::setRequestModeration);
        Optional.ofNullable(updateEventAdminRequest.getTitle()).ifPresent(event::setTitle);
        event.setInitiatorId(updateEventAdminRequest.getInitiator().getId());
        log.info("Event with ID={} was updated", eventId);
        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> getEventsByUser(Long userId, Integer from, Integer size) {
        UserShortDto user = getUserById(userId);
        Pageable pageable = PageRequest.of(from / size, size);
        log.info("Получение событий пользователем с ID={}", userId);
        return eventRepository.findAllByInitiatorId(userId, pageable).stream()
                .map(event -> eventMapper.toEventShortDto(event, user))
                .toList();
    }

    private UserShortDto getUserById(Long userId) {
        UserShortDto user = userClient.getById(userId);
        if (user == null) {
            throw new NotFoundException("Такого пользователя не существует: " + userId);
        }

        return user;
    }

    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        log.info("Создание события пользователем с ID={}", userId);
        if (newEventDto.getEventDate() != null &&
                !newEventDto.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            throw new EventDateValidationException("Event date should be in 2+ hours after now");
        }
        Category category = getCategory(newEventDto.getCategory());

        Event event = eventMapper.toEvent(newEventDto, category, userId);
        event.setLocation(locationRepository.save(locationMapper.toLocation(newEventDto.getLocation())));
        if (newEventDto.getPaid() == null) event.setPaid(false);
        if (newEventDto.getParticipantLimit() == null) event.setParticipantLimit(0L);
        if (newEventDto.getRequestModeration() == null) event.setRequestModeration(true);

        Event saved = eventRepository.save(event);
        EventFullDto dto = eventMapper.toEventFullDto(saved);
        dto.setInitiator(userClient.getById(userId));
        return dto;
    }

    @Override
    public EventFullDto getFullEventInformation(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new RuntimeException(
                        String.format("Event with ID=%d was not found ", eventId)));

        EventFullDto dto = eventMapper.toEventFullDto(event);
        dto.setInitiator(userClient.getById(event.getInitiatorId()));
        return dto;
    }

    @Override
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserDto updateEventUserDto) {
        log.info("Обновление события с ID={} пользователем с ID={}", eventId, userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new RuntimeException(String.format("Event with ID=%d was not found ", eventId)));
        if (event.getPublishedOn() != null) {
            throw new ConflictDataException("Event is already published");
        }
        if (updateEventUserDto.getEventDate() != null && !updateEventUserDto.getEventDate()
                .isAfter(LocalDateTime.now().plusHours(2))) {
            throw new EventDateValidationException("Event date should be in 2+ hours after now");
        }
        Optional.ofNullable(updateEventUserDto.getAnnotation()).ifPresent(event::setAnnotation);
        if (updateEventUserDto.getCategory() != null) {
            Category category = getCategory(updateEventUserDto.getCategory());
            event.setCategory(category);
        }
        Optional.ofNullable(updateEventUserDto.getDescription()).ifPresent(event::setDescription);
        Optional.ofNullable(updateEventUserDto.getEventDate()).ifPresent(event::setEventDate);
        if (updateEventUserDto.getLocation() != null) {
            event.setLocation(locationMapper.toLocation(updateEventUserDto.getLocation()));
        }
        Optional.ofNullable(updateEventUserDto.getPaid()).ifPresent(event::setPaid);
        Optional.ofNullable(updateEventUserDto.getParticipantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(updateEventUserDto.getRequestModeration()).ifPresent(event::setRequestModeration);
        Optional.ofNullable(updateEventUserDto.getTitle()).ifPresent(event::setTitle);
        if (updateEventUserDto.getStateAction() != null) {
            if (updateEventUserDto.getStateAction().equals(StateActionForUser.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            } else {
                event.setState(EventState.CANCELED);
            }
        }
        log.info("Event with ID={} was updated", eventId);
        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> publicGetEvents(EventPublicParam eventPublicParam) {
        Pageable page = PageRequest.of(eventPublicParam.getFrom() / eventPublicParam.getSize(),
                eventPublicParam.getSize());

        if (eventPublicParam.getRangeStart() == null || eventPublicParam.getRangeEnd() == null) {
            eventPublicParam.setRangeStart(LocalDateTime.now());
            eventPublicParam.setRangeEnd(eventPublicParam.getRangeStart().plusYears(1));
        }

        if (eventPublicParam.getRangeStart().isAfter(eventPublicParam.getRangeEnd())) {
            throw new EventDateValidationException("End date should be before start date");
        }

        Page<Event> events = eventRepository.findPublicEvents(
                eventPublicParam.getText(),
                eventPublicParam.getCategory(),
                eventPublicParam.getPaid(),
                eventPublicParam.getRangeStart(),
                eventPublicParam.getRangeEnd(),
                eventPublicParam.getOnlyAvailable(),
                page);

        if (events.isEmpty()) {
            return List.of();
        }

        if (eventPublicParam.getSort() != null) {
            return switch (eventPublicParam.getSort()) {
                case EVENT_DATE -> events.stream()
                        .sorted(Comparator.comparing(Event::getEventDate))
                        .map(event -> eventMapper.toEventShortDto(event, getUserById(event.getInitiatorId())))
                        .toList();
                case VIEWS -> events.stream()
                        .sorted(Comparator.comparing(Event::getViews))
                        .map(event -> eventMapper.toEventShortDto(event, getUserById(event.getInitiatorId())))
                        .toList();
            };
        }
        log.info("Вызов метода по добавлению просмотров");
        for (Event event : events) {
            addViews("/events/" + event.getId(), event);
        }
        return events.stream()
                .map(event -> eventMapper.toEventShortDto(event, getUserById(event.getInitiatorId())))
                .toList();
    }

    @Override
    public EventFullDto publicGetEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ConflictDataException(String.format("Event with ID=%d was not found", eventId)));
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException(String.format("Event with ID=%d was not published", eventId));
        }
        log.info("Вызов метода по добавлению просмотров");
        addViews("/events/" + event.getId(), event);
        return eventMapper.toEventFullDto(event);
    }

    @Override
    public Long addLike(Long userId, Long eventId) {
        Event event = getEvent(eventId);

        if (!likeRepository.existsByIdUserIdAndIdEventId(userId, eventId)) {
            Like like = new Like(userId, event);
            likeRepository.save(like);
        }
        return likeRepository.countByEventId(eventId);
    }

    @Override
    public Long removeLike(Long userId, Long eventId) {
        if (likeRepository.existsByIdUserIdAndIdEventId(userId, eventId)) {
            likeRepository.deleteByIdUserIdAndIdEventId(userId, eventId);
        }
        return likeRepository.countByEventId(eventId);
    }

    @Override
    public List<UserShortDto> getLikedUsers(Long eventId) {
        List<Like> likes = likeRepository.findAllByEventId(eventId);
        List<Long> userIds = likes.stream()
                .map(Like::getUserId)
                .toList();
        return userClient.getByIds(userIds);
    }

    @Override
    public List<EventFullDto> adminGetEventsLikedByUser(Long userId) {
        List<Long> eventIds = getEventIdsLikedByUser(userId);
        return eventMapper.toEventFullDto(eventRepository.findAllById(eventIds));
    }

    @Override
    public List<EventShortDto> getAllLikedEvents(Long userId) {
        UserShortDto user = getUserById(userId);
        List<Long> eventIds = getEventIdsLikedByUser(userId);
        return eventRepository.findAllById(eventIds).stream()
                .map(event -> eventMapper.toEventShortDto(event, user))
                .toList();
    }

    private List<Long> getEventIdsLikedByUser(Long userId) {
        userClient.getById(userId);
        List<Like> likes = likeRepository.findAllByIdUserId(userId);
        if (likes.isEmpty()) {
            throw new NotFoundException(String.format("User with id=%d did not like any events", userId));
        }
        return likes.stream()
                .map(Like::getEvent)
                .map(Event::getId)
                .toList();
    }

    private void addViews(String uri, Event event) {
        ResponseEntity<Object> response = statClient.getStats(START, END, List.of(uri), false);
        ObjectMapper mapper = new ObjectMapper();
        List<ViewStatsDto> views = mapper.convertValue(response.getBody(), new TypeReference<List<ViewStatsDto>>() {
        });
        event.setViews(views.isEmpty() ? 0L : (long) views.size());
        log.info("Views was updated, views= {}", event.getViews());
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(String.format("Category with ID=%d was not found", categoryId)));
    }

    public Event getEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException(String.format("Event with id=%d was not published", eventId));
        }
        return event;
    }

    @Override
    public List<EventFullDto> getByLocation(Long locationId) {
        return eventRepository.findAllByLocationId(locationId).stream()
                .map(eventMapper::toEventFullDto)
                .toList();
    }

    @Override
    public EventFullDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Такого события не существует: " + eventId));
        return eventMapper.toEventFullDto(event);
    }

    @Override
    public List<ParticipationRequestDto> getEventAllParticipationRequests(Long eventId, Long userId) {
        Event event = checkAndGetEventByIdAndInitiatorId(eventId, userId);
        return requestClient.getByStatus(event.getId(), RequestStatus.PENDING);
    }

    @Override
    public Event checkAndGetEventByIdAndInitiatorId(Long eventId, Long initiatorId) {
        return eventRepository.findByIdAndInitiatorId(eventId, initiatorId)
                .orElseThrow(() -> new NotFoundException(String.format("On event operations - " +
                        "Event doesn't exist with id %s or not available for User with id %s: ", eventId, initiatorId)));
    }

    @Override
    public EventRequestStatusUpdateResultDto changeEventState(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequestDto requestStatusUpdateRequest) {
        Event event = checkAndGetEventByIdAndInitiatorId(eventId, userId);
        Long participantsLimit = event.getParticipantLimit();

        List<ParticipationRequestDto> confirmedRequests = requestClient.getByStatus(eventId,
                RequestStatus.CONFIRMED);
        log.info("confirmedRequests: {}", confirmedRequests);

        List<ParticipationRequestDto> requestToChangeStatus = requestClient.getByIds(requestStatusUpdateRequest.getRequestIds());
        List<Long> idsToChangeStatus = requestToChangeStatus.stream()
                .map(ParticipationRequestDto::getId)
                .toList();
        log.info("idsToChangeStatus: {}", idsToChangeStatus);
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            log.info("Заявки подтверждать не требуется");
            return null;
        }

        log.info("Заявки:  Лимит: {}, подтвержденных заявок {}, запрошенных заявок {}, разница между ними: {}", participantsLimit,
                confirmedRequests.size(), requestStatusUpdateRequest.getRequestIds().size(), (participantsLimit
                        - confirmedRequests.size() - requestStatusUpdateRequest.getRequestIds().size()));

        if (requestStatusUpdateRequest.getStatus().equals(RequestStatus.CONFIRMED)) {
            log.info("меняем статус заявок для статуса: {}", RequestStatus.CONFIRMED);
            if ((participantsLimit - (confirmedRequests.size()) - requestStatusUpdateRequest.getRequestIds().size()) >= 0) {
                List<ParticipationRequestDto> requestUpdated = requestClient.updateStatus(
                        RequestStatus.CONFIRMED, idsToChangeStatus);
                return new EventRequestStatusUpdateResultDto(requestUpdated, null);
            } else {
                throw new ConflictDataException("слишком много участников. Лимит: " + participantsLimit +
                        ", уже подтвержденных заявок: " + confirmedRequests.size() + ", а заявок на одобрение: " +
                        idsToChangeStatus.size() +
                        ". Разница между ними: " + (participantsLimit - confirmedRequests.size() -
                        idsToChangeStatus.size()));
            }
        } else if (requestStatusUpdateRequest.getStatus().equals(RequestStatus.REJECTED)) {
            log.info("меняем статус заявок для статуса: {}", RequestStatus.REJECTED);

            for (ParticipationRequestDto request : requestToChangeStatus) {
                if (request.getStatus() == RequestStatus.CONFIRMED) {
                    throw new ConflictDataException("Заявка" + request.getStatus() + "уже подтверждена.");
                }
            }

            List<ParticipationRequestDto> requestUpdated = requestClient.updateStatus(
                    RequestStatus.REJECTED, idsToChangeStatus);
            return new EventRequestStatusUpdateResultDto(null, requestUpdated);
        }
        return null;
    }
}