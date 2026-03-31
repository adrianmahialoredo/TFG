package com.demo.demo.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.demo.demo.model.Genre;

/**
 * Repository interface for managing Genre entities.
 *
 * This interface extends JpaRepository to provide CRUD operations
 * for Genre entities in the database.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
    
}
