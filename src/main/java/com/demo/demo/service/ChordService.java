package com.demo.demo.service;

import com.demo.demo.dto.ChordDto;
import com.demo.demo.model.Chord;
import com.demo.demo.repository.ChordRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class ChordService {

    private final ChordRepository chordRepository;

    public List<Chord> getAllChords() {
        return chordRepository.findAll();
    }

    public Optional<Chord> getChordById(Long id) {
        return chordRepository.findById(id);
    }

    public Chord saveChord(ChordDto chord) {
        
        Chord chordEntity = new Chord();
        chordEntity.setName(chord.getName());
        chordEntity.setNotes(chord.getNotes());
        chordEntity.setImageUrl(chord.getImageUrl());
        chordEntity.setOpen(chord.isOpen());
        chordEntity.setBarre(chord.isBarre());
        chordEntity.setPowerChord(chord.isPowerChord());
        chordEntity.setMinor(chord.isMinor());
        chordEntity.setMajor(chord.isMajor());
        chordEntity.setSeventh(chord.isSeventh());
        chordEntity.setId(chord.getId());

        return chordRepository.save(chordEntity);
    }

    public void deleteChord(Long id) {
        chordRepository.deleteById(id);
    }
}

