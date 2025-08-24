package ru.practicum.events.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.events.EventFullDto;
import ru.practicum.dto.events.EventPublicParam;
import ru.practicum.dto.events.EventShortDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.events.service.EventService;
import ru.practicum.dto.events.SortState;
import ru.practicum.stat.StatClientImpl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventPublicController {

    private final EventService eventService;
    private final StatClientImpl statClientImpl;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> publicGetEvents(@RequestParam(required = false) String text,
                                                               @RequestParam(required = false) List<Long> categories,
                                                               @RequestParam(required = false) Boolean paid,
                                                               @RequestParam(required = false)
                                                               @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                               @RequestParam(required = false)
                                                               @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                               @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                               @RequestParam(required = false) SortState sort,
                                                               @RequestParam(defaultValue = "0") Integer from,
                                                               @RequestParam(defaultValue = "10") Integer size,
                                                               HttpServletRequest request) {
        EventPublicParam eventPublicParam = new EventPublicParam(text, categories, paid,
                rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        saveHit(request);
        return ResponseEntity.ok().body(eventService.publicGetEvents(eventPublicParam));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> publicGetEvent(@PathVariable("eventId") Long eventId,
                                                       @RequestHeader("X-EWM-USER-ID") Long userId,
                                                       HttpServletRequest request) {
        saveHit(request);
        return ResponseEntity.ok().body(eventService.publicGetEvent(eventId, userId));
    }

    @GetMapping("/{eventId}/likes")
    public ResponseEntity<List<UserShortDto>> publicGetLikedUsers(@PathVariable("eventId") Long eventId, HttpServletRequest request) {
        saveHit(request);
        return ResponseEntity.ok(eventService.getLikedUsers(eventId));
    }

    private void saveHit(HttpServletRequest request) {
        EndpointHitDto hitDto = new EndpointHitDto();
        hitDto.setApp("main-service");
        hitDto.setUri(request.getRequestURI());
        hitDto.setIp(request.getRemoteAddr());
        hitDto.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}
