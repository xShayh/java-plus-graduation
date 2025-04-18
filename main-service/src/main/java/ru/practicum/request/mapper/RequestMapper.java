package ru.practicum.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.model.Request;

import java.util.List;

@Mapper
public interface RequestMapper {
    RequestMapper INSTANCE = Mappers.getMapper(RequestMapper.class);

    @Mapping(source = "event.id", target = "event")
    @Mapping(source = "requester.id", target = "requester")
    @Mapping(source = "created", target = "created", dateFormat = "yyyy-MM-dd HH:mm:ss")
    RequestDto mapToRequestDto(Request request);

    List<RequestDto> mapListRequests(List<Request> requests);
}
