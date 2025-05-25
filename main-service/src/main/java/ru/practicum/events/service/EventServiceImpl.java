package ru.practicum.events.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.categories.repository.CategoryRepository;
import ru.practicum.categories.model.Category;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.events.dto.*;
import ru.practicum.events.mapper.EventMapper;
import ru.practicum.events.mapper.LocationMapper;
import ru.practicum.events.model.Event;
import ru.practicum.events.dto.EventAdminParams;
import ru.practicum.events.dto.EventPublicParam;
import ru.practicum.events.model.Like;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.events.repository.LikeRepository;
import ru.practicum.events.repository.LocationRepository;
import ru.practicum.events.util.AdminEventState;
import ru.practicum.events.util.EventState;
import ru.practicum.events.util.StateActionForUser;
import ru.practicum.exceptions.EventDateValidationException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import stat.StatClient;

import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final LikeRepository likeRepository;
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final StatClient statClient;
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
        List<EventFullDto> events = eventMapper.toEventFullDto(eventRepository.findAdminEvents(
                eventParams.getUsers(),
                eventParams.getStates(),
                eventParams.getCategories(),
                eventParams.getRangeStart(),
                eventParams.getRangeEnd(),
                page
        ));
        if (events.isEmpty()) {
            return List.of();
        }
        return events;
    }

    @Override
    public EventFullDto updateAdminEvent(Integer eventId, UpdateEventAdminRequest updateEventAdminRequest) {
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
        log.info("Event with ID={} was updated", eventId);
        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> getEventsByUser(Integer userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        log.info("Получение событий пользователем с ID={}", userId);
        return eventRepository.findAllByInitiatorId(userId, pageable).stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }

    @Override
    public EventFullDto createEvent(Integer userId, NewEventDto newEventDto) {
        log.info("Создание события пользователем с ID={}", userId);
        if (newEventDto.getEventDate() != null && !newEventDto.getEventDate()
                .isAfter(LocalDateTime.now().plusHours(2))) {
            throw new EventDateValidationException("Event date should be in 2+ hours after now");
        }
        Category category = getCategory(newEventDto.getCategory());
        User user = getUser(userId);
        Event event = eventMapper.toEvent(newEventDto, category, user);
        event.setLocation(locationRepository.save(locationMapper.toLocation(newEventDto.getLocation())));
        if (newEventDto.getPaid() == null) {
            event.setPaid(false);
        }
        if (newEventDto.getParticipantLimit() == null) {
            event.setParticipantLimit(0L);
        }
        if (newEventDto.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }
        log.info("Event was created");
        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto getFullEventInformation(Integer userId, Integer eventId) {
        log.info("Получение полной информации о событии с ID={}", eventId);
        return eventMapper.toEventFullDto(eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new RuntimeException(String.format("Event with ID=%d was not found ", eventId))));
    }

    @Override
    public EventFullDto updateEventByUser(Integer userId, Integer eventId, UpdateEventUserDto updateEventUserDto) {
        log.info("Обновление события с ID={} пользователем с ID={}", eventId, userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new RuntimeException(String.format("Event with ID=%d was not found ", eventId)));
        if (event.getPublishedOn() != null) {
            throw new InvalidParameterException("Event is already published");
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

        List<Event> events = eventRepository.findPublicEvents(
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
                        .map(eventMapper::toEventShortDto)
                        .toList();
                case VIEWS -> events.stream()
                        .sorted(Comparator.comparing(Event::getViews))
                        .map(eventMapper::toEventShortDto)
                        .toList();
            };
        }
        log.info("Вызов метода по добавлению просмотров");
        for (Event event : events) {
            addViews("/events/" + event.getId(), event);
        }
        return events.stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }

    @Override
    public EventFullDto publicGetEvent(Integer eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with ID=%d was not found", eventId)));
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException(String.format("Event with ID=%d was not published", eventId));
        }
        log.info("Вызов метода по добавлению просмотров");
        addViews("/events/" + event.getId(), event);
        return eventMapper.toEventFullDto(event);
    }

    @Override
    public Long addLike(Integer userId, Integer eventId) {
        User user = getUser(userId);
        Event event = getEvent(eventId);

        if (!likeRepository.existsByUserIdAndEventId(userId, eventId)) {
            Like like = new Like(user, event);
            likeRepository.save(like);
        }
        return likeRepository.countByEventId(eventId);
    }

    @Override
    public Long removeLike(Integer userId, Integer eventId) {
        if (likeRepository.existsByUserIdAndEventId(userId, eventId)) {
            likeRepository.deleteByUserIdAndEventId(userId, eventId);
        }
        return likeRepository.countByEventId(eventId);
    }

    @Override
    public List<UserShortDto> getLikedUsers(Integer eventId) {
        Event event = getEvent(eventId);
        addViews("/events/" + event.getId(), event);
        List<Like> likes = likeRepository.findAllByEventId(eventId);
        return likes.stream().map(like -> userMapper.toUserShortDto(like.getUser())).toList();
    }

    @Override
    public List<EventFullDto> adminGetEventsLikedByUser(Integer userId) {
        List<Integer> eventIds = getEventIdsLikedByUser(userId);
        return eventMapper.toEventFullDto(eventRepository.findAllById(eventIds));
    }

    @Override
    public List<EventShortDto> getAllLikedEvents(Integer userId) {
        List<Integer> eventIds = getEventIdsLikedByUser(userId);
        return eventRepository.findAllById(eventIds).stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }

    private List<Integer> getEventIdsLikedByUser(Integer userId) {
        User user = getUser(userId);
        List<Like> likes = likeRepository.findAllByUserId(userId);
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
        if (views.isEmpty()) {
            event.setViews(0L);
        } else {
            event.setViews((long) views.size());
        }
        log.info("Views was updated, views= {}", event.getViews());
    }

    private Category getCategory(Integer categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(String.format("Category with ID=%d was not found", categoryId)));
    }

    private User getUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
    }

    private Event getEvent(Integer eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException(String.format("Event with id=%d was not published", eventId));
        }
        return event;
    }
}





