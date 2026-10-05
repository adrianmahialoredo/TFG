package com.demo.demo.utils.mapper;

import com.demo.demo.dto.*;
import com.demo.demo.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

@Component
public class EntityMapper {

    public Chord chordDtoToChord(ChordDto dto) {
        Chord chord = new Chord();
        chord.setId(dto.getId());
        chord.setName(dto.getName());
        chord.setNotes(dto.getNotes());
        chord.setImageUrl(dto.getImageUrl());
        chord.setOpen(dto.isOpen());
        chord.setBarre(dto.isBarre());
        chord.setPowerChord(dto.isPowerChord());
        chord.setMinor(dto.isMinor());
        chord.setMajor(dto.isMajor());
        chord.setSeventh(dto.isSeventh());
        chord.setCapo(dto.isCapo());
        chord.setCapoFret(dto.getCapoFret());
        return chord;
    }

    public Genre genreDtoToGenre(GenreDTO dto, Set<Lesson> lessons) {
        Genre genre = new Genre();
        genre.setId(dto.getId());
        genre.setTitle(dto.getTitle());
        genre.setDescription(dto.getDescription());
        genre.setImageUrl(dto.getImageUrl());
        genre.setLevel(dto.getLevel());
        genre.setBannerUrl(dto.getBannerUrl());
        genre.setWelcomeIntroVideoId(dto.getWelcomeIntroVideoId());
        genre.setWelcomeSongVideoId(dto.getWelcomeSongVideoId());
        genre.setType(Item.Type.Genre);
        if (lessons != null) {
            genre.setLessons(lessons);
        }
        return genre;
    }

    public Instrument instrumentDtoToInstrument(InstrumentDTO dto) {
        Instrument instrument = new Instrument();
        instrument.setId(dto.getId());
        instrument.setName(dto.getName());
        instrument.setImageUrl(dto.getImageUrl());
        return instrument;
    }

    /**
     * Lessons are not mapped here: they own the relationship, so they must be linked
     * (with {@link InstrumentGenre#setLessons}) once the InstrumentGenre is managed.
     */
    public InstrumentGenre instrumentGenreDtoToInstrumentGenre(
            InstrumentGenreDTO dto,
            Instrument instrument,
            Genre genre
    ) {
        InstrumentGenre ig = new InstrumentGenre();
        ig.setId(dto.getId());
        ig.setTitle(dto.getTitle());
        ig.setImageUrl(dto.getImageUrl());
        ig.setLevel(dto.getLevel());
        ig.setType(Item.Type.InstrumentGenre);
        ig.setInstrument(instrument);
        ig.setGenre(genre);
        return ig;
    }

    public ItemList itemListDtoToItemList(ItemListDTO dto, User user, Collection<? extends Item> items) {
        ItemList list = new ItemList();
        list.setId(dto.getId());
        list.setName(dto.getName());
        list.setImageUrl(dto.getImageUrl());
        list.setUser(user);
        list.setItems(items);
        return list;
    }

    public LessonSection lessonSectionDtoToLessonSection(LessonSectionDTO dto, Lesson lesson) {
        LessonSection section = new LessonSection();
        section.setId(dto.getId());
        section.setTitle(dto.getTitle());
        section.setHtmlContent(dto.getHtmlContent());
        section.setOrderIndex(dto.getOrderIndex());
        section.setLesson(lesson);
        return section;
    }

    public Song songDtoToSong(SongDTO dto, Set<Genre> genres, Set<Instrument> instruments, Set<SongLesson> lessons) {
        Song song = new Song();
        song.setId(dto.getId());
        song.setTitle(dto.getTitle());
        song.setArtist(dto.getArtist());
        song.setGenres(genres);
        song.setInstruments(instruments);
        song.setLessons(lessons);
        return song;
    }

    public SongLesson songLessonDtoToSongLesson(
            SongLessonDTO dto,
            Song song,
            Instrument instrument,
            Set<Chord> chords
    ) {
        SongLesson lesson = new SongLesson();
        lesson.setId(dto.getId());
        lesson.setDifficulty(SongLesson.DifficultyLevel.valueOf(dto.getDifficulty()));
        lesson.setChordedLyrics(dto.getChordedLyrics());
        lesson.setVideoUrl(dto.getVideoUrl());
        lesson.setSong(song);
        lesson.setInstrument(instrument);
        lesson.setChords(chords);
        lesson.setTabSections(new ArrayList<>());
        return lesson;
    }

    public TabSection tabSectionDtoToTabSection(TabSectionDTO dto, SongLesson songLesson) {
        TabSection section = new TabSection();
        section.setId(dto.getId());
        section.setIdentifier(dto.getIdentifier());
        section.setContent(dto.getContent());
        section.setSongLesson(songLesson);
        return section;
    }

    public User userDtoToUser(UserDto dto, Set<Genre> favouriteGenres, Set<Instrument> favouriteInstruments) {
        User user = new User();
        user.setId(dto.getId());
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setName(dto.getName());
        user.setProfileImageUrl(dto.getProfileImageUrl());
        user.setPassword(dto.getPassword());

        Preferences preferences = user.getPreferences();
        preferences.setFavoriteGenres(favouriteGenres);
        preferences.setFavoriteInstruments(favouriteInstruments);
        return user;
    }
}

