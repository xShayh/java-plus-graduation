package ru.practicum.events.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.events.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.events.service.EventService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EventPrivateController {

    private final EventService eventService;

    @PostMapping(path = "/users/{userId}/events")
    public ResponseEntity<EventFullDto> createEvent(@PathVariable Long userId,
                                                    @Valid @RequestBody NewEventDto eventDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(userId, eventDto));
    }

    @GetMapping(path = "/users/{userId}/events")
    public ResponseEntity<List<EventShortDto>> getEventsByUser(@PathVariable Long userId,
                                                               @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                               @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return ResponseEntity.ok().body(eventService.getEventsByUser(userId, from, size));
    }

    @GetMapping(path = "/users/{userId}/events/{eventId}")
    public ResponseEntity<EventFullDto> getFullEventInformation(@PathVariable Long userId,
                                                                @PathVariable Long eventId) {
        return ResponseEntity.ok().body(eventService.getFullEventInformation(userId, eventId));
    }

    @PatchMapping(path = "/users/{userId}/events/{eventId}")
    public ResponseEntity<EventFullDto> updateEventByUser(@PathVariable Long userId,
                                                          @PathVariable Long eventId,
                                                          @Valid @RequestBody UpdateEventUserDto updateEventUserDto) {
        return ResponseEntity.ok().body(eventService.updateEventByUser(userId, eventId, updateEventUserDto));
    }

    @PostMapping("/{eventId}/like")
    public ResponseEntity<Long> addLike(@PathVariable(name = "eventId") Long eventId,
                                        @PathVariable(name = "userId") Long userId) {
        return ResponseEntity.ok().body(eventService.addLike(userId, eventId));
    }

    @DeleteMapping("/{eventId}/like")
    public ResponseEntity<Long> removeLike(@PathVariable(name = "eventId") Long eventId,
                                           @PathVariable(name = "userId") Long userId) {
        return ResponseEntity.status(HttpStatus.GONE).body(eventService.removeLike(userId, eventId));
    }

    @GetMapping("/like")
    public ResponseEntity<List<EventShortDto>> getAllLikedEvents(@PathVariable Long userId) {
        return ResponseEntity.ok().body(eventService.getAllLikedEvents(userId));
    }

    @GetMapping(path = "/users/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getParticipationRequests(@PathVariable("userId") Long userId,
                                                                  @PathVariable("eventId") Long eventId) {
        return eventService.getEventAllParticipationRequests(eventId, userId);
    }

    @PatchMapping(path = "/users/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResultDto updatedEventRequestStatus(@PathVariable("userId") Long userId,
                                                                       @PathVariable("eventId") Long eventId,
                                                                       @RequestBody EventRequestStatusUpdateRequestDto request) {
        return eventService.changeEventState(userId, eventId, request);
    }
}


