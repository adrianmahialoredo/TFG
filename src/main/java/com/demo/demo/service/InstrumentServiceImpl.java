package com.demo.demo.service;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.demo.demo.dto.InstrumentDTO;
import com.demo.demo.utils.mapper.DtoMapper;
import com.demo.demo.utils.mapper.EntityMapper;
import com.demo.demo.model.*;
import com.demo.demo.repository.*;
import lombok.RequiredArgsConstructor;

import static com.demo.demo.utils.EntityResolver.findInstrument;

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
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstrumentServiceImpl implements InstrumentService {

    private final InstrumentRepository instrumentRepository;
    private final SongRepository songRepository;
    private final DtoMapper dtoMapper;
    private final EntityMapper entityMapper;

    @Override
    public List<InstrumentDTO> findAll() {
        return instrumentRepository.findAll().stream()
                .map(dtoMapper::instrumentToInstrumentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InstrumentDTO> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return instrumentRepository.findAllById(ids).stream()
                .map(dtoMapper::instrumentToInstrumentDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<InstrumentDTO> findById(Long id) {
        return findInstrument(id)
                .map(dtoMapper::instrumentToInstrumentDto);
    }

    @Override
    @Transactional
    public InstrumentDTO create(InstrumentDTO dto) {
        Instrument instrument = entityMapper.instrumentDtoToInstrument(dto);
        return dtoMapper.instrumentToInstrumentDto(instrumentRepository.save(instrument));
    }

    @Override
    @Transactional
    public Optional<InstrumentDTO> update(Long id, InstrumentDTO dto) {
        return findInstrument(id).map(existing -> {
            return dtoMapper.instrumentToInstrumentDto(instrumentRepository.save(updateInstrumentFromDTO(existing, dto)));
        });
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        if (!instrumentRepository.existsById(id)) {
            return false;
        }
        instrumentRepository.deleteById(id);
        return true;
    }

    // ----------------------
    // DTO Mapping Utilities
    // ----------------------


    private Instrument updateInstrumentFromDTO(Instrument existing, InstrumentDTO dto) {
        existing.setName(dto.getName());
        existing.setImageUrl(dto.getImageUrl());
        return existing;
    }
}

