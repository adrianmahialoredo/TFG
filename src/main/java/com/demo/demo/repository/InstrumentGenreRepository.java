package com.demo.demo.repository;

import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.demo.demo.model.InstrumentGenre;

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
    
}
