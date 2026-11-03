package com.uo289390.toco.utils;

import com.uo289390.toco.model.*;
import com.uo289390.toco.repository.*;
import com.uo289390.toco.exception.GlobalExceptionHandler;
import com.uo289390.toco.model.*;
import com.uo289390.toco.repository.*;
import com.uo289390.toco.model.*;
import com.uo289390.toco.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Centralized resolver that turns entity ids into managed entities.
 *
 * Spring injects the repositories through the constructor and they are stored
 * in static fields, so services can simply {@code import static} the
 * {@code resolveX(id)} methods instead of calling {@code findById} themselves.
 *
 * The {@code resolveX(id)} methods throw an {@link IllegalArgumentException}
 * (400) when the id is {@code null} and an {@link EntityNotFoundException} (404)
 * when the entity does not exist, both mapped by
 * {@link GlobalExceptionHandler}. Collection variants return a
 * {@link LinkedHashSet} (request order, no duplicates), empty when the id list is {@code null} or empty.
 *
 * The {@code findX(id)} variants return an {@link Optional} instead of throwing,
 * for callers that translate a missing entity into a 404 response.
 *
 * It is never lazily initialised ({@code @Lazy(false)}): no bean injects it, so with
 * {@code spring.main.lazy-initialization=true} (LAZY_INIT in production) it would never be
 * created, the static repositories would stay {@code null} and every resolve would fail with a 500.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2026-09-29
 */
@Component
@Lazy(false)
public class EntityResolver {

    private static ChordRepository chordRepository;
    private static GenreRepository genreRepository;
    private static InstrumentRepository instrumentRepository;
    private static InstrumentGenreRepository instrumentGenreRepository;
    private static ItemRepository itemRepository;
    private static ItemListRepository itemListRepository;
    private static LessonRepository lessonRepository;
    private static LessonSectionRepository lessonSectionRepository;
    private static SongRepository songRepository;
    private static SongLessonRepository songLessonRepository;
    private static SpotifyUserRepository spotifyUserRepository;
    private static TabSectionRepository tabSectionRepository;
    private static TuneRepository tuneRepository;
    private static UserRepository userRepository;

    public EntityResolver(ChordRepository chordRepository,
                          GenreRepository genreRepository,
                          InstrumentRepository instrumentRepository,
                          InstrumentGenreRepository instrumentGenreRepository,
                          ItemRepository itemRepository,
                          ItemListRepository itemListRepository,
                          LessonRepository lessonRepository,
                          LessonSectionRepository lessonSectionRepository,
                          SongRepository songRepository,
                          SongLessonRepository songLessonRepository,
                          SpotifyUserRepository spotifyUserRepository,
                          TabSectionRepository tabSectionRepository,
                          TuneRepository tuneRepository,
                          UserRepository userRepository) {
        EntityResolver.chordRepository = chordRepository;
        EntityResolver.genreRepository = genreRepository;
        EntityResolver.instrumentRepository = instrumentRepository;
        EntityResolver.instrumentGenreRepository = instrumentGenreRepository;
        EntityResolver.itemRepository = itemRepository;
        EntityResolver.itemListRepository = itemListRepository;
        EntityResolver.lessonRepository = lessonRepository;
        EntityResolver.lessonSectionRepository = lessonSectionRepository;
        EntityResolver.songRepository = songRepository;
        EntityResolver.songLessonRepository = songLessonRepository;
        EntityResolver.spotifyUserRepository = spotifyUserRepository;
        EntityResolver.tabSectionRepository = tabSectionRepository;
        EntityResolver.tuneRepository = tuneRepository;
        EntityResolver.userRepository = userRepository;
    }

    // -------- Optional lookups (absent id or entity -> Optional.empty()) --------

    public static Optional<Chord> findChord(Long id) {
        return find(chordRepository, id);
    }

    public static Optional<Genre> findGenre(Long id) {
        return find(genreRepository, id);
    }

    public static Optional<Instrument> findInstrument(Long id) {
        return find(instrumentRepository, id);
    }

    public static Optional<InstrumentGenre> findInstrumentGenre(Long id) {
        return find(instrumentGenreRepository, id);
    }

    public static Optional<Item> findItem(Long id) {
        return find(itemRepository, id);
    }

    public static Optional<ItemList> findItemList(Long id) {
        return find(itemListRepository, id);
    }

    public static Optional<Lesson> findLesson(Long id) {
        return find(lessonRepository, id);
    }

    public static Optional<LessonSection> findLessonSection(Long id) {
        return find(lessonSectionRepository, id);
    }

    public static Optional<Song> findSong(Long id) {
        return find(songRepository, id);
    }

    public static Optional<SongLesson> findSongLesson(Long id) {
        return find(songLessonRepository, id);
    }

    public static Optional<SpotifyUser> findSpotifyUser(Long id) {
        return find(spotifyUserRepository, id);
    }

    public static Optional<TabSection> findTabSection(Long id) {
        return find(tabSectionRepository, id);
    }

    public static Optional<Tune> findTune(Long id) {
        return find(tuneRepository, id);
    }

    public static Optional<User> findUser(Long id) {
        return find(userRepository, id);
    }

    // -------- Single entities --------

    public static Chord resolveChord(Long id) {
        return resolve(chordRepository, id, "Chord");
    }

    public static Genre resolveGenre(Long id) {
        return resolve(genreRepository, id, "Genre");
    }

    public static Instrument resolveInstrument(Long id) {
        return resolve(instrumentRepository, id, "Instrument");
    }

    public static InstrumentGenre resolveInstrumentGenre(Long id) {
        return resolve(instrumentGenreRepository, id, "InstrumentGenre");
    }

    public static Item resolveItem(Long id) {
        return resolve(itemRepository, id, "Item");
    }

    public static ItemList resolveItemList(Long id) {
        return resolve(itemListRepository, id, "ItemList");
    }

    public static Lesson resolveLesson(Long id) {
        return resolve(lessonRepository, id, "Lesson");
    }

    public static LessonSection resolveLessonSection(Long id) {
        return resolve(lessonSectionRepository, id, "LessonSection");
    }

    public static Song resolveSong(Long id) {
        return resolve(songRepository, id, "Song");
    }

    public static SongLesson resolveSongLesson(Long id) {
        return resolve(songLessonRepository, id, "SongLesson");
    }

    public static SpotifyUser resolveSpotifyUser(Long id) {
        return resolve(spotifyUserRepository, id, "SpotifyUser");
    }

    public static TabSection resolveTabSection(Long id) {
        return resolve(tabSectionRepository, id, "TabSection");
    }

    public static Tune resolveTune(Long id) {
        return resolve(tuneRepository, id, "Tune");
    }

    public static User resolveUser(Long id) {
        return resolve(userRepository, id, "User");
    }

    // -------- Collections --------

    public static Set<Chord> resolveChords(List<Long> ids) {
        return resolveAll(ids, EntityResolver::resolveChord);
    }

    /**
     * Resolves the chords of a song lesson, which must all belong to the lesson's instrument.
     *
     * @throws IllegalArgumentException (400) if any chord belongs to another instrument
     */
    public static Set<Chord> resolveChords(List<Long> ids, Instrument instrument) {
        Set<Chord> chords = resolveChords(ids);
        for (Chord chord : chords) {
            if (chord.getInstrument() == null || !chord.getInstrument().getId().equals(instrument.getId())) {
                throw new IllegalArgumentException(
                        "Chord " + chord.getId() + " does not belong to instrument " + instrument.getId());
            }
        }
        return chords;
    }

    public static Set<Genre> resolveGenres(List<Long> ids) {
        return resolveAll(ids, EntityResolver::resolveGenre);
    }

    public static Set<Instrument> resolveInstruments(List<Long> ids) {
        return resolveAll(ids, EntityResolver::resolveInstrument);
    }

    public static Set<Item> resolveItems(List<Long> ids) {
        return resolveAll(ids, EntityResolver::resolveItem);
    }

    public static Set<Lesson> resolveLessons(List<Long> ids) {
        return resolveAll(ids, EntityResolver::resolveLesson);
    }

    public static Set<SongLesson> resolveSongLessons(List<Long> ids) {
        return resolveAll(ids, EntityResolver::resolveSongLesson);
    }

    // -------- Helpers --------

    private static <T> T resolve(JpaRepository<T, Long> repository, Long id, String entityName) {
        if (id == null) {
            throw new IllegalArgumentException(entityName + " id is required");
        }
        return find(repository, id)
                .orElseThrow(() -> new EntityNotFoundException(entityName + " not found: " + id));
    }

    private static <T> Optional<T> find(JpaRepository<T, Long> repository, Long id) {
        return id == null ? Optional.empty() : repository.findById(id);
    }

    private static <T> Set<T> resolveAll(List<Long> ids, Function<Long, T> resolver) {
        if (ids == null || ids.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return ids.stream()
                .map(resolver)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
