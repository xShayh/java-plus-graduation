package ru.practicum.events.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.events.dto.*;
import ru.practicum.events.dto.EventAdminParams;
import ru.practicum.events.dto.EventPublicParam;
import ru.practicum.user.dto.UserShortDto;

import java.util.List;

@Transactional(readOnly = true)
public interface EventService {

    List<EventShortDto> getEventsByUser(Integer userId, Integer from, Integer size);

    @Transactional
    EventFullDto createEvent(Integer userId, NewEventDto newEventDto);

    EventFullDto getFullEventInformation(Integer userId, Integer eventId);

    @Transactional
    EventFullDto updateEventByUser(Integer userId, Integer eventId, UpdateEventUserDto updateEventUserDto);

    @Transactional
    EventFullDto updateAdminEvent(Integer eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventFullDto> adminGetEvents(EventAdminParams eventParams);

    List<EventShortDto> publicGetEvents(EventPublicParam eventPublicParam);

    EventFullDto publicGetEvent(Integer eventId);

    @Transactional
    Long addLike(Integer userId, Integer eventId);

    @Transactional
    Long removeLike(Integer userId, Integer eventId);

    List<UserShortDto> getLikedUsers(Integer eventId);

    List<EventFullDto> adminGetEventsLikedByUser(Integer userId);

    List<EventShortDto> getAllLikedEvents(Integer userId);
}
