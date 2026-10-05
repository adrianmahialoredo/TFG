package com.demo.demo.service;

import com.demo.demo.dto.SongLessonDTO;
import com.demo.demo.dto.TabSectionDTO;
import com.demo.demo.utils.mapper.DtoMapper;
import com.demo.demo.model.*;
import com.demo.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.demo.demo.dto.SongDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

import static com.demo.demo.utils.Utils.resolvePageable;
import static com.demo.demo.utils.EntityResolver.resolveGenres;
import static com.demo.demo.utils.EntityResolver.resolveInstrument;
import static com.demo.demo.utils.EntityResolver.resolveInstruments;
import static com.demo.demo.utils.EntityResolver.resolveSongLessons;
import static com.demo.demo.utils.EntityResolver.findSong;

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
@Transactional(readOnly = true)
public class SongServiceImpl implements SongService {

    private final SongRepository songRepository;
    private final GenreRepository genreRepository;
    private final InstrumentRepository instrumentRepository;
    private final ChordRepository chordRepository;
    private final UserRepository userRepository;
    private final ItemListRepository itemListRepository;
    private final DtoMapper dtoMapper;

    @Override
    public List<SongDTO> findAll() {
        return songRepository.findAll().stream()
                .map(dtoMapper::songToSongDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SongDTO> findById(Long id) {
        return findSong(id)
                .map(dtoMapper::songToSongDto);
    }

    @Override
    @Transactional
    public SongDTO create(SongDTO dto) {
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
                lesson.setInstrument(resolveInstrument(lDto.getInstrumentId()));

                // Chords (ManyToMany)
                if (lDto.getChordIds() != null) {
                    lesson.setChords(new LinkedHashSet<>(chordRepository.findAllById(lDto.getChordIds())));
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
        return dtoMapper.songToSongDto(saved);
    }

    @Override
    @Transactional
    public Optional<SongDTO> update(Long id, SongDTO dto) {
        return findSong(id).map(existing -> {
            updateSongFromDto(existing, dto);
            return dtoMapper.songToSongDto(songRepository.save(existing));
        });
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        if (!songRepository.existsById(id)) {
            return false;
        }
        songRepository.deleteById(id);
        return true;
    }

    @Override
    public List<SongDTO> findByDecade(String decade) {
        return songRepository.findByDecade(decade).stream()
                .map(dtoMapper::songToSongDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SongDTO> findPopular(Integer limit) {
        return songRepository.findByPopularTrue(resolvePageable(limit)).stream()
                .map(dtoMapper::songToSongDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SongDTO> findNew(Integer limit) {
        return songRepository.findByGenreNewTrue(resolvePageable(limit)).stream()
                .map(dtoMapper::songToSongDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SongDTO> findPopularForGenre(Long genreId) {
        return songRepository.findDistinctByGenresIdAndGenrePopularTrue(genreId).stream()
                .map(dtoMapper::songToSongDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SongDTO> findClassicForGenre(Long genreId) {
        return songRepository.findDistinctByGenresIdAndGenreClassicTrue(genreId).stream()
                .map(dtoMapper::songToSongDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SongDTO> findNewForGenre(Long genreId) {
        return songRepository.findDistinctByGenresIdAndGenreNewTrue(genreId).stream()
                .map(dtoMapper::songToSongDto)
                .collect(Collectors.toList());
    }


    // -------- DTO Mapping --------

    private void updateSongFromDto(Song song, SongDTO dto) {
        song.setTitle(dto.getTitle());
        song.setArtist(dto.getArtist());
        song.setGenres(resolveGenres(dto.getGenreIds()));
        song.setInstruments(resolveInstruments(dto.getInstrumentIds()));
        song.setLessons(resolveSongLessons(dto.getLessonIds()));
    }

    @Override
    public List<SongDTO> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return songRepository.findAllById(ids).stream()
                .map(dtoMapper::songToSongDto)
                .collect(Collectors.toList());
    }
}

