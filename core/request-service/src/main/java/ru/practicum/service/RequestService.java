package ru.practicum.service;

import ru.practicum.dto.request.*;

import java.util.List;

public interface RequestService {
    List<ParticipationRequestDto> getRequests(Long userId);

    ParticipationRequestDto createRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getRequestByUserOfEvent(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest requestStatusUpdateRequest);

    List<ParticipationRequestDto> findAllByEventIdAndStatus(Long eventId, RequestStatus status);

    List<ParticipationRequestDto> getByIds(List<Long> ids);

    List<RequestCountDto> getConfirmedCount(List<Long> ids);

    List<ParticipationRequestDto> updateStatus(RequestStatus status, List<Long> ids);
}
