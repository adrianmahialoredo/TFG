package com.uo289390.toco.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.ExpectedCount.never;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.uo289390.toco.service.recommendation.SpotifyRecommendationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.uo289390.toco.dto.SongDTO;
import com.uo289390.toco.model.Genre;
import com.uo289390.toco.model.Instrument;
import com.uo289390.toco.model.InstrumentGenre;
import com.uo289390.toco.model.Item;
import com.uo289390.toco.model.Lesson;
import com.uo289390.toco.model.Song;
import com.uo289390.toco.model.User;
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
import com.uo289390.toco.repository.SongRepository.TitleArtist;
import com.uo289390.toco.repository.SpotifyUserRepository;
import com.uo289390.toco.repository.TabSectionRepository;
import com.uo289390.toco.repository.TuneRepository;
import com.uo289390.toco.repository.UserRepository;
import com.uo289390.toco.utils.EntityResolver;
import com.uo289390.toco.utils.RecommendationConstants;
import com.uo289390.toco.utils.mapper.DtoMapper;

import jakarta.persistence.EntityNotFoundException;

/**
 * Unit tests for SpotifyRecommendationServiceImpl.
 * Tests how the user's Spotify top tracks and top artists are requested, matched
 * against the songs in the database and completed up to the limit. Spotify is
 * simulated with a {@link MockRestServiceServer}, so no request leaves the test.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SpotifyRecommendationServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final String ACCESS_TOKEN = "access-token";
    private static final String TOP_TRACKS_URL = "https://api.spotify.com/v1/me/top/tracks";
    private static final String TOP_ARTISTS_URL = "https://api.spotify.com/v1/me/top/artists";
    private static final String EMPTY_ITEMS = "{\"items\":[]}";

    @Mock
    private SpotifyAuthService spotifyAuthService;

    @Mock
    private SongRepository songRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private InstrumentGenreRepository instrumentGenreRepository;

    @Mock
    private DtoMapper dtoMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private User user;

    private MockRestServiceServer server;
    private SpotifyRecommendationServiceImpl service;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        service = new SpotifyRecommendationServiceImpl(restTemplate, spotifyAuthService, songRepository,
                lessonRepository, instrumentGenreRepository, dtoMapper);

        new EntityResolver(mock(ChordRepository.class), mock(GenreRepository.class), mock(InstrumentRepository.class),
                mock(InstrumentGenreRepository.class), mock(ItemRepository.class), mock(ItemListRepository.class),
                mock(LessonRepository.class), mock(LessonSectionRepository.class), songRepository,
                mock(SongLessonRepository.class), mock(SpotifyUserRepository.class), mock(TabSectionRepository.class),
                mock(TuneRepository.class),
                userRepository);

        when(spotifyAuthService.getValidAccessToken(USER_ID)).thenReturn(ACCESS_TOKEN);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(user.getIdsExcludedFromRecommendationsOfType(Song.class)).thenReturn(new HashSet<>());
        when(songRepository.findByTitleAndArtistIn(any())).thenReturn(List.of());
        when(songRepository.findByArtistIgnoreCaseAndArtistPopularTrueAndIdNotIn(anyString(), any(), any())).thenReturn(List.of());
        when(songRepository.findByArtistIgnoreCaseAndArtistClassicTrueAndIdNotIn(anyString(), any(), any())).thenReturn(List.of());
        when(songRepository.findByArtistIgnoreCaseAndArtistNewTrueAndIdNotIn(anyString(), any(), any())).thenReturn(List.of());
        when(dtoMapper.songToSongDto(any())).thenAnswer(invocation -> {
            Song song = invocation.getArgument(0);
            SongDTO dto = new SongDTO();
            dto.setId(song.getId());
            dto.setTitle(song.getTitle());
            return dto;
        });
    }

    private static Song song(long id, String title, String artist) {
        Song song = new Song();
        song.setId(id);
        song.setTitle(title);
        song.setArtist(artist);
        song.setType(Item.Type.Song);
        return song;
    }

    private static Song song(long id) {
        return song(id, "Song " + id, "Artist " + id);
    }

    private static String track(String name, String artist) {
        return "{\"name\":\"" + name + "\",\"artists\":[{\"name\":\"" + artist + "\"}]}";
    }

    private static String items(String... items) {
        return "{\"items\":[" + String.join(",", items) + "]}";
    }

    private static String artist(String name) {
        return "{\"name\":\"" + name + "\",\"id\":\"id-" + name.trim() + "\"}";
    }

    private void expectTopTracks(int limit, String timeRange, String responseJson) {
        server.expect(requestTo(TOP_TRACKS_URL + "?limit=" + limit + "&time_range=" + timeRange))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));
    }

    private void expectTopArtists(String responseJson) {
        server.expect(requestTo(TOP_ARTISTS_URL + "?limit=" + RecommendationConstants.N_TOP_ARTISTS_USED_IN_SPOTIFY_SONG_RECOMMENDATION + "&time_range=medium_term"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));
    }

    private static List<Long> idsOf(List<SongDTO> songs) {
        return songs.stream().map(SongDTO::getId).toList();
    }

    @Nested
    class FindSongsForUser {

        @Test
        void testFindSongsForUser_NonPositiveLimit_ReturnsEmptyWithoutCalls() {
            // Arrange
            server.expect(never(), anything());

            // Act
            List<SongDTO> result = service.findSongsForUser(USER_ID, 0);

            // Assert
            assertTrue(result.isEmpty());
            server.verify();
            verifyNoInteractions(spotifyAuthService, songRepository);
        }

        @Test
        void testFindSongsForUser_NullUserId_ThrowsIllegalArgument() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> service.findSongsForUser(null, 5));
            verifyNoInteractions(spotifyAuthService);
        }

        @Test
        void testFindSongsForUser_TokenUnavailable_PropagatesWithoutCallingSpotify() {
            // Arrange
            when(spotifyAuthService.getValidAccessToken(USER_ID)).thenThrow(new EntityNotFoundException("Usuario de Spotify no encontrado"));
            server.expect(never(), anything());

            // Act & Assert
            assertThrows(EntityNotFoundException.class, () -> service.findSongsForUser(USER_ID, 5));
            server.verify();
        }

        @Test
        void testFindSongsForUser_UserNotFound_ThrowsEntityNotFound() {
            // Arrange
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
            server.expect(never(), anything());

            // Act & Assert
            assertThrows(EntityNotFoundException.class, () -> service.findSongsForUser(USER_ID, 5));
            server.verify();
        }

        @Test
        void testFindSongsForUser_RequestsDoubleSplitBetweenTimeRangesInPriorityOrder() {
            // Arrange (limit 5 -> 10 requested: medium 5, long 3, short 2)
            expectTopTracks(5, "medium_term", EMPTY_ITEMS);
            expectTopTracks(3, "long_term", EMPTY_ITEMS);
            expectTopTracks(2, "short_term", EMPTY_ITEMS);
            expectTopArtists(EMPTY_ITEMS);

            // Act
            List<SongDTO> result = service.findSongsForUser(USER_ID, 5);

            // Assert
            assertTrue(result.isEmpty());
            server.verify();
        }

        @Test
        void testFindSongsForUser_RequestedSongsAreCappedAtSpotifyMaximum() {
            // Arrange (limit 30 -> 60 capped at 50: medium 25, long 13, short 12)
            expectTopTracks(25, "medium_term", EMPTY_ITEMS);
            expectTopTracks(13, "long_term", EMPTY_ITEMS);
            expectTopTracks(12, "short_term", EMPTY_ITEMS);
            expectTopArtists(EMPTY_ITEMS);

            // Act
            service.findSongsForUser(USER_ID, 30);

            // Assert
            server.verify();
        }

        @Test
        void testFindSongsForUser_LimitOne_SkipsShortTermRequest() {
            // Arrange (limit 1 -> 2 requested: medium 1, long 1, short 0)
            expectTopTracks(1, "medium_term", EMPTY_ITEMS);
            expectTopTracks(1, "long_term", EMPTY_ITEMS);
            expectTopArtists(EMPTY_ITEMS);

            // Act
            service.findSongsForUser(USER_ID, 1);

            // Assert
            server.verify();
        }

        @SuppressWarnings("unchecked")
        @Test
        void testFindSongsForUser_MatchesTopTracksIgnoringCaseInSpotifyOrder() {
            // Arrange
            expectTopTracks(2, "medium_term", items(
                    track(" Song B ", "Artist B"),
                    "{\"name\":\"No artists\",\"artists\":[]}",
                    "{\"artists\":[{\"name\":\"No name\"}]}"));
            expectTopTracks(1, "long_term", items(track("song a", "artist a"), track("Song B", "Artist B")));
            expectTopTracks(1, "short_term", items(track("Unknown", "Nobody")));
            Song songA = song(1, "Song A", "Artist A");
            Song songB = song(2, "SONG B", "artist b");
            when(songRepository.findByTitleAndArtistIn(any())).thenReturn(List.of(songA, songB));

            // Act
            List<SongDTO> result = service.findSongsForUser(USER_ID, 2);

            // Assert
            assertEquals(List.of(2L, 1L), idsOf(result));
            server.verify();
            ArgumentCaptor<Collection<TitleArtist>> pairs = ArgumentCaptor.forClass(Collection.class);
            verify(songRepository).findByTitleAndArtistIn(pairs.capture());
            assertEquals(List.of(
                    new TitleArtist("Song B", "Artist B"),
                    new TitleArtist("song a", "artist a"),
                    new TitleArtist("Unknown", "Nobody")), List.copyOf(pairs.getValue()));
            verify(songRepository, mockitoNever()).findByArtistIgnoreCaseAndArtistPopularTrueAndIdNotIn(anyString(), any(), any());
        }

        @Test
        void testFindSongsForUser_DuplicateSongsInDatabase_KeepsTheFirstOne() {
            // Arrange: two stored songs share the same (case-insensitive) title and artist
            expectTopTracks(2, "medium_term", items(track("Song A", "Artist A")));
            expectTopTracks(1, "long_term", EMPTY_ITEMS);
            expectTopTracks(1, "short_term", EMPTY_ITEMS);
            when(songRepository.findByTitleAndArtistIn(any()))
                    .thenReturn(List.of(song(5, "Song A", "Artist A"), song(6, "SONG A", "artist a")));
            expectTopArtists(EMPTY_ITEMS);

            // Act
            List<SongDTO> result = service.findSongsForUser(USER_ID, 2);

            // Assert
            assertEquals(List.of(5L), idsOf(result));
            server.verify();
        }

        @Test
        void testFindSongsForUser_ExcludesSongsInUserListsAndLimitsResult() {
            // Arrange
            when(user.getIdsExcludedFromRecommendationsOfType(Song.class)).thenReturn(new HashSet<>(Set.of(1L)));
            expectTopTracks(2, "medium_term", items(track("Song 1", "Artist 1"), track("Song 2", "Artist 2")));
            expectTopTracks(1, "long_term", items(track("Song 3", "Artist 3")));
            expectTopTracks(1, "short_term", items(track("Song 4", "Artist 4")));
            when(songRepository.findByTitleAndArtistIn(any())).thenReturn(List.of(song(1), song(2), song(3), song(4)));

            // Act
            List<SongDTO> result = service.findSongsForUser(USER_ID, 2);

            // Assert
            assertEquals(List.of(2L, 3L), idsOf(result));
            server.verify();
        }

        @Test
        void testFindSongsForUser_MissingSongs_CompletedFromTopArtistsInFilterOrder() {
            // Arrange (limit 2, 2 artists -> 1 song each)
            when(user.getIdsExcludedFromRecommendationsOfType(Song.class)).thenReturn(new HashSet<>(Set.of(50L)));
            expectTopTracks(2, "medium_term", EMPTY_ITEMS);
            expectTopTracks(1, "long_term", EMPTY_ITEMS);
            expectTopTracks(1, "short_term", EMPTY_ITEMS);
            expectTopArtists(items(artist(" Top "), artist("Second")));
            when(songRepository.findByArtistIgnoreCaseAndArtistPopularTrueAndIdNotIn(eq("Top"), any(), any())).thenReturn(List.of(song(10)));
            when(songRepository.findByArtistIgnoreCaseAndArtistClassicTrueAndIdNotIn(eq("Second"), any(), any())).thenReturn(List.of(song(20)));

            // Act
            List<SongDTO> result = service.findSongsForUser(USER_ID, 2);

            // Assert
            assertEquals(List.of(10L, 20L), idsOf(result));
            server.verify();
            ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Collection<Long>> excluded = ArgumentCaptor.forClass(Collection.class);
            verify(songRepository).findByArtistIgnoreCaseAndArtistPopularTrueAndIdNotIn(eq("Top"), any(), pageable.capture());
            assertEquals(1, pageable.getValue().getPageSize());
            verify(songRepository, mockitoNever()).findByArtistIgnoreCaseAndArtistClassicTrueAndIdNotIn(eq("Top"), any(), any());
            verify(songRepository).findByArtistIgnoreCaseAndArtistClassicTrueAndIdNotIn(eq("Second"), excluded.capture(), pageable.capture());
            assertEquals(1, pageable.getValue().getPageSize());
            assertTrue(excluded.getValue().containsAll(Set.of(50L, 10L)));
            verify(songRepository, mockitoNever()).findByArtistIgnoreCaseAndArtistNewTrueAndIdNotIn(anyString(), any(), any());
        }

        @Test
        void testFindSongsForUser_TopArtistGetsTheRemainder() {
            // Arrange (limit 5, nothing matched, 2 artists -> top 3, second 2)
            expectTopTracks(5, "medium_term", EMPTY_ITEMS);
            expectTopTracks(3, "long_term", EMPTY_ITEMS);
            expectTopTracks(2, "short_term", EMPTY_ITEMS);
            expectTopArtists(items(artist("Top"), artist("Second")));

            // Act
            service.findSongsForUser(USER_ID, 5);

            // Assert
            ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
            verify(songRepository).findByArtistIgnoreCaseAndArtistPopularTrueAndIdNotIn(eq("Top"), any(), pageable.capture());
            assertEquals(3, pageable.getValue().getPageSize());
            verify(songRepository).findByArtistIgnoreCaseAndArtistPopularTrueAndIdNotIn(eq("Second"), any(), pageable.capture());
            assertEquals(2, pageable.getValue().getPageSize());
        }

        @Test
        void testFindSongsForUser_ArtistFiltersAskOnlyForWhatIsStillMissing() {
            // Arrange (limit 3, single artist)
            expectTopTracks(3, "medium_term", EMPTY_ITEMS);
            expectTopTracks(2, "long_term", EMPTY_ITEMS);
            expectTopTracks(1, "short_term", EMPTY_ITEMS);
            expectTopArtists(items(artist("Only")));
            when(songRepository.findByArtistIgnoreCaseAndArtistPopularTrueAndIdNotIn(eq("Only"), any(), any())).thenReturn(List.of(song(1)));
            when(songRepository.findByArtistIgnoreCaseAndArtistNewTrueAndIdNotIn(eq("Only"), any(), any())).thenReturn(List.of(song(2)));

            // Act
            List<SongDTO> result = service.findSongsForUser(USER_ID, 3);

            // Assert
            assertEquals(List.of(1L, 2L), idsOf(result));
            ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
            verify(songRepository).findByArtistIgnoreCaseAndArtistPopularTrueAndIdNotIn(eq("Only"), any(), pageable.capture());
            assertEquals(3, pageable.getValue().getPageSize());
            verify(songRepository).findByArtistIgnoreCaseAndArtistClassicTrueAndIdNotIn(eq("Only"), any(), pageable.capture());
            assertEquals(2, pageable.getValue().getPageSize());
            verify(songRepository).findByArtistIgnoreCaseAndArtistNewTrueAndIdNotIn(eq("Only"), any(), pageable.capture());
            assertEquals(2, pageable.getValue().getPageSize());
        }

        @Test
        void testFindSongsForUser_PartialTopTracksMatch_ArtistsCompleteTheRest() {
            // Arrange
            expectTopTracks(2, "medium_term", items(track("Song 1", "Artist 1")));
            expectTopTracks(1, "long_term", EMPTY_ITEMS);
            expectTopTracks(1, "short_term", EMPTY_ITEMS);
            expectTopArtists(items(artist("Artist 1")));
            when(songRepository.findByTitleAndArtistIn(any())).thenReturn(List.of(song(1)));
            when(songRepository.findByArtistIgnoreCaseAndArtistPopularTrueAndIdNotIn(eq("Artist 1"), any(), any())).thenReturn(List.of(song(5)));

            // Act
            List<SongDTO> result = service.findSongsForUser(USER_ID, 2);

            // Assert
            assertEquals(List.of(1L, 5L), idsOf(result));
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Collection<Long>> excluded = ArgumentCaptor.forClass(Collection.class);
            verify(songRepository).findByArtistIgnoreCaseAndArtistPopularTrueAndIdNotIn(eq("Artist 1"), excluded.capture(), any());
            assertTrue(excluded.getValue().contains(1L), "songs already recommended are excluded");
        }

        @Test
        void testFindSongsForUser_ResponseWithoutItems_TreatedAsEmpty() {
            // Arrange
            expectTopTracks(1, "medium_term", "{}");
            expectTopTracks(1, "long_term", "{}");
            expectTopArtists("{}");

            // Act
            List<SongDTO> result = service.findSongsForUser(USER_ID, 1);

            // Assert
            assertTrue(result.isEmpty());
            server.verify();
        }

        @Test
        void testFindSongsForUser_SpotifyUnauthorized_PropagatesClientError() {
            // Arrange
            server.expect(requestTo(TOP_TRACKS_URL + "?limit=1&time_range=medium_term"))
                    .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

            // Act & Assert
            assertThrows(HttpClientErrorException.class, () -> service.findSongsForUser(USER_ID, 1));
            verify(songRepository, mockitoNever()).findByTitleAndArtistIn(any());
        }
    }

    @Nested
    class FindItemsForUser {

        @SuppressWarnings("unchecked")
        private final ArgumentCaptor<Collection<Item>> itemsCaptor = ArgumentCaptor.forClass(Collection.class);

        // Item IDs are unique across all item types: songs 1-9, genres 30-39, lessons 100+, instrument-genres 200+

        private static Genre genre(long id) {
            Genre genre = new Genre();
            genre.setId(id);
            return genre;
        }

        private static Instrument instrument(long id) {
            Instrument instrument = new Instrument();
            instrument.setId(id);
            return instrument;
        }

        private static Song songWith(long id, List<Genre> genres, List<Instrument> instruments) {
            Song song = song(id);
            song.setGenres(new HashSet<>(genres));
            song.setInstruments(new HashSet<>(instruments));
            return song;
        }

        private static Lesson lesson(long id) {
            Lesson lesson = new Lesson();
            lesson.setId(id);
            return lesson;
        }

        private static InstrumentGenre instrumentGenre(long id) {
            InstrumentGenre instrumentGenre = new InstrumentGenre();
            instrumentGenre.setId(id);
            return instrumentGenre;
        }

        /**
         * Spotify returns the given songs as the top tracks (6 recommended songs: 12 requested, 6 + 3 + 3)
         * and no top artists, so nothing is completed from them.
         */
        private void expectTopSongs(Song... songs) {
            String[] tracks = Arrays.stream(songs).map(song -> track(song.getTitle(), song.getArtist())).toArray(String[]::new);
            expectTopTracks(6, "medium_term", items(tracks));
            expectTopTracks(3, "long_term", EMPTY_ITEMS);
            expectTopTracks(3, "short_term", EMPTY_ITEMS);
            expectTopArtists(EMPTY_ITEMS);
            when(songRepository.findByTitleAndArtistIn(any())).thenReturn(List.of(songs));
        }

        private List<Item> mappedItems() {
            verify(dtoMapper).itemsToItemDTOs(itemsCaptor.capture());
            return List.copyOf(itemsCaptor.getValue());
        }

        @BeforeEach
        void setUpItems() {
            when(user.getIdsExcludedFromRecommendationsOfType(any())).thenReturn(new HashSet<>());
            when(dtoMapper.itemsToItemDTOs(any())).thenReturn(List.of());
        }

        @Test
        void testFindItemsForUser_NullUserId_ThrowsIllegalArgument() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> service.findItemsForUser(null));
        }

        @Test
        void testFindItemsForUser_NoSpotifySongsInDatabase_ReturnsEmptyWithoutQueryingOtherItems() {
            // Arrange
            expectTopSongs();

            // Act
            List<?> result = service.findItemsForUser(USER_ID);

            // Assert
            assertTrue(result.isEmpty());
            verifyNoInteractions(lessonRepository, instrumentGenreRepository);
            verify(dtoMapper, mockitoNever()).itemsToItemDTOs(any());
            server.verify();
        }

        @Test
        void testFindItemsForUser_MergesSongsLessonsInstrumentGenresAndTopGenre() {
            // Arrange: genre 30 is in both songs, genre 31 only in one; instrument 10 in both, 11 in one
            Song first = songWith(1, List.of(genre(30), genre(31)), List.of(instrument(10)));
            Song second = songWith(2, List.of(genre(30)), List.of(instrument(10), instrument(11)));
            expectTopSongs(first, second);
            when(lessonRepository.findByGenreIdInAndBelongsToGenreTrueAndIdNotIn(any(), any(), any())).thenReturn(List.of(lesson(100)));
            when(instrumentGenreRepository.findByGenreIdInAndInstrumentIdInAndIdNotIn(any(), any(), any(), any()))
                    .thenReturn(List.of(instrumentGenre(200)));

            // Act
            service.findItemsForUser(USER_ID);

            // Assert: the songs, the lesson, the instrument-genre and only the most repeated genre
            Set<Long> ids = new HashSet<>(mappedItems().stream().map(Item::getId).toList());
            assertEquals(Set.of(1L, 2L, 30L, 100L, 200L), ids);

            ArgumentCaptor<Pageable> lessonsPage = ArgumentCaptor.forClass(Pageable.class);
            verify(lessonRepository).findByGenreIdInAndBelongsToGenreTrueAndIdNotIn(eq(List.of(30L, 31L)), any(), lessonsPage.capture());
            assertEquals(RecommendationConstants.N_SPOTIFY_RECOMMENDED_LESSONS, lessonsPage.getValue().getPageSize());

            ArgumentCaptor<Pageable> instrumentGenresPage = ArgumentCaptor.forClass(Pageable.class);
            verify(instrumentGenreRepository).findByGenreIdInAndInstrumentIdInAndIdNotIn(
                    eq(List.of(30L, 31L)), eq(List.of(10L, 11L)), any(), instrumentGenresPage.capture());
            assertEquals(RecommendationConstants.N_SPOTIFY_RECOMMENDED_INSTRUMENT_GENRES, instrumentGenresPage.getValue().getPageSize());
            server.verify();
        }

        @Test
        void testFindItemsForUser_ExcludesItemsInUserLists() {
            // Arrange
            when(user.getIdsExcludedFromRecommendationsOfType(Lesson.class)).thenReturn(new HashSet<>(Set.of(100L)));
            when(user.getIdsExcludedFromRecommendationsOfType(InstrumentGenre.class)).thenReturn(new HashSet<>(Set.of(200L)));
            expectTopSongs(songWith(1, List.of(genre(30)), List.of(instrument(10))));

            // Act
            service.findItemsForUser(USER_ID);

            // Assert
            verify(lessonRepository).findByGenreIdInAndBelongsToGenreTrueAndIdNotIn(any(), eq(Set.of(100L)), any());
            verify(instrumentGenreRepository).findByGenreIdInAndInstrumentIdInAndIdNotIn(any(), any(), eq(Set.of(200L)), any());
        }

        @Test
        void testFindItemsForUser_GenreAlreadyInLists_RecommendsTheNextMostRepeated() {
            // Arrange
            when(user.getIdsExcludedFromRecommendationsOfType(Genre.class)).thenReturn(new HashSet<>(Set.of(30L)));
            Song first = songWith(1, List.of(genre(30), genre(31)), List.of());
            Song second = songWith(2, List.of(genre(30)), List.of());
            expectTopSongs(first, second);

            // Act
            service.findItemsForUser(USER_ID);

            // Assert: songs 1 and 2 and genre 31 (genre 30 is already in the user's lists)
            Set<Long> ids = new HashSet<>(mappedItems().stream().map(Item::getId).toList());
            assertEquals(Set.of(1L, 2L, 31L), ids);
        }

        @Test
        void testFindItemsForUser_SongsWithoutInstruments_SkipsInstrumentGenres() {
            // Arrange
            expectTopSongs(songWith(1, List.of(genre(30)), List.of()));

            // Act
            service.findItemsForUser(USER_ID);

            // Assert
            verifyNoInteractions(instrumentGenreRepository);
        }

        @Test
        void testFindItemsForUser_SongsWithoutGenres_ReturnsOnlySongs() {
            // Arrange
            expectTopSongs(songWith(1, List.of(), List.of(instrument(10))));

            // Act
            service.findItemsForUser(USER_ID);

            // Assert
            verifyNoInteractions(lessonRepository, instrumentGenreRepository);
            assertEquals(List.of(1L), mappedItems().stream().map(Item::getId).toList());
        }

        @Test
        void testFindItemsForUser_SpotifyUnauthorized_PropagatesClientError() {
            // Arrange
            server.expect(anything()).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

            // Act & Assert
            assertThrows(HttpClientErrorException.class, () -> service.findItemsForUser(USER_ID));
        }
    }

    /**
     * Mockito's {@code never()} (the static import of {@code never} belongs to {@code ExpectedCount}).
     */
    private static org.mockito.verification.VerificationMode mockitoNever() {
        return org.mockito.Mockito.never();
    }
}
