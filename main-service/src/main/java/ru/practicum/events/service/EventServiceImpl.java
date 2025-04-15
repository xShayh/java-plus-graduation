package ru.practicum.events.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.categories.repository.CategoryRepository;
import ru.practicum.categories.model.Category;
import ru.practicum.events.dto.*;
import ru.practicum.events.mapper.EventMapper;
import ru.practicum.events.model.Event;
import ru.practicum.events.params.EventAdminParams;
import ru.practicum.events.params.EventPublicParam;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.events.repository.LocationRepository;
import ru.practicum.events.util.AdminEventState;
import ru.practicum.events.util.EventState;
import ru.practicum.events.util.StateActionForUser;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;

    @Override
    public List<EventFullDto> adminGetEvents(EventAdminParams eventParams) {
        Pageable page = PageRequest.of(eventParams.getFrom(), eventParams.getSize());

        List<EventFullDto> events = eventMapper.toEventFullDto(eventRepository.findAdminEvents(
                eventParams.getUsersIds(),
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
        Event event = getEvent(eventId);
        updateEventState(event, updateEventAdminRequest);
        if (updateEventAdminRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }
        if (updateEventAdminRequest.getCategory() != null) {
            Category category = getCategory(updateEventAdminRequest.getCategory().getId());
            event.setCategory(category);
        }
        if (updateEventAdminRequest.getDescription() != null) {
            event.setDescription(updateEventAdminRequest.getDescription());
        }
        if (updateEventAdminRequest.getEventDate() != null) {
            event.setEventDate(updateEventAdminRequest.getEventDate());
        }
        if (updateEventAdminRequest.getLocation() != null) {
            event.setLocation(locationRepository.save(updateEventAdminRequest.getLocation()));
        }
        if (updateEventAdminRequest.getPaid() != null) {
            event.setPaid(updateEventAdminRequest.getPaid());
        }
        if (updateEventAdminRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }
        if (updateEventAdminRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }
        if (updateEventAdminRequest.getTitle() != null) {
            event.setTitle(updateEventAdminRequest.getTitle());
        }
        log.info("Event with ID={} was updated", eventId);
        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> getEventsByUser(Integer userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return eventRepository.findAllByInitiatorId(userId, pageable).stream()
                .map(eventMapper::toEventShortDto)
                .toList();

    }

    @Override
    public EventFullDto createEvent(Integer userId, NewEventDto newEventDto) {
        Category category = getCategory(newEventDto.getCategory().getId());
        User user = getUser(userId);
        Event event = eventMapper.toEvent(newEventDto, category, user);
        event.setLocation(locationRepository.save(newEventDto.getLocation()));
        if (newEventDto.getPaid() == null) {
            event.setPaid(false);
        }
        if (newEventDto.getParticipantLimit() == null) {
            event.setParticipantLimit(0);
        }
        if (newEventDto.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }
        log.info("Event was created");
        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto getFullEventInformation(Integer userId, Integer eventId) {
        getEvent(eventId);
        return eventMapper.toEventFullDto(eventRepository.findByIdAndInitiatorId(eventId, userId));
    }

    @Override
    public EventFullDto updateEventByUser(Integer userId, Integer eventId, UpdateEventUserDto updateEventUserDto) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId);
        if (event.getPublishedOn() != null) {
            throw new InvalidParameterException("Event is already published");
        }
        if (updateEventUserDto.getEventDate() != null && !updateEventUserDto.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            throw new InvalidParameterException("Event date should be in 2+ hours after now");
        }
        if (updateEventUserDto.getAnnotation() != null) {
            event.setAnnotation(updateEventUserDto.getAnnotation());
        }
        if (updateEventUserDto.getCategory() != null) {
            Category category = getCategory(updateEventUserDto.getCategory().getId());
            event.setCategory(category);
        }
        if (updateEventUserDto.getDescription() != null) {
            event.setDescription(updateEventUserDto.getDescription());
        }
        if (updateEventUserDto.getEventDate() != null) {
            event.setEventDate(updateEventUserDto.getEventDate());
        }
        if (updateEventUserDto.getLocation() != null) {
            event.setLocation(updateEventUserDto.getLocation());
        }
        if (updateEventUserDto.getPaid() != null) {
            event.setPaid(updateEventUserDto.getPaid());
        }
        if (updateEventUserDto.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserDto.getParticipantLimit());
        }
        if (updateEventUserDto.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserDto.getRequestModeration());
        }
        if (updateEventUserDto.getTitle() != null) {
            event.setTitle(updateEventUserDto.getTitle());
        }

        if (updateEventUserDto.getStateAction() != null) {
            if (updateEventUserDto.getStateAction().equals(StateActionForUser.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            } else {
                event.setState(EventState.CANCELED);
            }
        }
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
            throw new NotFoundException("End date should be before start date");
        }
        List<Event> events = eventRepository.findPublicEvents(
                eventPublicParam.getText(),
                eventPublicParam.getCategoriesIds(),
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
        return events.stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }

    @Override
    public EventFullDto publicGetEvent(Integer eventId) {
        Event event = getEvent(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException(String.format("Event with ID=%d was not published", eventId));
        }
        return eventMapper.toEventFullDto(event);
    }

    private void updateEventState(Event event, UpdateEventAdminRequest updateEventAdminRequest) {
        if (updateEventAdminRequest.getStateAction() != null) {
            AdminEventState action = updateEventAdminRequest.getStateAction();

            if (action == AdminEventState.PUBLISH_EVENT && event.getState() != EventState.PENDING) {
                throw new DataIntegrityViolationException("Event should be in PENDING state");
            }
            if (action == AdminEventState.REJECT_EVENT && event.getState() == EventState.PUBLISHED) {
                throw new DataIntegrityViolationException("Event can be rejected only in PENDING state");
            }
            if (action == AdminEventState.PUBLISH_EVENT) {
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (action == AdminEventState.REJECT_EVENT) {
                event.setState(EventState.CANCELED);
            }
        }
    }

    private Category getCategory(Integer categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(String.format("Category with ID=%d was not found", categoryId)));
    }

    private Event getEvent(Integer eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with ID=%d was not found", eventId)));
    }

    private User getUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with ID=%d was not found", userId)));
    }
}

