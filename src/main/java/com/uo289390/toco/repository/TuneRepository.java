package com.uo289390.toco.repository;

import com.uo289390.toco.model.Tune;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing Tune entities.
 *
 * This interface extends JpaRepository to provide CRUD operations
 * for Tune entities in the database.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2026-10-23
 */
@Repository
public interface TuneRepository extends JpaRepository<Tune, Long> {

    /**
     * Finds the tunings of an instrument, in id order (the order of {@code data/tunes.json},
     * where the standard tuning goes first).
     *
     * @param instrumentId the ID of the instrument
     * @return the tunings of the instrument
     */
    List<Tune> findByInstrumentIdOrderByIdAsc(Long instrumentId);
}
