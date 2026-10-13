package com.uo289390.toco.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.uo289390.toco.model.InstrumentGenre;

/**
 * Repository interface for managing InstrumentGenreScreen entities.
 * 
 * This interface extends JpaRepository to provide CRUD operations
 * and custom query methods for InstrumentGenreScreen entities.
 * 
 * It allows finding InstrumentGenreScreen entities by genreId or instrumentId.
 * 
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Repository
public interface InstrumentGenreRepository extends JpaRepository<InstrumentGenre, Long> {

    /**
     * Finds all InstrumentGenreScreen entities by genreId.
     * 
     * @param genreId the ID of the genre to search for
     * @return a list of InstrumentGenreScreen entities associated with the given genreId
     */
    List<InstrumentGenre> findByGenreId(Long genreId);
    
    /**
     * Finds all InstrumentGenreScreen entities by instrumentId.
     * 
     * @param instrumentId the ID of the instrument to search for
     * @return a list of InstrumentGenreScreen entities associated with the given instrumentId
     */
    List<InstrumentGenre> findByInstrumentId(Long instrumentId);

    Optional<InstrumentGenre> findByGenreIdAndInstrumentId(Long genreId, Long instrumentId);

    /**
     * Finds the instrument-genres that combine any of the given genres with any of the given instruments,
     * up to the given level and excluding the given IDs.
     *
     * @param genreIds the IDs of the candidate genres
     * @param instrumentIds the IDs of the candidate instruments
     * @param level the maximum level (inclusive)
     * @param excludedIds the IDs to exclude (always through {@code Utils.idsToExclude})
     * @param pageable the result limit
     * @return the matching instrument-genres
     */
    List<InstrumentGenre> findByGenreIdInAndInstrumentIdInAndLevelLessThanEqualAndIdNotIn(
            Collection<Long> genreIds, Collection<Long> instrumentIds, int level, Collection<Long> excludedIds, Pageable pageable);

    /**
     * Same as {@link #findByGenreIdInAndInstrumentIdInAndLevelLessThanEqualAndIdNotIn} without filtering by level.
     */
    List<InstrumentGenre> findByGenreIdInAndInstrumentIdInAndIdNotIn(
            Collection<Long> genreIds, Collection<Long> instrumentIds, Collection<Long> excludedIds, Pageable pageable);
}
