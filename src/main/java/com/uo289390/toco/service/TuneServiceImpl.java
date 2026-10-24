package com.uo289390.toco.service;

import com.uo289390.toco.dto.TuneDTO;
import com.uo289390.toco.model.Tune;
import com.uo289390.toco.repository.TuneRepository;
import com.uo289390.toco.utils.mapper.DtoMapper;
import com.uo289390.toco.utils.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.uo289390.toco.utils.EntityResolver.findTune;
import static com.uo289390.toco.utils.EntityResolver.resolveInstrument;

/**
 * Default implementation of {@link TuneService}.
 *
 * Resolves the instrument of a tuning through {@code EntityResolver} and converts
 * the entities to DTOs for the API responses.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2026-10-23
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TuneServiceImpl implements TuneService {

    private final TuneRepository tuneRepository;
    private final DtoMapper dtoMapper;
    private final EntityMapper entityMapper;

    @Override
    public List<TuneDTO> findAll() {
        return tuneRepository.findAll().stream()
                .map(dtoMapper::tuneToTuneDto)
                .toList();
    }

    @Override
    public List<TuneDTO> findByIds(List<Long> ids) {
        return tuneRepository.findAllById(ids).stream()
                .map(dtoMapper::tuneToTuneDto)
                .toList();
    }

    @Override
    public Optional<TuneDTO> findById(Long id) {
        return findTune(id).map(dtoMapper::tuneToTuneDto);
    }

    @Override
    public List<TuneDTO> findForInstrument(Long instrumentId) {
        return tuneRepository.findByInstrumentIdOrderByIdAsc(instrumentId).stream()
                .map(dtoMapper::tuneToTuneDto)
                .toList();
    }

    @Override
    @Transactional
    public TuneDTO create(TuneDTO dto) {
        Tune tune = entityMapper.tuneDtoToTune(dto, resolveInstrument(dto.getInstrumentId()));
        return dtoMapper.tuneToTuneDto(tuneRepository.save(tune));
    }

    @Override
    @Transactional
    public Optional<TuneDTO> update(Long id, TuneDTO dto) {
        return findTune(id).map(existing -> {
            updateFromDto(existing, dto);
            return dtoMapper.tuneToTuneDto(tuneRepository.save(existing));
        });
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        if (!tuneRepository.existsById(id)) {
            return false;
        }
        tuneRepository.deleteById(id);
        return true;
    }

    private void updateFromDto(Tune tune, TuneDTO dto) {
        tune.setTitle(dto.getTitle());
        tune.setInstrument(resolveInstrument(dto.getInstrumentId()));
        tune.setNotes(new ArrayList<>(dto.getNotes()));
    }
}
