package com.uo289390.toco.utils.mapper;

import com.uo289390.toco.dto.*;
import com.uo289390.toco.model.*;
import com.uo289390.toco.dto.*;
import com.uo289390.toco.model.*;
import com.uo289390.toco.dto.*;
import com.uo289390.toco.model.*;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DtoMapper {

    public ChordDto chordToChordDto(Chord chord) {
        ChordDto dto = new ChordDto();
        dto.setId(chord.getId());
        dto.setName(chord.getName());
        dto.setNotes(chord.getNotes());
        dto.setImageUrl(chord.getImageUrl());
        dto.setOpen(chord.isOpen());
        dto.setBarre(chord.isBarre());
        dto.setPowerChord(chord.isPowerChord());
        dto.setMinor(chord.isMinor());
        dto.setMajor(chord.isMajor());
        dto.setSeventh(chord.isSeventh());
        dto.setCapo(chord.isCapo());
        dto.setCapoFret(chord.getCapoFret());
        return dto;
    }

    public GenreDTO genreToGenreDto(Genre genre) {
        GenreDTO dto = new GenreDTO();
        dto.setId(genre.getId());
        dto.setTitle(genre.getTitle());
        dto.setImageUrl(genre.getImageUrl());
        dto.setLevel(genre.getLevel());
        dto.setDescription(genre.getDescription());
        dto.setBannerUrl(genre.getBannerUrl());
        dto.setWelcomeIntroVideoId(genre.getWelcomeIntroVideoId());
        dto.setWelcomeSongVideoId(genre.getWelcomeSongVideoId());

        if (genre.getLessons() != null) {
            dto.setLessonIds(genre.getLessons().stream().map(Lesson::getId).collect(Collectors.toList()));
        }

        return dto;
    }

    public InstrumentDTO instrumentToInstrumentDto(Instrument instrument) {
        InstrumentDTO dto = new InstrumentDTO();
        dto.setId(instrument.getId());
        dto.setName(instrument.getName());
        dto.setImageUrl(instrument.getImageUrl());
        return dto;
    }

    public InstrumentGenreDTO instrumentGenreToInstrumentGenreDto(InstrumentGenre instrumentGenre) {
        InstrumentGenreDTO dto = new InstrumentGenreDTO();
        dto.setId(instrumentGenre.getId());
        dto.setTitle(instrumentGenre.getTitle());
        dto.setImageUrl(instrumentGenre.getImageUrl());
        dto.setLevel(instrumentGenre.getLevel());
        dto.setInstrumentId(instrumentGenre.getInstrument().getId());
        dto.setGenreId(instrumentGenre.getGenre().getId());
        dto.setLessonIds(instrumentGenre.getLessons().stream().map(Lesson::getId).collect(Collectors.toList()));
        return dto;
    }

    public ItemListDTO itemListToItemListDto(ItemList entity) {
        ItemListDTO dto = new ItemListDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setImageUrl(entity.getImageUrl());
        dto.setUserId(entity.getUser().getId());
        dto.setItemIds(entity.getItems().stream().map(Item::getId).collect(Collectors.toList()));
        return dto;
    }

    public ItemSummaryDTO itemToItemSummaryDto(Item item) {
        ItemSummaryDTO dto = new ItemSummaryDTO();
        dto.setId(item.getId());
        dto.setTitle(item.getTitle());
        dto.setImageUrl(item.getImageUrl());
        dto.setType(item.getType());
        dto.setLevel(item.getLevel());

        // Search metadata: lets the frontend filter by genre, instrument or decade without the full DTO.
        // A lazy Item proxy is not an instance of the concrete subclass, so unproxy it first.
        switch ((Item) Hibernate.unproxy(item)) {
            case Song song -> {
                dto.setArtist(song.getArtist());
                dto.setDecade(song.getDecade());
                dto.setGenreIds(song.getGenres().stream().map(Genre::getId).toList());
                dto.setInstrumentIds(song.getInstruments().stream().map(Instrument::getId).toList());
            }
            case Lesson lesson -> {
                // A lesson of an instrument-genre inherits its genre and instrument
                Optional<InstrumentGenre> instrumentGenre = Optional.ofNullable(lesson.getInstrumentGenre());
                Optional.ofNullable(lesson.getGenre())
                        .or(() -> instrumentGenre.map(InstrumentGenre::getGenre))
                        .ifPresent(genre -> dto.setGenreIds(List.of(genre.getId())));
                Optional.ofNullable(lesson.getInstrument())
                        .or(() -> instrumentGenre.map(InstrumentGenre::getInstrument))
                        .ifPresent(instrument -> dto.setInstrumentIds(List.of(instrument.getId())));
            }
            case InstrumentGenre instrumentGenre -> {
                dto.setGenreIds(List.of(instrumentGenre.getGenre().getId()));
                dto.setInstrumentIds(List.of(instrumentGenre.getInstrument().getId()));
            }
            default -> { }
        }

        return dto;
    }

    public LessonSectionDTO lessonSectionToLessonSectionDto(LessonSection section) {
        LessonSectionDTO dto = new LessonSectionDTO();
        dto.setId(section.getId());
        dto.setTitle(section.getTitle());
        dto.setHtmlContent(section.getHtmlContent());
        dto.setVideoUrl(section.getVideoUrl());
        dto.setOrderIndex(section.getOrderIndex());
        dto.setLessonId(section.getLesson().getId());
        return dto;
    }

    public LessonDTO lessonToLessonDto(Lesson lesson) {
        LessonDTO dto = new LessonDTO();
        dto.setId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setLevel(lesson.getLevel());
        Optional<Genre> genre = Optional.ofNullable(lesson.getGenre());
        dto.setImageUrl(genre.map(Genre::getImageUrl).orElse(lesson.getImageUrl()));
        dto.setBelongsToGenre(lesson.isBelongsToGenre());
        dto.setPopular(lesson.isPopular());
        genre.map(Genre::getId).ifPresent(dto::setGenreId);
        dto.setBelongsToInstrumentGenre(lesson.isBelongsToInstrumentGenre());
        dto.setIndexInInstrumentGenre(lesson.getIndexInInstrumentGenre());
        Optional.ofNullable(lesson.getInstrumentGenre())
                .map(InstrumentGenre::getId)
                .ifPresent(dto::setInstrumentGenreId);
        Optional.ofNullable(lesson.getInstrument())
                .map(Instrument::getId)
                .ifPresent(dto::setInstrumentId);

        if (lesson.getSections() != null) {
            dto.setSectionIds(lesson.getSections().stream().map(LessonSection::getId).collect(Collectors.toList()));
        }

        return dto;
    }

    public SongDTO songToSongDto(Song song) {
        SongDTO dto = new SongDTO();
        dto.setId(song.getId());
        dto.setTitle(song.getTitle());
        dto.setImageUrl(song.getImageUrl());
        dto.setLevel(song.getLevel());
        dto.setArtist(song.getArtist());
        dto.setDecade(song.getDecade());
        dto.setGenreIds(song.getGenres().stream().map(Genre::getId).collect(Collectors.toList()));
        dto.setInstrumentIds(song.getInstruments().stream().map(Instrument::getId).collect(Collectors.toList()));
        dto.setLessonIds(song.getLessons().stream().map(SongLesson::getId).collect(Collectors.toList()));
        return dto;
    }

    public SongLessonDTO songLessonToSongLessonDto(SongLesson lesson) {
        SongLessonDTO dto = new SongLessonDTO();
        dto.setId(lesson.getId());
        if (lesson.getDifficulty() != null) {
            dto.setDifficulty(lesson.getDifficulty().name());
        }
        dto.setChordedLyrics(lesson.getChordedLyrics());
        dto.setVideoUrl(lesson.getVideoUrl());
        dto.setSongId(lesson.getSong().getId());
        dto.setInstrumentId(lesson.getInstrument().getId());
        dto.setChordIds(lesson.getChords().stream().map(Chord::getId).collect(Collectors.toList()));
        dto.setTabSectionsIds(lesson.getTabSections().stream().map(TabSection::getId).collect(Collectors.toList()));
        return dto;
    }

    public TabSectionDTO tabSectionToTabSectionDto(TabSection section) {
        TabSectionDTO dto = new TabSectionDTO();
        dto.setId(section.getId());
        dto.setIdentifier(section.getIdentifier());
        dto.setContent(section.getContent());
        dto.setSongLessonId(section.getSongLesson().getId());
        return dto;
    }

    public TuneDTO tuneToTuneDto(Tune tune) {
        TuneDTO dto = new TuneDTO();
        dto.setId(tune.getId());
        dto.setTitle(tune.getTitle());
        dto.setInstrumentId(tune.getInstrument().getId());
        dto.setNotes(new ArrayList<>(tune.getNotes()));
        return dto;
    }

    public UserDto userToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setFavouriteGenresIds(user.getPreferences().getFavoriteGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toList()));
        dto.setFavouriteInstrumentsIds(user.getPreferences().getFavoriteInstruments().stream()
                .map(Instrument::getId)
                .collect(Collectors.toList()));
        dto.setSpotifyConnected(user.getSpotifyUser() != null);
        dto.setDifficultyLevel(user.getDifficultyLevel());
        return dto;
    }

    public UserResponseDTO userToUserResponseDto(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setFavouriteGenresIds(user.getPreferences().getFavoriteGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toList()));
        dto.setFavouriteInstrumentsIds(user.getPreferences().getFavoriteInstruments().stream()
                .map(Instrument::getId)
                .collect(Collectors.toList()));
        dto.setSpotifyConnected(user.getSpotifyUser() != null);
        dto.setDifficultyLevel(user.getDifficultyLevel());
        return dto;
    }

    /**
     * Maps any item to the full DTO of its concrete subtype (song, lesson, genre or instrument-genre).
     */
    public ItemDTO itemToItemDto(Item item) {
        // A lazy Item proxy is not an instance of the concrete subclass, so unproxy it first.
        return switch ((Item) Hibernate.unproxy(item)) {
            case Song song -> songToSongDto(song);
            case Lesson lesson -> lessonToLessonDto(lesson);
            case Genre genre -> genreToGenreDto(genre);
            case InstrumentGenre instrumentGenre -> instrumentGenreToInstrumentGenreDto(instrumentGenre);
            default -> throw new IllegalArgumentException("Unknown item type: " + item.getClass().getSimpleName());
        };
    }

    public List<ItemDTO> itemsToItemDTOs(Collection<? extends Item> items) {
        return items.stream()
                .map(this::itemToItemDto)
                .toList();
    }

    public List<ItemSummaryDTO> itemsToItemSummaryDTOs(Collection<? extends Item> items) {
        return items.stream()
                .map(this::itemToItemSummaryDto)
                .toList();
    }
}


