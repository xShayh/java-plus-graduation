package ru.practicum.request.service;


import ru.practicum.request.dto.RequestDto;

import java.util.List;

public interface RequestService {
    List<RequestDto> getRequests(Integer userId);

    RequestDto createRequest(Integer userId, Integer eventId);

    RequestDto cancelRequest(Integer userId, Integer requestId);
}
