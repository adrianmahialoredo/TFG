package com.demo.demo.service;

import com.demo.demo.dto.InstrumentGenreDTO;
import com.demo.demo.model.*;
import com.demo.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link InstrumentGenreService}.
 *
 * This service uses repositories to interact with the database
 * and convert entities to DTOs for API responses.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InstrumentGenreServiceImpl implements InstrumentGenreService {

    private final InstrumentGenreRepository instrumentGenreRepository;
    private final InstrumentRepository instrumentRepository;
    private final GenreRepository genreRepository;
    private final LessonRepository lessonRepository;

    @Override
    public List<InstrumentGenreDTO> getAllInstrumentGenres() {
        return instrumentGenreRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<InstrumentGenreDTO> getInstrumentGenreById(Long id) {
        return instrumentGenreRepository.findById(id)
                .map(this::toDTO);
    }

    @Override
    public InstrumentGenreDTO createInstrumentGenre(InstrumentGenreDTO dto) {
        InstrumentGenre ig = toEntity(dto);
        return toDTO(instrumentGenreRepository.save(ig));
    }

    @Override
    public Optional<InstrumentGenreDTO> updateInstrumentGenre(Long id, InstrumentGenreDTO dto) {
        return instrumentGenreRepository.findById(id).map(existing -> {
            return toDTO(instrumentGenreRepository.save(updateInstrumentGenre(existing, dto)));
        });
    }

    @Override
    public boolean deleteInstrumentGenre(Long id) {
        if (!instrumentGenreRepository.existsById(id)) return false;
        instrumentGenreRepository.deleteById(id);
        return true;
    }

    @Override
    public List<InstrumentGenreDTO> findByGenreId(Long genreId) {
        return instrumentGenreRepository.findByGenreId(genreId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InstrumentGenreDTO> findByInstrumentId(Long instrumentId) {
        return instrumentGenreRepository.findByInstrumentId(instrumentId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // -----------------------
    // Mapping Methods
    // -----------------------

    private InstrumentGenreDTO toDTO(InstrumentGenre ig) {
        InstrumentGenreDTO dto = new InstrumentGenreDTO();
        dto.setId(ig.getId());
        dto.setInstrumentId(ig.getInstrument().getId());
        dto.setGenreId(ig.getGenre().getId());
        dto.setLessonIds(ig.getLessons().stream()
                .map(Lesson::getId)
                .collect(Collectors.toList()));
        return dto;
    }

    private InstrumentGenre toEntity(InstrumentGenreDTO dto) {
        InstrumentGenre ig = new InstrumentGenre();
        ig.setId(dto.getId());
        ig.setInstrument(resolveInstrument(dto.getInstrumentId()));
        ig.setGenre(resolveGenre(dto.getGenreId()));
        ig.setLessons(resolveLessons(dto.getLessonIds()));
        return ig;
    }

    private InstrumentGenre updateInstrumentGenre(InstrumentGenre existing, InstrumentGenreDTO dto) {
        existing.setInstrument(resolveInstrument(dto.getInstrumentId()));
        existing.setGenre(resolveGenre(dto.getGenreId()));
        existing.setLessons(resolveLessons(dto.getLessonIds()));
        return existing;
    }

    private Instrument resolveInstrument(Long id) {
        return instrumentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Instrument not found: " + id));
    }

    private Genre resolveGenre(Long id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Genre not found: " + id));
    }

    private Set<Lesson> resolveLessons(List<Long> lessonIds) {
        if (lessonIds == null || lessonIds.isEmpty()) {
            return new HashSet<>();
        }
        return lessonIds.stream()
                .map(id -> lessonRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + id)))
                .collect(Collectors.toSet());
    }

}

