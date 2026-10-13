package com.uo289390.toco.repository;

import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.uo289390.toco.model.LessonSection;

/**
 * Repository interface for managing LessonSection entities.
 * 
 * This interface extends JpaRepository to provide CRUD operations
 * and custom query methods for LessonSection entities.
 * 
 * It allows finding lesson sections by their associated lesson ID.
 * 
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Repository
public interface LessonSectionRepository extends JpaRepository<LessonSection, Long> {

    /**
     * Finds all LessonSection entities associated with a specific lesson ID.
     *
     * @param lessonId the ID of the lesson to find sections for
     * @return a list of LessonSection entities associated with the given lesson ID
     */
    List<LessonSection> findByLessonId(Long lessonId);
}

