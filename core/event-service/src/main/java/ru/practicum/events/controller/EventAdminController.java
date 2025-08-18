package ru.practicum.events.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.events.EventAdminParams;
import ru.practicum.dto.events.EventFullDto;
import ru.practicum.dto.events.UpdateEventAdminRequestDto;
import ru.practicum.events.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Validated
public class EventAdminController {
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventFullDto>> adminGetEvents(@RequestParam(required = false) List<Long> users,
                                                             @RequestParam(required = false) List<String> states,
                                                             @RequestParam(required = false) List<Long> categories,
                                                             @RequestParam(required = false)
                                                             @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                             @RequestParam(required = false)
                                                             @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                             @RequestParam(defaultValue = "0") Integer from,
                                                             @RequestParam(defaultValue = "10") Integer size) {
        EventAdminParams eventAdminParams = new EventAdminParams(users, states, categories, rangeStart, rangeEnd, from, size);
        return ResponseEntity.ok().body(eventService.adminGetEvents(eventAdminParams));
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> adminUpdateEvents(@PathVariable Long eventId,
                                                          @Valid @RequestBody UpdateEventAdminRequestDto updateEventAdminRequestDto) {
        return ResponseEntity.ok().body(eventService.updateAdminEvent(eventId, updateEventAdminRequestDto));
    }

    @GetMapping("/{userId}/like")
    public ResponseEntity<List<EventFullDto>> adminGetEventsLikedByUser(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok().body(eventService.adminGetEventsLikedByUser(userId));
    }
}
