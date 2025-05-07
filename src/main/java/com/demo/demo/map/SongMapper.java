package com.demo.demo.map;

import java.util.List;

import org.springframework.stereotype.Component;

import com.demo.demo.dto.SongDto;
import com.demo.demo.model.Chord;
import com.demo.demo.model.Item;
import com.demo.demo.model.Song;
import com.demo.demo.repository.ChordRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SongMapper {

    private final ChordRepository chordRepository;
    
    public Song toEntity(SongDto dto) {

        Song song = new Song();

        song.setTitle(dto.getTitle());
        song.setArtist(dto.getArtist());
        song.setGenre(dto.getGenre());
        song.setLyricsURL(dto.getLyricsURL());
        song.setDescription(dto.getDescription());
        song.setTags(dto.getTags());
        song.setVideos(dto.getVideos());

        song.setType(Item.Type.Song); // Set the type to Song
        song.setName(dto.getTitle() + " - " + dto.getArtist() + " [Song]"); 

        if (dto.getChordIds() != null && !dto.getChordIds().isEmpty()) {
            List<Chord> chords = chordRepository.findAllById(dto.getChordIds());
            song.setChords(chords);
        }

        return song;

    }
}
