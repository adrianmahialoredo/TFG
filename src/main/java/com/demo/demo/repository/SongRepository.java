package com.demo.demo.repository;

import com.demo.demo.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;
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

    Song findByTitle(String title);

    Song findById(long id);

    /**
     * Finds all songs associated with a specific genre ID.
     * 
     * @param artist the name of the artist whose songs are to be retrieved
     * @return a list of songs associated with the given artist name
     */
    List<Song> findByArtist(String artist);

    List<Song> findByDecade(String decade);
    
}
