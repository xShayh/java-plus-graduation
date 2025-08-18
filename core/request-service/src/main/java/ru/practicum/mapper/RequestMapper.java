package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.RequestCountDto;
import ru.practicum.model.Request;
import ru.practicum.model.RequestCount;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RequestMapper {
    @Mapping(source = "eventId", target = "event")
    @Mapping(source = "requesterId", target = "requester")
    @Mapping(source = "created", target = "created", dateFormat = "yyyy-MM-dd HH:mm:ss")
    ParticipationRequestDto toParticipationRequestDto(Request request);

    List<ParticipationRequestDto> toParticipationRequestDto(List<Request> requests);

    RequestCountDto toParticipationRequestDto(RequestCount requestCount);
}
