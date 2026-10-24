package com.uo289390.toco.service;

import com.uo289390.toco.dto.SongLessonDTO;
import com.uo289390.toco.dto.TabSectionDTO;
import com.uo289390.toco.model.Item;
import com.uo289390.toco.model.Song;
import com.uo289390.toco.model.SongLesson;
import com.uo289390.toco.model.TabSection;
import com.uo289390.toco.repository.*;
import com.uo289390.toco.repository.*;
import com.uo289390.toco.utils.mapper.DtoMapper;
import com.uo289390.toco.model.*;
import com.uo289390.toco.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.uo289390.toco.dto.SongDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

import static com.uo289390.toco.utils.Utils.idsToExclude;
import static com.uo289390.toco.utils.Utils.resolvePageable;
import static com.uo289390.toco.utils.RecommendationConstants.DEFAULT_SONGS_BY_ARTISTS_COUNT;
import static com.uo289390.toco.utils.RecommendationConstants.MAX_ARTISTS_IN_SONGS_BY_ARTISTS;
import static com.uo289390.toco.utils.RecommendationConstants.MAX_SONGS_BY_ARTISTS_COUNT;
import static com.uo289390.toco.utils.EntityResolver.resolveGenres;
import static com.uo289390.toco.utils.EntityResolver.resolveInstrument;
import static com.uo289390.toco.utils.EntityResolver.resolveInstruments;
import static com.uo289390.toco.utils.EntityResolver.resolveSongLessons;
import static com.uo289390.toco.utils.EntityResolver.findSong;

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
        song.setLevel(dto.getLevel());
        song.setType(Item.Type.Song);

        // 2. Campos específicos de Song
        song.setArtist(dto.getArtist());
        song.setDecade(dto.getDecade());

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
                lesson.setDifficulty(SongLesson.DifficultyLevel.from(lDto.getDifficulty()));
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
    public List<SongDTO> findNewForArtists(List<String> artists, Integer limit) {
        return findForArtists(artists, limit, songRepository::findByArtistIgnoreCaseAndArtistNewTrueAndIdNotIn);
    }

    @Override
    public List<SongDTO> findPopularForArtists(List<String> artists, Integer limit) {
        return findForArtists(artists, limit, songRepository::findByArtistIgnoreCaseAndArtistPopularTrueAndIdNotIn);
    }

    @Override
    public List<SongDTO> findPopularForGenre(Long genreId) {
        return songRepository.findByGenresIdAndGenrePopularTrue(genreId).stream()
                .map(dtoMapper::songToSongDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SongDTO> findClassicForGenre(Long genreId) {
        return songRepository.findByGenresIdAndGenreClassicTrue(genreId).stream()
                .map(dtoMapper::songToSongDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SongDTO> findNewForGenre(Long genreId) {
        return songRepository.findByGenresIdAndGenreNewTrue(genreId).stream()
                .map(dtoMapper::songToSongDto)
                .collect(Collectors.toList());
    }


    // -------- DTO Mapping --------

    /**
     * Applies the DTO to an existing song. Relations omitted from the request
     * ({@code null} id lists) are kept; an empty list clears them.
     */
    private void updateSongFromDto(Song song, SongDTO dto) {
        song.setTitle(dto.getTitle());
        song.setArtist(dto.getArtist());
        song.setImageUrl(dto.getImageUrl());
        if (dto.getLevel() > 0) {
            song.setLevel(dto.getLevel());
        }
        if (dto.getDecade() != null) {
            song.setDecade(dto.getDecade());
        }
        if (dto.getGenreIds() != null) {
            song.setGenres(resolveGenres(dto.getGenreIds()));
        }
        if (dto.getInstrumentIds() != null) {
            song.setInstruments(resolveInstruments(dto.getInstrumentIds()));
        }
        if (dto.getLessonIds() != null) {
            replaceLessons(song, resolveSongLessons(dto.getLessonIds()));
        }
    }

    /**
     * Replaces the song lessons modifying the managed collection in place: it is
     * mapped with {@code orphanRemoval}, so Hibernate rejects swapping it for a new
     * instance. Lessons left out are removed as orphans.
     */
    private void replaceLessons(Song song, Set<SongLesson> lessons) {
        song.getLessons().retainAll(lessons);
        for (SongLesson lesson : lessons) {
            lesson.setSong(song);
            song.getLessons().add(lesson);
        }
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

    // ------------------------------
    // SONGS BY ARTISTS
    // ------------------------------

    /**
     * Query of the songs of one artist (new, popular...), with exclusions and a limit.
     */
    @FunctionalInterface
    private interface ArtistSongsQuery {
        List<Song> find(String artist, Collection<Long> excludedIds, Pageable pageable);
    }

    /**
     * Songs of several artists mixed in turns (round robin), so that the first artist does not fill
     * the whole limit: first song of each artist, then the second of each one... in the given order.
     */
    private List<SongDTO> findForArtists(List<String> artists, Integer limit, ArtistSongsQuery query) {
        int total = limit == null ? DEFAULT_SONGS_BY_ARTISTS_COUNT : limit;
        if (total < 1 || total > MAX_SONGS_BY_ARTISTS_COUNT) {
            throw new IllegalArgumentException("limit must be between 1 and " + MAX_SONGS_BY_ARTISTS_COUNT);
        }
        Set<String> names = new LinkedHashSet<>();
        Optional.ofNullable(artists).orElseGet(List::of).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .forEach(names::add);
        if (names.isEmpty()) {
            throw new IllegalArgumentException("At least one artist is required");
        }
        if (names.size() > MAX_ARTISTS_IN_SONGS_BY_ARTISTS) {
            throw new IllegalArgumentException("At most " + MAX_ARTISTS_IN_SONGS_BY_ARTISTS + " artists are allowed");
        }

        // Each artist could fill the limit on their own if the others have no songs
        List<Iterator<Song>> songsByArtist = names.stream()
                .map(name -> query.find(name, idsToExclude(Set.of()), resolvePageable(total)).iterator())
                .toList();

        SequencedSet<Song> result = new LinkedHashSet<>();
        boolean added = true;
        while (result.size() < total && added) {
            added = false;
            for (Iterator<Song> songs : songsByArtist) {
                if (result.size() >= total) break;
                if (songs.hasNext()) {
                    result.add(songs.next());
                    added = true;
                }
            }
        }
        return result.stream().map(dtoMapper::songToSongDto).toList();
    }
}
