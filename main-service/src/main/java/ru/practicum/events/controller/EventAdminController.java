package ru.practicum.events.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.UpdateEventAdminRequest;
import ru.practicum.events.dto.EventAdminParams;
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
    public ResponseEntity<List<EventFullDto>> adminGetEvents(@RequestParam(required = false) List<Integer> users,
                                                             @RequestParam(required = false) List<String> states,
                                                             @RequestParam(required = false) List<Integer> categories,
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
    public ResponseEntity<EventFullDto> adminUpdateEvents(@PathVariable("eventId") Integer eventId,
                                                          @Valid @RequestBody UpdateEventAdminRequest updateAdminEvent) {
        return ResponseEntity.ok().body(eventService.updateAdminEvent(eventId, updateAdminEvent));
    }
}
