package com.demo.demo.service;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.demo.demo.dto.InstrumentDTO;
import com.demo.demo.model.*;
import com.demo.demo.repository.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of {@link InstrumentService}.
 *
 * This service uses repositories to interact with the database
 * and convert entities to DTOs for API responses.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Slf4j
@AllArgsConstructor
@Service
public class InstrumentServiceImpl implements InstrumentService {

    private final InstrumentRepository instrumentRepository;
    private final SongRepository songRepository;

    @Override
    public List<InstrumentDTO> getAllInstruments() {
        return instrumentRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<InstrumentDTO> getInstrumentById(Long id) {
        return instrumentRepository.findById(id)
                .map(this::toDTO);
    }

    @Override
    public InstrumentDTO createInstrument(InstrumentDTO dto) {
        Instrument instrument = toEntity(dto);
        return toDTO(instrumentRepository.save(instrument));
    }

    @Override
    public Optional<InstrumentDTO> updateInstrument(Long id, InstrumentDTO dto) {
        return instrumentRepository.findById(id).map(existing -> {
            return toDTO(instrumentRepository.save(updateInstrumentFromDTO(existing, dto)));
        });
    }

    @Override
    public boolean deleteInstrument(Long id) {
        if (!instrumentRepository.existsById(id)) {
            return false;
        }
        instrumentRepository.deleteById(id);
        return true;
    }

    // ----------------------
    // DTO Mapping Utilities
    // ----------------------

    private InstrumentDTO toDTO(Instrument instrument) {
        InstrumentDTO dto = new InstrumentDTO();
        dto.setId(instrument.getId());
        dto.setName(instrument.getName());
        dto.setImageUrl(instrument.getImageUrl());
        return dto;
    }

    private Instrument toEntity(InstrumentDTO dto) {
        Instrument instrument = new Instrument();
        instrument.setId(dto.getId());
        instrument.setName(dto.getName());
        instrument.setImageUrl(dto.getImageUrl());
        return instrument;
    }

    private Instrument updateInstrumentFromDTO(Instrument existing, InstrumentDTO dto) {
        existing.setName(dto.getName());
        existing.setImageUrl(dto.getImageUrl());
        return existing;
    }
}

