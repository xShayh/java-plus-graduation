package ru.practicum.events.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.client.EventClient;
import ru.practicum.dto.events.EventFullDto;
import ru.practicum.events.service.EventService;

import java.util.List;

@RestController
@RequestMapping(path = "/internal/api/events")
@RequiredArgsConstructor
public class EventClientController implements EventClient {
    private final EventService eventService;
    @Override
    public EventFullDto getById(Long eventId) {
        return eventService.getEventById(eventId);
    }

    @Override
    public List<EventFullDto> getByLocation(Long locationId) {
        return eventService.getByLocation(locationId);
    }
}
