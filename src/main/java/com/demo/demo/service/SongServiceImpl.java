package com.demo.demo.service;

import com.demo.demo.dto.SongLessonDTO;
import com.demo.demo.dto.TabSectionDTO;
import com.demo.demo.model.*;
import com.demo.demo.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.demo.demo.dto.SongDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link SongService}.
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
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;
    private final GenreRepository genreRepository;
    private final InstrumentRepository instrumentRepository;
    private final SongLessonRepository songLessonRepository;
    private final ChordRepository chordRepository;

    @Override
    public List<SongDTO> getAllSongs() {
        return songRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SongDTO> getSongById(Long id) {
        return songRepository.findById(id)
                .map(this::toDto);
    }

    @Override
    @Transactional
    public SongDTO createSong(SongDTO dto) {
        Song song = new Song();

        // 1. IDs y campos básicos (Item)
        song.setId(dto.getId());
        song.setTitle(dto.getTitle());
        song.setImageUrl(dto.getImageUrl());
        song.setType(Item.Type.Song);

        // 2. Campos específicos de Song
        song.setArtist(dto.getArtist());

        // 3. Relaciones ManyToMany (Géneros e Instrumentos)
        if (dto.getGenreIds() != null) {
            song.setGenres(new HashSet<>(genreRepository.findAllById(dto.getGenreIds())));
        }
        if (dto.getInstrumentIds() != null) {
            song.setInstruments(new HashSet<>(instrumentRepository.findAllById(dto.getInstrumentIds())));
        }

        // 4. CASCADA NIVEL 1: SongLessons
        if (dto.getLessons() != null) {
            for (SongLessonDTO lDto : dto.getLessons()) {
                SongLesson lesson = new SongLesson();
                lesson.setId(lDto.getId()); // ID Manual para la lección de canción
                lesson.setDifficulty(SongLesson.DifficultyLevel.valueOf(lDto.getDifficulty()));
                lesson.setChordedLyrics(lDto.getChordedLyrics());
                lesson.setVideoUrl(lDto.getVideoUrl());

                // Instrumento de la lección
                lesson.setInstrument(Optional.ofNullable(lDto.getInstrumentId())
                        .flatMap(instrumentRepository::findById)
                        .orElseThrow(() -> new IllegalArgumentException(
                                lDto.getInstrumentId() == null
                                        ? "Instrument id is required"
                                        : "Instrument not found: " + lDto.getInstrumentId())));

                // Chords (ManyToMany)
                if (lDto.getChordIds() != null) {
                    lesson.setChords(chordRepository.findAllById(lDto.getChordIds()));
                }

                // CASCADA NIVEL 2: TabSections
                if (lDto.getTabSections() != null) {
                    for (TabSectionDTO tDto : lDto.getTabSections()) {
                        TabSection tab = new TabSection();
                        tab.setIdentifier(tDto.getIdentifier());
                        tab.setContent(tDto.getContent());

                        // Vinculación Tab -> SongLesson
                        tab.setSongLesson(lesson);
                        lesson.getTabSections().add(tab);
                    }
                }

                // Vinculación SongLesson -> Song
                lesson.setSong(song);
                song.getLessons().add(lesson);
            }
        }

        Song saved = songRepository.saveAndFlush(song);
        log.info("Canción '{}' (ID: {}) cargada con sus lecciones y tabs", saved.getTitle(), saved.getId());
        return toDto(saved);
    }

    @Override
    public Optional<SongDTO> updateSong(Long id, SongDTO dto) {
        return songRepository.findById(id).map(existing -> {
            updateSongFromDto(existing, dto);
            return toDto(songRepository.save(existing));
        });
    }

    @Override
    public boolean deleteSong(Long id) {
        if (!songRepository.existsById(id)) {
            return false;
        }
        songRepository.deleteById(id);
        return true;
    }

    @Override
    public List<SongDTO> getSongsByDecade(String decade) {
        return songRepository.findByDecade(decade).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SongDTO> getPopularSongs() {
        return songRepository.findAll().stream()
                .filter(Song::isPopular)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getPopularSongsWithLimit(int limit) {
        return songRepository.findPopular(PageRequest.of(0, limit)).stream()
                .map(Song::getId)
                .collect(Collectors.toList());
    }

    @Override
    public List<SongDTO> getPopularSongsByGenre(Long genreId) {
        return songRepository.findAll().stream()
                .filter(song -> song.getGenres().stream().anyMatch(genre -> genre.getId().equals(genreId)) && song.isGenrePopular())
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SongDTO> getClassicSongsByGenre(Long genreId) {
        return songRepository.findAll().stream()
                .filter(song -> song.getGenres().stream().anyMatch(genre -> genre.getId().equals(genreId)) && song.isGenreClassic())
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SongDTO> getNewSongsByGenre(Long genreId) {
        return songRepository.findAll().stream()
                .filter(song -> song.getGenres().stream().anyMatch(genre -> genre.getId().equals(genreId)) && song.isGenreNew())
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SongDTO> getPopularByGenreId(Long genreId) {
        return songRepository.findGenrePopularByGenreId(genreId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SongDTO> getClassicByGenreId(Long genreId) {
        return songRepository.findGenreClassicByGenreId(genreId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SongDTO> getNewByGenreId(Long genreId) {
        return songRepository.findGenreNewByGenreId(genreId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    // -------- DTO Mapping --------

    private SongDTO toDto(Song song) {
        SongDTO dto = new SongDTO();
        dto.setId(song.getId());
        dto.setTitle(song.getTitle());
        dto.setArtist(song.getArtist());
        dto.setGenreIds(song.getGenres().stream().map(Genre::getId).collect(Collectors.toList()));
        dto.setInstrumentIds(song.getInstruments().stream().map(Instrument::getId).collect(Collectors.toList()));
        dto.setLessonIds(song.getLessons().stream().map(SongLesson::getId).collect(Collectors.toList()));
        return dto;
    }

    private Song toEntity(SongDTO dto) {
        Song song = new Song();
        song.setId(dto.getId());
        song.setTitle(dto.getTitle());
        song.setArtist(dto.getArtist());
        song.setGenres(resolveGenres(dto.getGenreIds()));
        song.setInstruments(resolveInstruments(dto.getInstrumentIds()));
        song.setLessons(resolveLessons(dto.getLessonIds()));
        return song;
    }

    private void updateSongFromDto(Song song, SongDTO dto) {
        song.setTitle(dto.getTitle());
        song.setArtist(dto.getArtist());
        song.setGenres(resolveGenres(dto.getGenreIds()));
        song.setInstruments(resolveInstruments(dto.getInstrumentIds()));
        song.setLessons(resolveLessons(dto.getLessonIds()));
    }

    private Set<Genre> resolveGenres(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        return ids.stream()
                .map(id -> genreRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Genre not found: " + id)))
                .collect(Collectors.toSet());
    }

    private Set<Instrument> resolveInstruments(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        return ids.stream()
                .map(id -> instrumentRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Instrument not found: " + id)))
                .collect(Collectors.toSet());
    }

    @Override
    public List<SongDTO> getSongsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return songRepository.findAllById(ids).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private List<SongLesson> resolveLessons(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return ids.stream()
                .map(id -> songLessonRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + id)))
                .collect(Collectors.toList());
    }

    @Override
    public List<SongDTO> getHomeSongsPopular() {
        return songRepository.findHomeSongsPopular().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SongDTO> getHomeSongsNew() {
        return songRepository.findHomeSongsNew().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SongDTO> getHomeSongsRecommended() {
        return songRepository.findHomeSongsRecommended().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SongDTO> getHomeSongsFromSpotify() {
        return songRepository.findHomeSongsFromSpotify().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}

