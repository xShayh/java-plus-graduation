package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.service.RequestService;

import java.util.List;

@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class RequestController {
    private final RequestService requestService;

    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> getRequests(@PathVariable Long userId) {
        log.info("Получить запросы по userId --> {}", userId);
        return requestService.getRequests(userId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{userId}/requests")
    public ParticipationRequestDto createRequest(@PathVariable Long userId,
                                    @RequestParam Long eventId) {
        log.info("Создать запрос userId --> {}, eventId --> {}", userId, eventId);
        return requestService.createRequest(userId, eventId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    ParticipationRequestDto cancelRequest(@PathVariable Long userId,
                             @PathVariable Long requestId) {
        log.info("Отменить запрос по userId --> {}, requestId --> {}", userId, requestId);
        return requestService.cancelRequest(userId, requestId);
    }
}
