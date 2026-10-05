package com.demo.demo.repository;

import com.demo.demo.model.Chord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Chord entities.
 *
 * This interface extends JpaRepository to provide CRUD operations
 * for Chord entities in the database. The chords of a song lesson are
 * read from {@code SongLesson.chords}.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Repository
public interface ChordRepository extends JpaRepository<Chord, Long> {
}
