package com.uo289390.toco.repository;

import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.uo289390.toco.model.TabSection;

/**
 * Repository interface for managing TabSection entities.
 * 
 * This interface extends JpaRepository to provide CRUD operations
 * and custom query methods for TabSection entities.
 * 
 * It allows finding tab sections by their associated song lesson ID.
 * 
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Repository
public interface TabSectionRepository extends JpaRepository<TabSection, Long> {

    /**
     * Finds all TabSection entities associated with a specific song lesson ID.
     *
     * @param songLessonId the ID of the song lesson to find tab sections for
     * @return a list of TabSection entities associated with the given song lesson ID
     */
    List<TabSection> findBySongLessonId(Long songLessonId);
    
}
