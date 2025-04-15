package ru.practicum.events.service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.events.dto.*;
import ru.practicum.events.params.EventAdminParams;
import ru.practicum.events.params.EventPublicParam;

import java.util.List;

@Transactional(readOnly = true)
public interface EventService {

    List<EventFullDto> adminGetEvents(EventAdminParams eventParams);

    @Transactional
    EventFullDto updateAdminEvent(Integer eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventShortDto> getEventsByUser(Integer userId, Integer from, Integer size);

    @Transactional
    EventFullDto createEvent(Integer userId, NewEventDto newEventDto);

    EventFullDto getFullEventInformation(Integer userId, Integer eventId);

    @Transactional
    EventFullDto updateEventByUser(Integer userId, Integer eventId, UpdateEventUserDto updateEventUserDto);

    List<EventShortDto> publicGetEvents(EventPublicParam eventPublicParam);

    EventFullDto publicGetEvent(Integer eventId);
}
