package ru.practicum.events.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.NewEventDto;
import ru.practicum.events.dto.UpdateEventUserDto;
import ru.practicum.events.service.EventService;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class EventPrivateController {

    private final EventService eventService;
    private final RequestService requestService;

    @PostMapping
    public ResponseEntity<EventFullDto> createEvent(@PathVariable Integer userId,
                                                    @Valid @RequestBody NewEventDto eventDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(userId, eventDto));
    }

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getEventsByUser(@PathVariable Integer userId,
                                                               @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                               @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return ResponseEntity.ok().body(eventService.getEventsByUser(userId, from, size));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getFullEventInformation(@PathVariable Integer userId,
                                                                @PathVariable Integer eventId) {
        return ResponseEntity.ok().body(eventService.getFullEventInformation(userId, eventId));
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEventByUser(@PathVariable Integer userId,
                                                          @PathVariable Integer eventId,
                                                          @Valid @RequestBody UpdateEventUserDto updateEventUserDto) {
        return ResponseEntity.ok().body(eventService.updateEventByUser(userId, eventId, updateEventUserDto));
    }

    @GetMapping("/{eventId}/requests")
    public ResponseEntity<List<RequestDto>> getRequestsByUserOfEvent(@PathVariable Integer userId,
                                                                     @PathVariable Integer eventId) {
        return ResponseEntity.ok().body(requestService.getRequestByUserOfEvent(userId, eventId));
    }

    @PatchMapping("/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> updateRequests(@PathVariable Integer userId,
                                                                         @PathVariable Integer eventId,
                                                                         @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        return ResponseEntity.ok().body(requestService.updateRequests(userId, eventId, updateRequest));
    }

    @PostMapping("/{eventId}/like")
    public ResponseEntity<Long> addLike(@PathVariable(name = "eventId") Integer eventId,
                                        @PathVariable(name = "userId") Integer userId) {
        return ResponseEntity.ok().body(eventService.addLike(userId, eventId));
    }

    @DeleteMapping("/{eventId}/like")
    public ResponseEntity<Long> removeLike(@PathVariable(name = "eventId") Integer eventId,
                                           @PathVariable(name = "userId") Integer userId) {
        return ResponseEntity.status(HttpStatus.GONE).body(eventService.removeLike(userId, eventId));
    }

    @GetMapping("/like")
    public ResponseEntity<List<EventShortDto>> getAllLikedEvents(@PathVariable Integer userId) {
        return ResponseEntity.ok().body(eventService.getAllLikedEvents(userId));
    }
}


