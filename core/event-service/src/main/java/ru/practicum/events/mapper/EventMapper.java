package ru.practicum.events.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;
import ru.practicum.categories.mapper.CategoryMapper;
import ru.practicum.categories.model.Category;
import ru.practicum.dto.events.*;
import ru.practicum.dto.request.RequestCountDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.events.model.Event;
import ru.practicum.grpc.stats.request.RecommendedEventProto;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EventMapper {
    private final CategoryMapper categoryMapper;
    private final LocationMapper locationMapper;

    @Mapping(target = "rating", ignore = true)
    public EventFullDto toEventFullDto(Event event) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryMapper.toCategoryDto(event.getCategory()))
                .eventDate(event.getEventDate())
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(event.getViews())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .location(locationMapper.toLocationDto(event.getLocation()))
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .confirmedRequests(event.getConfirmedRequests())
                .build();
    }

    @Mapping(target = "rating", ignore = true)
    public EventFullDto toEventFullDto(Event event, UserShortDto userShortDto) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryMapper.toCategoryDto(event.getCategory()))
                .eventDate(event.getEventDate())
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(event.getViews())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .location(locationMapper.toLocationDto(event.getLocation()))
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .initiator(userShortDto)
                .confirmedRequests(event.getConfirmedRequests())
                .build();
    }

    @Mapping(target = "rating", ignore = true)
    public Event toEvent(NewEventDto newEventDto, Category category, Long initiatorId) {
        return new Event(
                0L,
                newEventDto.getAnnotation(),
                category,
                0L,
                LocalDateTime.now(),
                newEventDto.getDescription(),
                newEventDto.getEventDate(),
                initiatorId,
                locationMapper.toLocation(newEventDto.getLocation()),
                newEventDto.getPaid(),
                newEventDto.getParticipantLimit(),
                null,
                newEventDto.getRequestModeration(),
                EventState.PENDING,
                newEventDto.getTitle(),
                0L
        );
    }

    public EventShortDto toEventShortDto(Event event, UserShortDto initiator) {
        return new EventShortDto(
                event.getId(),
                event.getAnnotation(),
                categoryMapper.toCategoryDto(event.getCategory()),
                event.getConfirmedRequests(),
                event.getEventDate(),
                initiator,
                event.getPaid(),
                event.getTitle(),
                event.getViews()
        );
    }

    public List<EventFullDto> toEventFullDto(List<Event> adminEvents) {
        return adminEvents.stream().map(this::toEventFullDto).toList();
    }

    public RecommendedEventDto map(RecommendedEventProto proto) {
        if (proto == null) {
            return null;
        }

        return RecommendedEventDto.builder()
                .eventId(proto.getEventId())
                .score(proto.getScore())
                .build();
    }
}
