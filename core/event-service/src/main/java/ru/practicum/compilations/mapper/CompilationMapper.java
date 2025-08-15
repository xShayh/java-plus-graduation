package ru.practicum.compilations.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.compilations.model.Compilation;
import ru.practicum.dto.compilations.CompilationDto;
import ru.practicum.dto.compilations.NewCompilationDto;
import ru.practicum.events.mapper.EventMapper;
import ru.practicum.events.model.Event;

import java.util.Set;

@Mapper(componentModel = "spring", uses = {EventMapper.class})
public interface CompilationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", source = "events")
    @Mapping(target = "pinned", expression = "java(Boolean.TRUE.equals(dto.getPinned()))")
    Compilation toCompilation(NewCompilationDto dto, Set<Event> events);

    @Mapping(target = "events", source = "events")
    CompilationDto toCompilationDto(Compilation compilation);
}
