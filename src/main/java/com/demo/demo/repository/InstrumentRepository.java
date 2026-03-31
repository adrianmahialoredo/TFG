package com.demo.demo.repository;

import com.demo.demo.model.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Instrument entities.
 *
 * This interface extends JpaRepository to provide CRUD operations
 * for Instrument entities in the database.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

}
