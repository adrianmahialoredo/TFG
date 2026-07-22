package com.demo.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.demo.demo.model.Lesson;
import java.util.List;

/**
 * Repository interface for managing Lesson entities.
 *
 * This interface extends JpaRepository to provide CRUD operations
 * for Lesson entities in the database.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    /**
     * Fetches all lessons that belong to a specific genre.
     *
     * @param genreId The ID of the genre.
     * @return List of lessons belonging to the specified genre.
     */
    @Query(
        "SELECT l " +
        "FROM Lesson l " +
        "WHERE l.belongsToGenre = true " +
        "AND l.genre.id = :genreId"
    )
    List<Lesson> findByGenreId(@Param("genreId") Long genreId);

    /**
     * Finds all popular lessons globally for the Home screen.
     *
     * @return a list of all popular lessons globally
     */
    @Query(
        "SELECT l " +
        "FROM Lesson l " +
        "WHERE l.popular = true"
    )
    List<Lesson> findPopular();

    /**
     * Finds the newest lessons globally for the Home screen, ordered by creation date.
     *
     * @return a list of lessons ordered from newest to oldest
     */
    @Query(
        "SELECT l " +
        "FROM Lesson l " +
        "ORDER BY l.createdAt DESC, l.id DESC")
    List<Lesson> findHomeLessonsNew();

    /**
     * Finds recommended lessons for the user for the Home screen.
     * @todo Implementar lógica de recomendación basada en preferencias del usuario.
     * Por ahora se devuelven las lecciones ordenadas por nivel (de menor a mayor dificultad).
     *
     * @return a list of recommended lessons for the user
     */
    @Query("SELECT l FROM Lesson l ORDER BY l.level ASC, l.id ASC")
    List<Lesson> findHomeLessonsRecommended();
}
