package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.RequestCountDto;
import ru.practicum.dto.request.RequestDto;
import ru.practicum.model.Request;
import ru.practicum.model.RequestCount;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RequestMapper {
    @Mapping(source = "eventId", target = "event")
    @Mapping(source = "requesterId", target = "requester")
    @Mapping(source = "created", target = "created", dateFormat = "yyyy-MM-dd HH:mm:ss")
    RequestDto mapToRequestDto(Request request);

    List<RequestDto> toParticipationRequestDto(List<Request> requests);

    @Mapping(source = "request.eventId", target = "event")
    @Mapping(source = "request.requesterId", target = "requester")
    ParticipationRequestDto toParticipationRequestDto(Request request);

    RequestCountDto toParticipationRequestDto(RequestCount requestCount);
}
