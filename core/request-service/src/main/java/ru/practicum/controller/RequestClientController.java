package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.client.RequestClient;
import ru.practicum.dto.request.RequestCountDto;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.RequestStatus;
import ru.practicum.service.RequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/internal/api/requests")
@RequiredArgsConstructor
public class RequestClientController implements RequestClient {
    private final RequestService requestService;

    @Override
    public List<ParticipationRequestDto> getByStatus(Long eventId, RequestStatus status) {
        return requestService.findAllByEventIdAndStatus(eventId, status);
    }

    @Override
    public List<ParticipationRequestDto> getByIds(List<Long> ids) {
        return requestService.getByIds(ids);
    }

    @Override
    public List<RequestCountDto> getConfirmedCount(List<Long> ids) {
        return requestService.getConfirmedCount(ids);
    }

    @Override
    public List<ParticipationRequestDto> updateStatus(RequestStatus status, List<Long> ids) {
        return requestService.updateStatus(status, ids);
    }
}
