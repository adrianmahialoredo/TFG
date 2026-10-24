package com.uo289390.toco.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

import com.uo289390.toco.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

/**
 * Bean Validation tests for the entities not covered by the dedicated
 * Chord, Genre and User validation tests.
 */
class EntityValidationTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private static <T> Set<String> invalidFields(T bean) {
        return validator.validate(bean).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Nested
    class InstrumentValidation {

        private Instrument instrument;

        @BeforeEach
        void setUp() {
            instrument = new Instrument(1L, "https://example.com/guitar.png", "Guitar");
        }

        @Test
        void validInstrument_hasNoViolations() {
            assertTrue(invalidFields(instrument).isEmpty());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = { "   " })
        void blankName_isRejected(String name) {
            instrument.setName(name);

            assertTrue(invalidFields(instrument).contains("name"));
        }

        @Test
        void nameTooShort_isRejected() {
            instrument.setName("G");

            assertEquals(Set.of("name"), invalidFields(instrument));
        }

        @Test
        void nameAtBoundaries_isAccepted() {
            instrument.setName("Gt");
            assertTrue(invalidFields(instrument).isEmpty());

            instrument.setName("x".repeat(100));
            assertTrue(invalidFields(instrument).isEmpty());
        }

        @Test
        void nameTooLong_isRejected() {
            instrument.setName("x".repeat(101));

            assertEquals(Set.of("name"), invalidFields(instrument));
        }

        @ParameterizedTest
        @ValueSource(strings = { "ftp://example.com/a.png", "example.com/a.png", "" })
        void invalidImageUrl_isRejected(String url) {
            instrument.setImageUrl(url);

            assertTrue(invalidFields(instrument).contains("imageUrl"));
        }
    }

    @Nested
    class SongValidation {

        private Song song;

        @BeforeEach
        void setUp() {
            song = new Song();
            song.setId(1L);
            song.setTitle("Imagine");
            song.setArtist("John Lennon");
            song.setType(Item.Type.Song);
        }

        @Test
        void validSong_hasNoViolations() {
            assertTrue(invalidFields(song).isEmpty());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = { " " })
        void blankTitle_isRejected(String title) {
            song.setTitle(title);

            assertEquals(Set.of("title"), invalidFields(song));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = { " " })
        void blankArtist_isRejected(String artist) {
            song.setArtist(artist);

            assertEquals(Set.of("artist"), invalidFields(song));
        }

        @ParameterizedTest
        @ValueSource(strings = { "1980s", "1900s", "2020s", "" })
        void validDecade_isAccepted(String decade) {
            song.setDecade(decade);

            assertTrue(invalidFields(song).isEmpty());
        }

        @Test
        void nullDecade_isAccepted() {
            song.setDecade(null);

            assertTrue(invalidFields(song).isEmpty());
        }

        @ParameterizedTest
        @ValueSource(strings = { "80s", "1980", "1880s", "2100s", "1980S", "abcds", "1985s", "2019s" })
        void invalidDecade_isRejected(String decade) {
            song.setDecade(decade);

            assertEquals(Set.of("decade"), invalidFields(song));
        }
    }

    @Nested
    class LessonValidation {

        @Test
        void lessonWithTitle_isValid() {
            Lesson lesson = new Lesson();
            lesson.setTitle("Intro");

            assertTrue(invalidFields(lesson).isEmpty());
        }

        @Test
        void lessonWithoutTitle_isRejected() {
            assertEquals(Set.of("title"), invalidFields(new Lesson()));
        }
    }

    @Nested
    class InstrumentGenreValidation {

        private InstrumentGenre instrumentGenre;

        @BeforeEach
        void setUp() {
            instrumentGenre = new InstrumentGenre();
            instrumentGenre.setTitle("Rock guitar");
            instrumentGenre.setInstrument(new Instrument());
            instrumentGenre.setGenre(new Genre());
        }

        @Test
        void validInstrumentGenre_hasNoViolations() {
            assertTrue(invalidFields(instrumentGenre).isEmpty());
        }

        @Test
        void nullInstrument_isRejected() {
            instrumentGenre.setInstrument(null);

            assertEquals(Set.of("instrument"), invalidFields(instrumentGenre));
        }

        @Test
        void nullGenre_isRejected() {
            instrumentGenre.setGenre(null);

            assertEquals(Set.of("genre"), invalidFields(instrumentGenre));
        }

        @Test
        void missingEverything_reportsAllFields() {
            assertEquals(Set.of("title", "instrument", "genre"), invalidFields(new InstrumentGenre()));
        }
    }

    @Nested
    class ItemListValidation {

        private ItemList itemList;

        @BeforeEach
        void setUp() {
            itemList = new ItemList();
            itemList.setName("Favourites");
            itemList.setUser(new User());
        }

        @Test
        void validItemList_hasNoViolations() {
            assertTrue(invalidFields(itemList).isEmpty());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = { "  " })
        void blankName_isRejected(String name) {
            itemList.setName(name);

            assertTrue(invalidFields(itemList).contains("name"));
        }

        @Test
        void nameLengthBoundaries() {
            itemList.setName("a");
            assertEquals(Set.of("name"), invalidFields(itemList));

            itemList.setName("ab");
            assertTrue(invalidFields(itemList).isEmpty());

            itemList.setName("a".repeat(100));
            assertTrue(invalidFields(itemList).isEmpty());

            itemList.setName("a".repeat(101));
            assertEquals(Set.of("name"), invalidFields(itemList));
        }

        @ParameterizedTest
        @ValueSource(strings = { "", "http://example.com/a.png", "https://example.com/a.png" })
        void validImageUrl_isAccepted(String url) {
            itemList.setImageUrl(url);

            assertTrue(invalidFields(itemList).isEmpty());
        }

        @Test
        void invalidImageUrl_isRejected() {
            itemList.setImageUrl("not-a-url");

            assertEquals(Set.of("imageUrl"), invalidFields(itemList));
        }

        @Test
        void nullUser_isRejected() {
            itemList.setUser(null);

            assertEquals(Set.of("user"), invalidFields(itemList));
        }
    }

    @Nested
    class LessonSectionValidation {

        @Test
        void validSection_hasNoViolations() {
            LessonSection section = new LessonSection(null, "Intro", "<p>x</p>", null, new Lesson(), 0);

            assertTrue(invalidFields(section).isEmpty());
        }

        @Test
        void blankTitleAndNullLesson_areRejected() {
            LessonSection section = new LessonSection(null, " ", null, null, null, 0);

            assertEquals(Set.of("title", "lesson"), invalidFields(section));
        }

        @Test
        void nullHtmlContent_isAllowed() {
            LessonSection section = new LessonSection(null, "Intro", null, null, new Lesson(), 0);

            assertTrue(invalidFields(section).isEmpty());
        }

        @Test
        void videoUrl_mustBeHttpOrEmpty() {
            LessonSection section = new LessonSection(null, "Intro", null, "https://www.youtube.com/watch?v=abc123", new Lesson(), 0);
            assertTrue(invalidFields(section).isEmpty());

            section.setVideoUrl("");
            assertTrue(invalidFields(section).isEmpty());

            section.setVideoUrl("ftp://video");
            assertEquals(Set.of("videoUrl"), invalidFields(section));
        }
    }

    @Nested
    class PreferencesValidation {

        @ParameterizedTest
        @ValueSource(ints = { 0, 50, 100 })
        void difficultyWithinRange_isAccepted(int level) {
            Preferences preferences = new Preferences();
            preferences.setDifficultyLevel(level);

            assertTrue(invalidFields(preferences).isEmpty());
        }

        @ParameterizedTest
        @ValueSource(ints = { -1, 101 })
        void difficultyOutOfRange_isRejected(int level) {
            Preferences preferences = new Preferences();
            preferences.setDifficultyLevel(level);

            assertEquals(Set.of("difficultyLevel"), invalidFields(preferences));
        }

        @Test
        void invalidPreferences_cascadeToUserValidation() {
            User user = new User();
            user.setEmail("a@example.com");
            user.setUsername("valid_user");
            user.setPassword("password123");
            user.setName("Valid Name");
            user.getPreferences().setDifficultyLevel(150);

            assertEquals(Set.of("preferences.difficultyLevel"), invalidFields(user));
        }
    }

    @Nested
    class SongLessonValidation {

        private SongLesson songLesson;

        @BeforeEach
        void setUp() {
            songLesson = new SongLesson();
            songLesson.setDifficulty(SongLesson.DifficultyLevel.MEDIUM);
            songLesson.setChordedLyrics("[C] Hello");
            songLesson.setSong(new Song());
            songLesson.setInstrument(new Instrument());
        }

        @Test
        void validSongLesson_hasNoViolations() {
            assertTrue(invalidFields(songLesson).isEmpty());
        }

        @Test
        void requiredFieldsMissing_areReported() {
            assertEquals(Set.of("difficulty", "chordedLyrics", "song", "instrument"), invalidFields(new SongLesson()));
        }

        @ParameterizedTest
        @ValueSource(strings = { "", "https://youtube.com/watch?v=1", "http://video.com/x" })
        void validVideoUrl_isAccepted(String url) {
            songLesson.setVideoUrl(url);

            assertTrue(invalidFields(songLesson).isEmpty());
        }

        @Test
        void invalidVideoUrl_isRejected() {
            songLesson.setVideoUrl("youtube.com/watch");

            assertEquals(Set.of("videoUrl"), invalidFields(songLesson));
        }

        @Test
        void blankChordedLyrics_isRejected() {
            songLesson.setChordedLyrics("   ");

            assertEquals(Set.of("chordedLyrics"), invalidFields(songLesson));
        }
    }

    @Nested
    class TabSectionValidation {

        @Test
        void validTabSection_hasNoViolations() {
            TabSection tab = new TabSection(null, "Intro", "e|--0--|", new SongLesson());

            assertTrue(invalidFields(tab).isEmpty());
        }

        @Test
        void requiredFieldsMissing_areReported() {
            assertEquals(Set.of("identifier", "content", "songLesson"), invalidFields(new TabSection()));
        }

        @Test
        void blankStrings_areRejected() {
            TabSection tab = new TabSection(null, " ", "", new SongLesson());

            assertEquals(Set.of("identifier", "content"), invalidFields(tab));
        }
    }

    @Nested
    class SpotifyUserValidation {

        @Test
        void validSpotifyUser_hasNoViolations() {
            SpotifyUser spotifyUser = new SpotifyUser(null, "spotify-id", "access", "refresh", Instant.now(), null);

            assertTrue(invalidFields(spotifyUser).isEmpty());
        }

        @Test
        void requiredFieldsMissing_areReported() {
            assertEquals(Set.of("spotifyUserId", "accessToken", "refreshToken", "tokenExpiration"),
                    invalidFields(new SpotifyUser()));
        }

        @Test
        void blankTokens_areRejected() {
            SpotifyUser spotifyUser = new SpotifyUser(null, "spotify-id", " ", "", Instant.now(), null);

            assertEquals(Set.of("accessToken", "refreshToken"), invalidFields(spotifyUser));
        }
    }
}
