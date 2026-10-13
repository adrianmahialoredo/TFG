package com.uo289390.toco.repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import com.uo289390.toco.model.Chord;
import com.uo289390.toco.model.Genre;
import com.uo289390.toco.model.Instrument;
import com.uo289390.toco.model.InstrumentGenre;
import com.uo289390.toco.model.Item;
import com.uo289390.toco.model.Lesson;
import com.uo289390.toco.model.LessonSection;
import com.uo289390.toco.model.Song;
import com.uo289390.toco.model.SongLesson;
import com.uo289390.toco.model.SpotifyUser;
import com.uo289390.toco.model.TabSection;
import com.uo289390.toco.model.User;

/**
 * Factory methods that build valid, not yet persisted entities for the repository tests.
 * Items, instruments, chords and song lessons have manually assigned IDs, so callers pass them in.
 */
final class RepositoryTestFixtures {

    private RepositoryTestFixtures() {
    }

    static Instrument instrument(long id, String name) {
        Instrument instrument = new Instrument();
        instrument.setId(id);
        instrument.setName(name);
        instrument.setImageUrl("https://example.com/" + name + ".png");
        return instrument;
    }

    static Genre genre(long id, String title) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setTitle(title);
        genre.setType(Item.Type.Genre);
        genre.setDescription(title + " description");
        genre.setBannerUrl("https://example.com/" + id + "-banner.png");
        genre.setWelcomeIntroVideoId("intro" + id);
        genre.setWelcomeSongVideoId("song" + id);
        return genre;
    }

    static Song song(long id, String title, String artist) {
        Song song = new Song();
        song.setId(id);
        song.setTitle(title);
        song.setArtist(artist);
        song.setType(Item.Type.Song);
        return song;
    }

    static Lesson lesson(long id, String title) {
        Lesson lesson = new Lesson();
        lesson.setId(id);
        lesson.setTitle(title);
        lesson.setType(Item.Type.Lesson);
        return lesson;
    }

    static InstrumentGenre instrumentGenre(long id, Instrument instrument, Genre genre) {
        InstrumentGenre instrumentGenre = new InstrumentGenre();
        instrumentGenre.setId(id);
        instrumentGenre.setTitle(instrument.getName() + " " + genre.getTitle());
        instrumentGenre.setType(Item.Type.InstrumentGenre);
        instrumentGenre.setInstrument(instrument);
        instrumentGenre.setGenre(genre);
        return instrumentGenre;
    }

    static Chord chord(long id, String name) {
        Chord chord = new Chord();
        chord.setId(id);
        chord.setName(name);
        chord.setNotes(List.of("C", "E", "G"));
        chord.setImageUrl("https://example.com/" + name + ".png");
        return chord;
    }

    static SongLesson songLesson(long id, Song song, Instrument instrument) {
        SongLesson songLesson = new SongLesson();
        songLesson.setId(id);
        songLesson.setSong(song);
        songLesson.setInstrument(instrument);
        songLesson.setDifficulty(SongLesson.DifficultyLevel.EASY);
        songLesson.setChordedLyrics("[C] line one");
        songLesson.setChords(Set.of());
        return songLesson;
    }

    static TabSection tabSection(String identifier, SongLesson songLesson) {
        TabSection tabSection = new TabSection();
        tabSection.setIdentifier(identifier);
        tabSection.setContent("e|---0---|");
        tabSection.setSongLesson(songLesson);
        return tabSection;
    }

    static LessonSection lessonSection(String title, Lesson lesson, int orderIndex) {
        LessonSection section = new LessonSection();
        section.setTitle(title);
        section.setHtmlContent("<p>" + title + "</p>");
        section.setLesson(lesson);
        section.setOrderIndex(orderIndex);
        return section;
    }

    static User user(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("Password1!");
        user.setName("User " + username);
        // Wires the Preferences -> User back-reference (as EntityMapper does); without it the
        // @MapsId preferences cannot be persisted.
        user.getPreferences();
        return user;
    }

    static SpotifyUser spotifyUser(String spotifyUserId) {
        SpotifyUser spotifyUser = new SpotifyUser();
        spotifyUser.setSpotifyUserId(spotifyUserId);
        spotifyUser.setAccessToken("access-" + spotifyUserId);
        spotifyUser.setRefreshToken("refresh-" + spotifyUserId);
        spotifyUser.setTokenExpiration(Instant.now().plusSeconds(3600));
        return spotifyUser;
    }
}
