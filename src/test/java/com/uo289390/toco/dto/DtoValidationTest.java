package com.uo289390.toco.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.uo289390.toco.dto.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.uo289390.toco.dto.validation.OnCreate;
import com.uo289390.toco.model.Item;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

/**
 * Bean Validation tests for every request DTO, including the OnCreate validation group
 * (which extends Default) versus the Default group used on updates.
 */
class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private static <T> Set<String> invalidFields(T bean, Class<?>... groups) {
        return validator.validate(bean, groups).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Nested
    class UserDtoValidation {

        private UserDto dto;

        @BeforeEach
        void setUp() {
            dto = new UserDto();
            dto.setEmail("user@example.com");
            dto.setName("Test User");
            dto.setUsername("testuser");
            dto.setPassword("Password1!");
            dto.setFavouriteGenresIds(List.of(1L, 2L));
            dto.setFavouriteInstrumentsIds(List.of(1L, 2L));
        }

        @Test
        void validDto_passesBothGroups() {
            assertTrue(invalidFields(dto).isEmpty());
            assertTrue(invalidFields(dto, OnCreate.class).isEmpty());
        }

        @Test
        void difficultyLevel_isOptionalAndBetween0And5() {
            dto.setDifficultyLevel(null);
            assertTrue(invalidFields(dto).isEmpty());
            dto.setDifficultyLevel(0);
            assertTrue(invalidFields(dto).isEmpty());
            dto.setDifficultyLevel(5);
            assertTrue(invalidFields(dto).isEmpty());

            dto.setDifficultyLevel(-1);
            assertEquals(Set.of("difficultyLevel"), invalidFields(dto));
            dto.setDifficultyLevel(6);
            assertEquals(Set.of("difficultyLevel"), invalidFields(dto));
        }

        @Test
        void missingPassword_isOnlyRejectedOnCreate() {
            dto.setPassword(null);

            assertTrue(invalidFields(dto).isEmpty());
            assertEquals(Set.of("password"), invalidFields(dto, OnCreate.class));
        }

        @Test
        void blankPassword_isRejectedOnCreate() {
            dto.setPassword("");

            assertTrue(invalidFields(dto, OnCreate.class).contains("password"));
        }

        @Test
        void onCreate_alsoAppliesDefaultConstraints() {
            dto.setEmail("not-an-email");

            assertEquals(Set.of("email"), invalidFields(dto, OnCreate.class));
        }

        @ParameterizedTest
        @ValueSource(strings = { "Password1!", "Abcdefg1#", "Zz9_____", "MyPass1€x" })
        void strongPassword_isAccepted(String password) {
            dto.setPassword(password);

            assertTrue(invalidFields(dto, OnCreate.class).isEmpty());
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "password1!",   // no uppercase
                "Password!!",   // no digit
                "Password12",   // no symbol
                "Pass 1!word",  // contains a space
                "Pa1!"          // too short
        })
        void weakPassword_isRejectedInBothGroups(String password) {
            dto.setPassword(password);

            assertEquals(Set.of("password"), invalidFields(dto));
            assertEquals(Set.of("password"), invalidFields(dto, OnCreate.class));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = { "plainaddress", "@example.com" })
        void invalidEmail_isRejected(String email) {
            dto.setEmail(email);

            assertEquals(Set.of("email"), invalidFields(dto));
        }

        @Test
        void usernameLengthBoundaries() {
            dto.setUsername("ab");
            assertEquals(Set.of("username"), invalidFields(dto));

            dto.setUsername("abc");
            assertTrue(invalidFields(dto).isEmpty());

            dto.setUsername("a".repeat(20));
            assertTrue(invalidFields(dto).isEmpty());

            dto.setUsername("a".repeat(21));
            assertEquals(Set.of("username"), invalidFields(dto));
        }

        @Test
        void nameLengthBoundaries() {
            dto.setName("A");
            assertEquals(Set.of("name"), invalidFields(dto));

            dto.setName("x".repeat(100));
            assertTrue(invalidFields(dto).isEmpty());

            dto.setName("x".repeat(101));
            assertEquals(Set.of("name"), invalidFields(dto));
        }

        @ParameterizedTest
        @ValueSource(strings = { "", "https://example.com/me.png", "http://example.com/me.png" })
        void validProfileImageUrl_isAccepted(String url) {
            dto.setProfileImageUrl(url);

            assertTrue(invalidFields(dto).isEmpty());
        }

        @Test
        void invalidProfileImageUrl_isRejected() {
            dto.setProfileImageUrl("ftp://example.com/me.png");

            assertEquals(Set.of("profileImageUrl"), invalidFields(dto));
        }

        @Test
        void favourites_nullAreRejected() {
            dto.setFavouriteGenresIds(null);
            dto.setFavouriteInstrumentsIds(null);

            assertEquals(Set.of("favouriteGenresIds", "favouriteInstrumentsIds"), invalidFields(dto));
        }

        @Test
        void favourites_belowMinimumAreRejected() {
            dto.setFavouriteGenresIds(List.of(1L));
            dto.setFavouriteInstrumentsIds(List.of());

            assertEquals(Set.of("favouriteGenresIds", "favouriteInstrumentsIds"), invalidFields(dto));
        }
    }

    @Nested
    class LoginRequestDtoValidation {

        private LoginRequestDto dto;

        @BeforeEach
        void setUp() {
            dto = new LoginRequestDto();
            dto.setIdentifier("user@example.com");
            dto.setPassword("Password1!");
        }

        @Test
        void validDto_hasNoViolations() {
            assertTrue(invalidFields(dto).isEmpty());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = { "   " })
        void blankIdentifier_isRejected(String identifier) {
            dto.setIdentifier(identifier);

            assertEquals(Set.of("identifier"), invalidFields(dto));
        }

        @ParameterizedTest
        @ValueSource(strings = { "user@example.com", "johnny", "j.doe_99" })
        void emailOrUsername_isAccepted(String identifier) {
            dto.setIdentifier(identifier);

            assertTrue(invalidFields(dto).isEmpty());
        }

        @ParameterizedTest
        @ValueSource(strings = { "Password1#", "Password1@", "MyPass1€x", "Zz9_____", "anything" })
        void anyNonBlankPassword_isAccepted(String password) {
            // Regression: passwords accepted at registration (e.g. "Password1#") used to be
            // rejected at login by a stricter pattern, so those users could never log in
            dto.setPassword(password);

            assertTrue(invalidFields(dto).isEmpty());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = { "   " })
        void blankPassword_isRejected(String password) {
            dto.setPassword(password);

            assertEquals(Set.of("password"), invalidFields(dto));
        }

        @ParameterizedTest
        @ValueSource(strings = { "Password1#", "Abcdefg1~", "MyPass1€x" })
        void everyPasswordAcceptedAtRegistration_isAcceptedAtLogin(String password) {
            UserDto registration = new UserDto();
            registration.setPassword(password);
            dto.setPassword(password);

            assertFalse(invalidFields(registration).contains("password"));
            assertTrue(invalidFields(dto).isEmpty());
        }
    }

    @Nested
    class ItemDtoValidation {

        private SongDTO song;

        @BeforeEach
        void setUp() {
            song = new SongDTO();
            song.setTitle("Imagine");
        }

        @Test
        void validDto_passesBothGroups() {
            assertTrue(invalidFields(song).isEmpty());
            assertTrue(invalidFields(song, OnCreate.class).isEmpty());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = { "  " })
        void blankTitle_isRejected(String title) {
            song.setTitle(title);

            assertEquals(Set.of("title"), invalidFields(song));
            assertEquals(Set.of("title"), invalidFields(song, OnCreate.class));
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, 3, 5 })
        void levelInRange_isAccepted(int level) {
            song.setLevel(level);

            assertTrue(invalidFields(song).isEmpty());
        }

        @ParameterizedTest
        @ValueSource(ints = { -1, 6 })
        void levelOutOfRange_isRejected(int level) {
            song.setLevel(level);

            assertEquals(Set.of("level"), invalidFields(song));
        }

        @Test
        void itemDtoSubclasses_reportTheirType() {
            assertEquals(Item.Type.Song, new SongDTO().getType());
            assertEquals(Item.Type.Lesson, new LessonDTO().getType());
            assertEquals(Item.Type.Genre, new GenreDTO().getType());
            assertEquals(Item.Type.InstrumentGenre, new InstrumentGenreDTO().getType());
        }

        @Test
        void songDto_relationIdsDefaultToNull_soOmittedFieldsAreDistinguishable() {
            // null means "not sent" (an update keeps the relation); an empty list clears it
            SongDTO dto = new SongDTO();

            assertNull(dto.getGenreIds());
            assertNull(dto.getInstrumentIds());
            assertNull(dto.getLessonIds());
            assertTrue(dto.getLessons().isEmpty());
        }

        @Test
        void lessonDto_inheritsTitleConstraint() {
            assertEquals(Set.of("title"), invalidFields(new LessonDTO()));
        }
    }

    @Nested
    class GenreDtoValidation {

        private GenreDTO dto;

        @BeforeEach
        void setUp() {
            dto = new GenreDTO();
            dto.setTitle("Rock");
            dto.setDescription("Loud guitars");
            dto.setBannerUrl("https://example.com/banner.png");
            dto.setWelcomeIntroVideoId("abc123");
            dto.setWelcomeSongVideoId("def456");
        }

        @Test
        void validDto_hasNoViolations() {
            assertTrue(invalidFields(dto, OnCreate.class).isEmpty());
        }

        @Test
        void optionalFieldsMayBeNull() {
            GenreDTO minimal = new GenreDTO();
            minimal.setTitle("Rock");

            assertTrue(invalidFields(minimal).isEmpty());
        }

        @Test
        void descriptionTooLong_isRejected() {
            dto.setDescription("x".repeat(1001));

            assertEquals(Set.of("description"), invalidFields(dto));
        }

        @Test
        void descriptionAtLimit_isAccepted() {
            dto.setDescription("x".repeat(1000));

            assertTrue(invalidFields(dto).isEmpty());
        }

        @Test
        void bannerUrl_mustBeHttpOrEmpty() {
            dto.setBannerUrl("");
            assertTrue(invalidFields(dto).isEmpty());

            dto.setBannerUrl("banner.png");
            assertEquals(Set.of("bannerUrl"), invalidFields(dto));
        }

        @Test
        void videoIdsTooLong_areRejected() {
            dto.setWelcomeIntroVideoId("x".repeat(101));
            dto.setWelcomeSongVideoId("x".repeat(101));

            assertEquals(Set.of("welcomeIntroVideoId", "welcomeSongVideoId"), invalidFields(dto));
        }
    }

    @Nested
    class InstrumentGenreDtoValidation {

        @Test
        void validDto_hasNoViolations() {
            InstrumentGenreDTO dto = new InstrumentGenreDTO();
            dto.setTitle("Rock guitar");
            dto.setInstrumentId(1L);
            dto.setGenreId(2L);

            assertTrue(invalidFields(dto, OnCreate.class).isEmpty());
        }

        @Test
        void missingIds_areRejected() {
            InstrumentGenreDTO dto = new InstrumentGenreDTO();
            dto.setTitle("Rock guitar");

            assertEquals(Set.of("instrumentId", "genreId"), invalidFields(dto));
            assertEquals(Set.of("instrumentId", "genreId"), invalidFields(dto, OnCreate.class));
        }
    }

    @Nested
    class ChordDtoValidation {

        private ChordDto dto;

        @BeforeEach
        void setUp() {
            dto = new ChordDto();
            dto.setName("Am");
            dto.setImageUrl("https://example.com/am.png");
            dto.setInstrumentId(1L);
        }

        @Test
        void validDto_hasNoViolations() {
            assertTrue(invalidFields(dto, OnCreate.class).isEmpty());
        }

        @Test
        void missingInstrumentId_isRejected() {
            dto.setInstrumentId(null);

            assertEquals(Set.of("instrumentId"), invalidFields(dto));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = { " " })
        void blankName_isRejected(String name) {
            dto.setName(name);

            assertTrue(invalidFields(dto).contains("name"));
        }

        @Test
        void nameLengthBoundaries() {
            dto.setName("x".repeat(50));
            assertTrue(invalidFields(dto).isEmpty());

            dto.setName("x".repeat(51));
            assertEquals(Set.of("name"), invalidFields(dto));
        }

        @Test
        void imageUrl_mustBeValidUrlWhenPresent() {
            dto.setImageUrl(null);
            assertTrue(invalidFields(dto).isEmpty());

            dto.setImageUrl("not a url");
            assertEquals(Set.of("imageUrl"), invalidFields(dto));
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, 7, 12 })
        void capoFretInRange_isAccepted(int fret) {
            dto.setCapoFret(fret);

            assertTrue(invalidFields(dto).isEmpty());
        }

        @ParameterizedTest
        @ValueSource(ints = { -1, 13 })
        void capoFretOutOfRange_isRejected(int fret) {
            dto.setCapoFret(fret);

            assertEquals(Set.of("capoFret"), invalidFields(dto));
        }
    }

    @Nested
    class InstrumentDtoValidation {

        @Test
        void validDto_hasNoViolations() {
            InstrumentDTO dto = new InstrumentDTO();
            dto.setName("Guitar");
            dto.setImageUrl("https://example.com/g.png");

            assertTrue(invalidFields(dto, OnCreate.class).isEmpty());
        }

        @Test
        void blankNameAndBadUrl_areRejected() {
            InstrumentDTO dto = new InstrumentDTO();
            dto.setName("");
            dto.setImageUrl("::bad::");

            assertEquals(Set.of("name", "imageUrl"), invalidFields(dto));
        }

        @Test
        void nameTooLong_isRejected() {
            InstrumentDTO dto = new InstrumentDTO();
            dto.setName("x".repeat(101));

            assertEquals(Set.of("name"), invalidFields(dto));
        }
    }

    @Nested
    class LessonSectionDtoValidation {

        private LessonSectionDTO dto;

        @BeforeEach
        void setUp() {
            dto = new LessonSectionDTO();
            dto.setTitle("Intro");
            dto.setHtmlContent("<p>Hello</p>");
            dto.setLessonId(1L);
        }

        @Test
        void validDto_hasNoViolations() {
            assertTrue(invalidFields(dto, OnCreate.class).isEmpty());
        }

        @Test
        void requiredFieldsMissing_areRejected() {
            assertEquals(Set.of("title", "htmlContent", "lessonId"), invalidFields(new LessonSectionDTO()));
        }

        @Test
        void titleTooLong_isRejected() {
            dto.setTitle("x".repeat(201));

            assertEquals(Set.of("title"), invalidFields(dto));
        }

        @Test
        void negativeOrderIndex_isRejected() {
            dto.setOrderIndex(-1);

            assertEquals(Set.of("orderIndex"), invalidFields(dto));
        }

        @Test
        void videoUrl_isOptionalButMustBeAUrl() {
            dto.setVideoUrl("https://www.youtube.com/watch?v=abc123");
            assertTrue(invalidFields(dto).isEmpty());

            dto.setVideoUrl("no es una url");
            assertEquals(Set.of("videoUrl"), invalidFields(dto));
        }
    }

    @Nested
    class SongLessonDtoValidation {

        private SongLessonDTO dto;

        @BeforeEach
        void setUp() {
            dto = new SongLessonDTO();
            dto.setDifficulty("EASY");
            dto.setSongId(1L);
            dto.setInstrumentId(2L);
        }

        @Test
        void validDto_hasNoViolations() {
            assertTrue(invalidFields(dto, OnCreate.class).isEmpty());
        }

        @ParameterizedTest
        @ValueSource(strings = { "EASY", "MEDIUM", "ORIGINAL" })
        void knownDifficulty_isAccepted(String difficulty) {
            dto.setDifficulty(difficulty);

            assertTrue(invalidFields(dto).isEmpty());
        }

        @ParameterizedTest
        @ValueSource(strings = { "easy", "HARD", "", "EASYMEDIUM" })
        void unknownDifficulty_isRejected(String difficulty) {
            dto.setDifficulty(difficulty);

            assertEquals(Set.of("difficulty"), invalidFields(dto));
        }

        @Test
        void missingDifficulty_isRejectedOnCreateAndUpdate() {
            // Regression: null passed the @Pattern and ended in a NullPointerException (500) when mapping
            dto.setDifficulty(null);

            assertEquals(Set.of("difficulty"), invalidFields(dto));
            assertEquals(Set.of("difficulty"), invalidFields(dto, OnCreate.class));
        }

        @Test
        void missingIds_areRejected() {
            dto.setSongId(null);
            dto.setInstrumentId(null);

            assertEquals(Set.of("songId", "instrumentId"), invalidFields(dto));
        }

        @Test
        void lyricsTooLong_isRejected() {
            dto.setChordedLyrics("x".repeat(5001));

            assertEquals(Set.of("chordedLyrics"), invalidFields(dto));
        }

        @Test
        void invalidVideoUrl_isRejected() {
            dto.setVideoUrl("not a url");

            assertEquals(Set.of("videoUrl"), invalidFields(dto));
        }
    }

    @Nested
    class TabSectionDtoValidation {

        private TabSectionDTO dto;

        @BeforeEach
        void setUp() {
            dto = new TabSectionDTO();
            dto.setIdentifier("Intro");
            dto.setContent("e|--0--|");
            dto.setSongLessonId(1L);
        }

        @Test
        void validDto_hasNoViolations() {
            assertTrue(invalidFields(dto, OnCreate.class).isEmpty());
        }

        @Test
        void requiredFieldsMissing_areRejected() {
            assertEquals(Set.of("identifier", "content", "songLessonId"), invalidFields(new TabSectionDTO()));
        }

        @Test
        void lengthLimits() {
            dto.setIdentifier("x".repeat(51));
            dto.setContent("x".repeat(10001));

            assertEquals(Set.of("identifier", "content"), invalidFields(dto));
        }

        @Test
        void lengthsAtLimit_areAccepted() {
            dto.setIdentifier("x".repeat(50));
            dto.setContent("x".repeat(10000));

            assertTrue(invalidFields(dto).isEmpty());
        }
    }

    @Nested
    class UnconstrainedDtos {

        @Test
        void itemListDto_hasNoConstraintsAndDefaultsToEmptyItems() {
            ItemListDTO dto = new ItemListDTO();

            assertTrue(invalidFields(dto).isEmpty());
            assertTrue(invalidFields(dto, OnCreate.class).isEmpty());
            assertEquals(new ArrayList<Long>(), dto.getItemIds());
        }

        @Test
        void responseDtos_haveNoConstraints() {
            assertTrue(invalidFields(new ItemSummaryDTO()).isEmpty());
            assertTrue(invalidFields(new UserResponseDTO()).isEmpty());
            assertTrue(invalidFields(new LoginResponse()).isEmpty());
        }

        @Test
        void loginResponse_allArgsConstructor() {
            UserResponseDTO user = new UserResponseDTO();
            user.setUsername("bob");

            LoginResponse response = new LoginResponse("token", user);

            assertEquals("token", response.getToken());
            assertSame(user, response.getUser());
        }
    }
}
