package ru.practicum.events.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.NewEventDto;
import ru.practicum.events.dto.UpdateEventUserDto;
import ru.practicum.events.service.EventService;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class EventPrivateController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getEventsByUser(@PathVariable Integer userId,
                                                               @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                               @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return ResponseEntity.ok().body(eventService.getEventsByUser(userId, from, size));
    }

    @PostMapping
    public ResponseEntity<EventFullDto> createEvent(@PathVariable Integer userId,
                                                    @RequestBody NewEventDto eventDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(userId, eventDto));
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
}
