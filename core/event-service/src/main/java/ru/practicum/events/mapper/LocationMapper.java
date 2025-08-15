package ru.practicum.events.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dto.events.LocationDto;
import ru.practicum.events.model.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationDto toLocationDto(Location location);

    Location toLocation(LocationDto locationDto);
}
