package ru.practicum.ewm.main.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.compilation.Compilation;
import ru.practicum.ewm.main.compilation.CompilationRepository;
import ru.practicum.ewm.main.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.main.compilation.dto.CompilationDto;
import ru.practicum.ewm.main.compilation.dto.CompilationMapper;
import ru.practicum.ewm.main.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.main.event.EventRepository;
import ru.practicum.ewm.main.event.dto.EventMapper;
import ru.practicum.ewm.main.event.dto.EventShortDto;
import ru.practicum.ewm.main.event.model.Event;
import ru.practicum.ewm.main.exception.NotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDto saveCompilation(NewCompilationDto dto) {

        Compilation compilation = CompilationMapper.toCompilation(dto);

        Set<Event> events = new HashSet<>();

        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            events.addAll(eventRepository.findAllByIdIn(dto.getEvents()));
        }

        compilation.setEvents(events);

        Compilation saved = compilationRepository.save(compilation);

        return buildCompilationDto(saved);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found"));

        if (updateRequest.getTitle() != null) {
            compilation.setTitle(updateRequest.getTitle());
        }

        if (updateRequest.getPinned() != null) {
            compilation.setPinned(updateRequest.getPinned());
        }

        if (updateRequest.getEvents() != null) {
            compilation.setEvents(new HashSet<>(
                    eventRepository.findAllByIdIn(updateRequest.getEvents())
            ));
        }

        return buildCompilationDto(compilation);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {

        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation not found");
        }

        compilationRepository.deleteById(compId);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {

        int page = from / size;

        List<Compilation> compilations;

        if (pinned != null) {
            compilations = compilationRepository
                    .findAllByPinned(pinned, PageRequest.of(page, size));
        } else {
            compilations = compilationRepository
                    .findAll(PageRequest.of(page, size))
                    .getContent();
        }

        return compilations.stream()
                .map(this::buildCompilationDto)
                .toList();
    }

    @Override
    public CompilationDto getCompilation(Long compId) {

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found"));

        return buildCompilationDto(compilation);
    }

    private CompilationDto buildCompilationDto(Compilation compilation) {

        List<EventShortDto> events = compilation.getEvents()
                .stream()
                .map(event -> EventMapper.toEventShortDto(event, 0L, 0L))
                .toList();

        return CompilationMapper.toCompilationDto(compilation, events);
    }
}