package com.uo289390.toco.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import com.uo289390.toco.model.*;
import com.uo289390.toco.repository.*;
import com.uo289390.toco.utils.EntityResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Unit tests for {@link EntityResolver}.
 * Every find/resolve method is checked against its repository: present entity,
 * missing entity, null id, and the collection variants (order, duplicates, empty input).
 */
@ExtendWith(MockitoExtension.class)
class EntityResolverTest {

    @Mock private ChordRepository chordRepository;
    @Mock private GenreRepository genreRepository;
    @Mock private InstrumentRepository instrumentRepository;
    @Mock private InstrumentGenreRepository instrumentGenreRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private ItemListRepository itemListRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private LessonSectionRepository lessonSectionRepository;
    @Mock private SongRepository songRepository;
    @Mock private SongLessonRepository songLessonRepository;
    @Mock private SpotifyUserRepository spotifyUserRepository;
    @Mock private TabSectionRepository tabSectionRepository;
    @Mock private TuneRepository tuneRepository;
    @Mock private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        new EntityResolver(chordRepository, genreRepository, instrumentRepository, instrumentGenreRepository,
                itemRepository, itemListRepository, lessonRepository, lessonSectionRepository, songRepository,
                songLessonRepository, spotifyUserRepository, tabSectionRepository, tuneRepository, userRepository);
    }

    /**
     * Describes one entity type: how to get its repository, how to build an instance,
     * and the find/resolve functions to exercise.
     */
    private record Case(String name,
                        Function<EntityResolverTest, JpaRepository<Object, Long>> repository,
                        Object entity,
                        Function<Long, Optional<?>> finder,
                        Function<Long, Object> resolver) {
        @Override
        public String toString() {
            return name;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Function<EntityResolverTest, JpaRepository<Object, Long>> repo(
            Function<EntityResolverTest, JpaRepository<T, Long>> getter) {
        return test -> (JpaRepository<Object, Long>) (JpaRepository<?, Long>) getter.apply(test);
    }

    static Stream<Arguments> cases() {
        Song song = new Song();
        song.setId(1L);
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        return Stream.of(
                new Case("Chord", repo(t -> t.chordRepository), new Chord(), EntityResolver::findChord, EntityResolver::resolveChord),
                new Case("Genre", repo(t -> t.genreRepository), new Genre(), EntityResolver::findGenre, EntityResolver::resolveGenre),
                new Case("Instrument", repo(t -> t.instrumentRepository), new Instrument(), EntityResolver::findInstrument, EntityResolver::resolveInstrument),
                new Case("InstrumentGenre", repo(t -> t.instrumentGenreRepository), new InstrumentGenre(), EntityResolver::findInstrumentGenre, EntityResolver::resolveInstrumentGenre),
                new Case("Item", repo(t -> t.itemRepository), song, EntityResolver::findItem, EntityResolver::resolveItem),
                new Case("ItemList", repo(t -> t.itemListRepository), new ItemList(), EntityResolver::findItemList, EntityResolver::resolveItemList),
                new Case("Lesson", repo(t -> t.lessonRepository), lesson, EntityResolver::findLesson, EntityResolver::resolveLesson),
                new Case("LessonSection", repo(t -> t.lessonSectionRepository), new LessonSection(), EntityResolver::findLessonSection, EntityResolver::resolveLessonSection),
                new Case("Song", repo(t -> t.songRepository), new Song(), EntityResolver::findSong, EntityResolver::resolveSong),
                new Case("SongLesson", repo(t -> t.songLessonRepository), new SongLesson(), EntityResolver::findSongLesson, EntityResolver::resolveSongLesson),
                new Case("SpotifyUser", repo(t -> t.spotifyUserRepository), new SpotifyUser(), EntityResolver::findSpotifyUser, EntityResolver::resolveSpotifyUser),
                new Case("TabSection", repo(t -> t.tabSectionRepository), new TabSection(), EntityResolver::findTabSection, EntityResolver::resolveTabSection),
                new Case("Tune", repo(t -> t.tuneRepository), new Tune(), EntityResolver::findTune, EntityResolver::resolveTune),
                new Case("User", repo(t -> t.userRepository), new User(), EntityResolver::findUser, EntityResolver::resolveUser)
        ).map(Arguments::of);
    }

    // -------- find --------

    @ParameterizedTest(name = "find{0} returns the entity when it exists")
    @MethodSource("cases")
    void testFind_Present(Case c) {
        // Arrange
        JpaRepository<Object, Long> repository = c.repository().apply(this);
        when(repository.findById(10L)).thenReturn(Optional.of(c.entity()));

        // Act
        Optional<?> result = c.finder().apply(10L);

        // Assert
        assertTrue(result.isPresent());
        assertSame(c.entity(), result.get());
        verify(repository).findById(10L);
    }

    @ParameterizedTest(name = "find{0} returns empty when the entity does not exist")
    @MethodSource("cases")
    void testFind_Missing(Case c) {
        // Arrange
        JpaRepository<Object, Long> repository = c.repository().apply(this);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<?> result = c.finder().apply(99L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @ParameterizedTest(name = "find{0} returns empty for a null id without hitting the repository")
    @MethodSource("cases")
    void testFind_NullId(Case c) {
        // Act
        Optional<?> result = c.finder().apply(null);

        // Assert
        assertTrue(result.isEmpty());
        verify(c.repository().apply(this), never()).findById(any());
    }

    // -------- resolve --------

    @ParameterizedTest(name = "resolve{0} returns the entity when it exists")
    @MethodSource("cases")
    void testResolve_Present(Case c) {
        // Arrange
        when(c.repository().apply(this).findById(5L)).thenReturn(Optional.of(c.entity()));

        // Act
        Object result = c.resolver().apply(5L);

        // Assert
        assertSame(c.entity(), result);
    }

    @ParameterizedTest(name = "resolve{0} throws EntityNotFoundException when missing")
    @MethodSource("cases")
    void testResolve_Missing(Case c) {
        // Arrange
        when(c.repository().apply(this).findById(42L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> c.resolver().apply(42L));
        assertEquals(c.name() + " not found: 42", ex.getMessage());
    }

    @ParameterizedTest(name = "resolve{0} throws IllegalArgumentException for a null id")
    @MethodSource("cases")
    void testResolve_NullId(Case c) {
        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> c.resolver().apply(null));
        assertEquals(c.name() + " id is required", ex.getMessage());
        verify(c.repository().apply(this), never()).findById(any());
    }

    // -------- collections --------

    private static Genre genre(long id) {
        Genre genre = new Genre();
        genre.setId(id);
        return genre;
    }

    @Test
    void testResolveGenres_NullIdsReturnsEmptyMutableSet() {
        // Act
        Set<Genre> result = EntityResolver.resolveGenres(null);

        // Assert
        assertTrue(result.isEmpty());
        assertDoesNotThrow(() -> result.add(genre(1)));
        verifyNoInteractions(genreRepository);
    }

    @Test
    void testResolveGenres_EmptyIdsReturnsEmpty() {
        // Act
        Set<Genre> result = EntityResolver.resolveGenres(List.of());

        // Assert
        assertTrue(result.isEmpty());
        verifyNoInteractions(genreRepository);
    }

    @Test
    void testResolveGenres_KeepsRequestOrderAndRemovesDuplicates() {
        // Arrange
        when(genreRepository.findById(3L)).thenReturn(Optional.of(genre(3)));
        when(genreRepository.findById(1L)).thenReturn(Optional.of(genre(1)));
        when(genreRepository.findById(2L)).thenReturn(Optional.of(genre(2)));

        // Act
        Set<Genre> result = EntityResolver.resolveGenres(List.of(3L, 1L, 3L, 2L));

        // Assert
        assertEquals(List.of(3L, 1L, 2L), result.stream().map(Genre::getId).toList());
    }

    @Test
    void testResolveGenres_MissingIdThrows() {
        // Arrange
        when(genreRepository.findById(1L)).thenReturn(Optional.of(genre(1)));
        when(genreRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> EntityResolver.resolveGenres(List.of(1L, 2L)));
    }

    @Test
    void testResolveGenres_NullElementThrows() {
        // Arrange
        List<Long> ids = new ArrayList<>();
        ids.add(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> EntityResolver.resolveGenres(ids));
    }

    @Test
    void testResolveChords() {
        // Arrange
        Chord a = new Chord();
        a.setId(1L);
        Chord b = new Chord();
        b.setId(2L);
        when(chordRepository.findById(1L)).thenReturn(Optional.of(a));
        when(chordRepository.findById(2L)).thenReturn(Optional.of(b));

        // Act
        Set<Chord> result = EntityResolver.resolveChords(List.of(2L, 1L));

        // Assert
        assertEquals(List.of(b, a), new ArrayList<>(result));
        assertTrue(EntityResolver.resolveChords(null).isEmpty());
    }

    @Test
    void testResolveInstruments() {
        // Arrange
        Instrument guitar = new Instrument();
        guitar.setId(1L);
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(guitar));

        // Act
        Set<Instrument> result = EntityResolver.resolveInstruments(List.of(1L, 1L));

        // Assert
        assertEquals(Set.of(guitar), result);
        assertTrue(EntityResolver.resolveInstruments(List.of()).isEmpty());
    }

    @Test
    void testResolveItems() {
        // Arrange
        Song song = new Song();
        song.setId(1L);
        Lesson lesson = new Lesson();
        lesson.setId(2L);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(song));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(lesson));

        // Act
        Set<Item> result = EntityResolver.resolveItems(List.of(1L, 2L));

        // Assert
        assertEquals(List.of(song, lesson), new ArrayList<>(result));
        assertTrue(EntityResolver.resolveItems(null).isEmpty());
    }

    @Test
    void testResolveLessons() {
        // Arrange
        Lesson lesson = new Lesson();
        lesson.setId(4L);
        when(lessonRepository.findById(4L)).thenReturn(Optional.of(lesson));

        // Act
        Set<Lesson> result = EntityResolver.resolveLessons(List.of(4L));

        // Assert
        assertEquals(Set.of(lesson), result);
        assertTrue(EntityResolver.resolveLessons(null).isEmpty());
    }

    @Test
    void testResolveSongLessons() {
        // Arrange
        SongLesson songLesson = new SongLesson();
        songLesson.setId(8L);
        when(songLessonRepository.findById(8L)).thenReturn(Optional.of(songLesson));

        // Act
        Set<SongLesson> result = EntityResolver.resolveSongLessons(List.of(8L));

        // Assert
        assertEquals(Set.of(songLesson), result);
        assertTrue(EntityResolver.resolveSongLessons(List.of()).isEmpty());
    }

    @Test
    void testResolveSongLessons_MissingThrows() {
        // Arrange
        when(songLessonRepository.findById(8L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> EntityResolver.resolveSongLessons(List.of(8L)));
        assertEquals("SongLesson not found: 8", ex.getMessage());
    }
}
