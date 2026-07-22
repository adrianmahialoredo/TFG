package com.demo.demo.repository;

import com.demo.demo.model.Song;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for managing Song entities.
 * 
 * This interface extends JpaRepository to provide CRUD operations
 * for Song entities, including methods to find songs by artist name.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Repository
public interface SongRepository extends JpaRepository<Song, Long> {

    /**
     * Finds a Song entity by its ID.
     *
     * @param id
     * @return the song matching id
     */
    Song findById(long id);

    List<Song> findByDecade(String decade);

    /**
     * Finds all popular songs with a limit.
     *
     * @return a list of popular songs limited by the specified limit
     */
    @Query(value =
        "SELECT s " +
        "FROM Song s " +
        "WHERE s.popular = true"
    )
    List<Song> findPopular(Pageable pageable);

    /**
     * Finds all popular songs associated with a specific genre.
     *
     * @param genreId the ID of the genre
     * @return a list of popular songs associated with the given genre
     */
    @Query(value =
        "SELECT DISTINCT s " +
        "FROM Song s " +
        "JOIN s.genres g " +
        "WHERE g.id = :genreId " +
        "AND s.genrePopular = true"
    )
    List<Song> findGenrePopularByGenreId(@Param("genreId") Long genreId);

    /**
     * Finds all classic songs associated with a specific genre.
     *
     * @param genreId the ID of the genre
     * @return a list of classic songs associated with the given genre
     */
    @Query(value =
        "SELECT DISTINCT s " +
        "FROM Song s " +
        "JOIN s.genres g " +
        "WHERE g.id = :genreId " +
        "AND s.genreClassic = true"
    )
    List<Song> findGenreClassicByGenreId(@Param("genreId") Long genreId);

    /**
     * Finds all new songs associated with a specific genre.
     *
     * @param genreId the ID of the genre
     * @return a list of new songs associated with the given genre
     */
    @Query(value =
        "SELECT DISTINCT s " +
        "FROM Song s " +
        "JOIN s.genres g " +
        "WHERE g.id = :genreId " +
        "AND s.genreNew = true"
    )
    List<Song> findGenreNewByGenreId(@Param("genreId") Long genreId);

    /**
     * Finds all popular songs globally for the Home screen.
     * @todo Implementar criterios de popularidad global
     * 
     * @return a list of all popular songs globally
     */
    @Query("SELECT s FROM Song s WHERE s.popular = true")
    List<Song> findHomeSongsPopular();

    /**
     * Finds all new songs globally for the Home screen.
     * @todo Implementar criterios de canciones nuevas
     * 
     * @return a list of all new songs globally
     */
    @Query("SELECT s FROM Song s WHERE s.genreNew = true")
    List<Song> findHomeSongsNew();

    /**
     * Finds all recommended songs for the user for the Home screen.
     * @todo Implementar lógica de recomendación basada en preferencias del usuario
     * 
     * @return a list of recommended songs for the user
     */
    @Query("SELECT s FROM Song s WHERE 1 = 0")
    List<Song> findHomeSongsRecommended();

    /**
     * Finds all songs from Spotify for the user for the Home screen.
     * @todo Implementar integración con Spotify para obtener canciones del usuario
     * 
     * @return a list of songs from Spotify
     */
    @Query("SELECT s FROM Song s WHERE 1 = 0")
    List<Song> findHomeSongsFromSpotify();
}
