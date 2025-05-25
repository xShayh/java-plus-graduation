package ru.practicum.events.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.EventPublicParam;
import ru.practicum.events.service.EventService;
import ru.practicum.events.util.SortState;
import ru.practicum.user.dto.UserShortDto;
import stat.StatClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventPublicController {

    private final EventService eventService;
    private final StatClient statClient;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> publicGetEvents(@RequestParam(required = false) String text,
                                                               @RequestParam(required = false) List<Integer> categories,
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
    public ResponseEntity<EventFullDto> publicGetEvent(@PathVariable Integer eventId, HttpServletRequest request) {
        saveHit(request);
        return ResponseEntity.ok().body(eventService.publicGetEvent(eventId));
    }

    @GetMapping("/{eventId}/likes")
    public List<UserShortDto> publicGetLikedUsers(@PathVariable("eventId") Integer eventId, HttpServletRequest request) {
        saveHit(request);
        return eventService.getLikedUsers(eventId);
    }

    private void saveHit(HttpServletRequest request) {
        EndpointHitDto hitDto = new EndpointHitDto();
        hitDto.setApp("main-service");
        hitDto.setUri(request.getRequestURI());
        hitDto.setIp(request.getRemoteAddr());
        hitDto.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        ResponseEntity<Object> response = statClient.saveHit(hitDto);
        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
            System.out.println("Hit saved successfully for URI: " + request.getRequestURI());
        } else {
            System.err.println("Failed to save hit: " + response.getStatusCode());
        }
    }
}
