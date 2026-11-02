package com.uo289390.toco.service.recommendation;

import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.uo289390.toco.model.Genre;
import com.uo289390.toco.model.Instrument;
import com.uo289390.toco.model.InstrumentGenre;
import com.uo289390.toco.model.Item;
import com.uo289390.toco.model.Lesson;
import com.uo289390.toco.model.Preferences;
import com.uo289390.toco.model.Song;
import com.uo289390.toco.repository.ChordRepository;
import com.uo289390.toco.repository.GenreRepository;
import com.uo289390.toco.repository.InstrumentGenreRepository;
import com.uo289390.toco.repository.InstrumentRepository;
import com.uo289390.toco.repository.ItemListRepository;
import com.uo289390.toco.repository.ItemRepository;
import com.uo289390.toco.repository.LessonRepository;
import com.uo289390.toco.repository.LessonSectionRepository;
import com.uo289390.toco.repository.SongLessonRepository;
import com.uo289390.toco.repository.SongRepository;
import com.uo289390.toco.repository.SpotifyUserRepository;
import com.uo289390.toco.repository.TabSectionRepository;
import com.uo289390.toco.repository.TuneRepository;
import com.uo289390.toco.repository.UserRepository;
import com.uo289390.toco.utils.EntityResolver;

/**
 * Shared builders for the recommendation service tests.
 *
 * Entities are created with only the fields the algorithms use (id, title and type),
 * and {@link #registerResolver} wires the static {@link EntityResolver} to the given
 * repository mocks, since the services resolve users and item lists through it.
 */
final class RecommendationFixtures {

    private RecommendationFixtures() {
    }

    static Genre genre(long id) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setTitle("Genre " + id);
        genre.setType(Item.Type.Genre);
        return genre;
    }

    static Instrument instrument(long id) {
        Instrument instrument = new Instrument();
        instrument.setId(id);
        instrument.setName("Instrument " + id);
        return instrument;
    }

    static Song song(long id) {
        Song song = new Song();
        song.setId(id);
        song.setTitle("Song " + id);
        song.setArtist("Artist " + id);
        song.setType(Item.Type.Song);
        return song;
    }

    static List<Song> songs(long... ids) {
        return Arrays.stream(ids).mapToObj(RecommendationFixtures::song).toList();
    }

    static Lesson lesson(long id) {
        Lesson lesson = new Lesson();
        lesson.setId(id);
        lesson.setTitle("Lesson " + id);
        lesson.setType(Item.Type.Lesson);
        return lesson;
    }

    static List<Lesson> lessons(long... ids) {
        return Arrays.stream(ids).mapToObj(RecommendationFixtures::lesson).toList();
    }

    static InstrumentGenre instrumentGenre(long id, Genre genre, Instrument instrument) {
        InstrumentGenre instrumentGenre = new InstrumentGenre();
        instrumentGenre.setId(id);
        instrumentGenre.setTitle("InstrumentGenre " + id);
        instrumentGenre.setType(Item.Type.InstrumentGenre);
        instrumentGenre.setGenre(genre);
        instrumentGenre.setInstrument(instrument);
        return instrumentGenre;
    }

    /**
     * Preferences whose favourites keep the given (insertion) order.
     */
    static Preferences preferences(List<Genre> favouriteGenres, List<Instrument> favouriteInstruments) {
        Preferences preferences = new Preferences();
        preferences.setFavoriteGenres(new LinkedHashSet<>(favouriteGenres));
        preferences.setFavoriteInstruments(new LinkedHashSet<>(favouriteInstruments));
        return preferences;
    }

    /**
     * Mutable set of ids, as returned by {@code User.getIdsExcludedFromRecommendationsOfType} (the services add to it).
     */
    static Set<Long> ids(Long... ids) {
        return new java.util.HashSet<>(Arrays.asList(ids));
    }

    /**
     * Points the static {@link EntityResolver} lookups to the given repositories (the rest are dummies).
     */
    static void registerResolver(ItemListRepository itemListRepository, UserRepository userRepository) {
        new EntityResolver(
                mock(ChordRepository.class),
                mock(GenreRepository.class),
                mock(InstrumentRepository.class),
                mock(InstrumentGenreRepository.class),
                mock(ItemRepository.class),
                itemListRepository,
                mock(LessonRepository.class),
                mock(LessonSectionRepository.class),
                mock(SongRepository.class),
                mock(SongLessonRepository.class),
                mock(SpotifyUserRepository.class),
                mock(TabSectionRepository.class),
                mock(TuneRepository.class),
                userRepository);
    }
}
