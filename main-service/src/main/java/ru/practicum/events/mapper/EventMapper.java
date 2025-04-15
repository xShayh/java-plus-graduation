package ru.practicum.events.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.categories.model.Category;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.dto.NewEventDto;
import ru.practicum.events.model.Event;
import ru.practicum.user.model.User;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventMapper {

    EventFullDto toEventFullDto(Event event);

    @Mapping(source = "newEventDto.id", target = "id")
    @Mapping(source = "category", target = "category")
    @Mapping(source = "initiator", target = "initiator")
    Event toEvent(NewEventDto newEventDto, Category category, User initiator);

    EventShortDto toEventShortDto(Event event);

    List<EventFullDto> toEventFullDto(List<Event> eventParams);
}
