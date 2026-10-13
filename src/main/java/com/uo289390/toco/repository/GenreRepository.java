package com.uo289390.toco.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.uo289390.toco.model.Genre;

import java.util.Collection;
import java.util.List;

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

    /**
     * Finds genres whose ID is not in the given ones.
     *
     * @param excludedIds the IDs of the genres to exclude
     * @param pageable    limit of genres to return
     * @return the matching genres
     */
    List<Genre> findByIdNotIn(Collection<Long> excludedIds, Pageable pageable);
}
