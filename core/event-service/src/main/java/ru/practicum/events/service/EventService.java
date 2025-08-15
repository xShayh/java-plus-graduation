package ru.practicum.events.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.events.*;
import ru.practicum.dto.user.UserShortDto;

import java.util.List;

@Transactional(readOnly = true)
public interface EventService {

    List<EventShortDto> getEventsByUser(Long userId, Integer from, Integer size);

    @Transactional
    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getFullEventInformation(Long userId, Long eventId);

    @Transactional
    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserDto updateEventUserDto);

    @Transactional
    EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventFullDto> adminGetEvents(EventAdminParams eventParams);

    List<EventShortDto> publicGetEvents(EventPublicParam eventPublicParam);

    EventFullDto publicGetEvent(Long eventId);

    @Transactional
    Long addLike(Long userId, Long eventId);

    @Transactional
    Long removeLike(Long userId, Long eventId);

    List<UserShortDto> getLikedUsers(Long eventId);

    List<EventFullDto> adminGetEventsLikedByUser(Long userId);

    List<EventShortDto> getAllLikedEvents(Long userId);
}
