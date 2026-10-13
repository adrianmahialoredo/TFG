package com.uo289390.toco.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.uo289390.toco.model.SongLesson;

/**
 * Repository interface for managing SongLesson entities.
 * 
 * This interface extends JpaRepository to provide CRUD operations
 * and custom query methods for SongLesson entities.
 * 
 * It allows finding SongLesson entities by songId.
 * 
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Repository
public interface SongLessonRepository extends JpaRepository<SongLesson, Long> {

    /**
     * Finds all SongLesson entities associated with a specific song ID.
     *
     * @param songId the ID of the song to find lessons for
     * @return a list of SongLesson entities associated with the given song ID
     */
    List<SongLesson> findBySongId(Long songId);

}
