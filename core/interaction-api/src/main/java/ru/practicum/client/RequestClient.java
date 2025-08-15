package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.RequestStatus;


import java.util.List;

@FeignClient(name = "request-service", path = "/internal/api/requests")
public interface RequestClient {
    @GetMapping("/search")
    List<ParticipationRequestDto> getByStatus(@RequestParam(name = "eventId") Long eventId,
                                              @RequestParam(name = "status") RequestStatus status);

    @GetMapping("/search/all")
    List<ParticipationRequestDto> getByIds(@RequestParam(name = "id") List<Long> ids);

    @PostMapping
    List<ParticipationRequestDto> updateStatus(
            @RequestParam(name = "status") RequestStatus status,
            @RequestBody List<Long> ids);
}
