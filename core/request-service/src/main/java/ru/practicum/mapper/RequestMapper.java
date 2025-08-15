package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.RequestDto;
import ru.practicum.model.Request;

import java.util.List;

@Mapper
public interface RequestMapper {
    RequestMapper INSTANCE = Mappers.getMapper(RequestMapper.class);

    @Mapping(source = "eventId", target = "event")
    @Mapping(source = "requesterId", target = "requester")
    @Mapping(source = "created", target = "created", dateFormat = "yyyy-MM-dd HH:mm:ss")
    RequestDto mapToRequestDto(Request request);

    List<RequestDto> mapListRequests(List<Request> requests);

    @Mapping(source = "request.event.id", target = "event")
    @Mapping(source = "request.requester.id", target = "requester")
    ParticipationRequestDto toParticipationRequestDto(Request request);
}
