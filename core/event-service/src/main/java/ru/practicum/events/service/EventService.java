package ru.practicum.events.service;

import ru.practicum.dto.events.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.events.model.Event;

import java.util.List;

public interface EventService {

    List<EventShortDto> getEventsByUser(Long userId, Integer from, Integer size);

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getFullEventInformation(Long userId, Long eventId);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserDto updateEventUserDto);

    EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequestDto updateEventAdminRequest);

    List<EventFullDto> adminGetEvents(EventAdminParams eventParams);

    List<EventShortDto> publicGetEvents(EventPublicParam eventPublicParam);

    EventFullDto publicGetEvent(Long eventId, Long userId);

    void addLike(Long userId, Long eventId);

    Long removeLike(Long userId, Long eventId);

    List<UserShortDto> getLikedUsers(Long eventId);

    List<EventFullDto> adminGetEventsLikedByUser(Long userId);

    List<EventShortDto> getAllLikedEvents(Long userId);

    List<EventFullDto> getByLocation(Long locationId);

    Event getEvent(Long eventId);

    EventFullDto getEventById(Long eventId);

    List<ParticipationRequestDto> getEventAllParticipationRequests(Long eventId, Long userId);

    Event checkAndGetEventByIdAndInitiatorId(Long eventId, Long initiatorId);

    EventRequestStatusUpdateResultDto changeEventState(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequestDto requestStatusUpdateRequest);

    List<EventShortDto> getRecommendations(Long userId, Integer from, Integer size);

}
