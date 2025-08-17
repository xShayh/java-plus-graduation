package ru.practicum.service;

import ru.practicum.dto.request.*;

import java.util.List;

public interface RequestService {
    List<RequestDto> getRequests(Long userId);

    RequestDto createRequest(Long userId, Long eventId);

    RequestDto cancelRequest(Long userId, Long requestId);

    List<RequestDto> getRequestByUserOfEvent(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest requestStatusUpdateRequest);

    List<ParticipationRequestDto> findAllByEventIdAndStatus(Long eventId, RequestStatus status);

    List<ParticipationRequestDto> getByIds(List<Long> ids);

    List<RequestCountDto> getConfirmedCount(List<Long> ids);

    List<ParticipationRequestDto> updateStatus(RequestStatus status, List<Long> ids);
}
