package ru.practicum.compilations.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.compilations.mapper.CompilationMapper;
import ru.practicum.compilations.model.Compilation;
import ru.practicum.compilations.repository.CompilationRepository;
import ru.practicum.dto.compilations.CompilationDto;
import ru.practicum.dto.compilations.NewCompilationDto;
import ru.practicum.dto.compilations.UpdateCompilationRequest;
import ru.practicum.events.model.Event;
import ru.practicum.events.repository.EventRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        log.info("Creating a new compilation with title: {}", newCompilationDto.getTitle());
        Set<Event> eventSet = new HashSet<>();
        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findAllByIdIn(newCompilationDto.getEvents());
            if (events.size() != newCompilationDto.getEvents().size()) {
                throw new EntityNotFoundException("Some events were not found.");
            }
            eventSet.addAll(events);
        }
        Compilation compilation = compilationMapper.toCompilation(newCompilationDto, eventSet);
        Compilation savedCompilation = compilationRepository.save(compilation);
        CompilationDto compilationDto = compilationMapper.toCompilationDto(savedCompilation);
        log.info("Successfully created compilation with ID: {}", compilationDto.getId());
        return compilationDto;
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        List<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll();
        } else {
            compilations = compilationRepository.findAllByPinned(pinned);
        }
        int toIndex = Math.min(from + size, compilations.size());
        compilations = compilations.subList(from, toIndex);
        return compilations.stream()
                .map(compilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new EntityNotFoundException("Compilation with id=" + compId + " was not found"));
        return compilationMapper.toCompilationDto(compilation);
    }

    @Override
    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new EntityNotFoundException("Compilation with id=" + compId + " was not found");
        }
        compilationRepository.deleteById(compId);
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        log.info("Attempting to update compilation with ID: {}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new EntityNotFoundException("Compilation with id=" + compId + " was not found"));

        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }

        Set<Event> eventSet = new HashSet<>();
        if (updateCompilationRequest.getEvents() != null && !updateCompilationRequest.getEvents().isEmpty()) {
            List<Event> events = eventRepository.findAllByIdIn(updateCompilationRequest.getEvents());
            if (events.size() != updateCompilationRequest.getEvents().size()) {
                throw new EntityNotFoundException("Some events were not found.");
            }
            eventSet.addAll(events);
        }
        compilation.setEvents(eventSet);

        Compilation savedCompilation = compilationRepository.save(compilation);
        CompilationDto compilationDto = compilationMapper.toCompilationDto(savedCompilation);

        log.info("Successfully updated compilation with ID: {}", compId);
        return compilationDto;
    }
}
