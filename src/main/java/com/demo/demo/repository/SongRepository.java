package com.demo.demo.repository;

import com.demo.demo.model.Genre;
import com.demo.demo.model.Instrument;
import com.demo.demo.model.Song;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * Repository interface for managing Song entities.
 *
 * All the queries are derived from the method names. {@code Distinct} avoids repeated songs when
 * joining the {@code genres} / {@code instruments} collections, and {@code IdNotIn} receives the IDs
 * of the songs that must be excluded (e.g. the songs already in a playlist).
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Repository
public interface SongRepository extends JpaRepository<Song, Long>, JpaSpecificationExecutor<Song> {

    /**
     * Finds the songs of a decade.
     *
     * @param decade the decade, in the format "1990s"
     * @return the songs of the decade
     */
    List<Song> findByDecade(String decade);

    /**
     * Finds the popular songs.
     *
     * @param pageable limit of songs to return
     * @return the popular songs
     */
    List<Song> findByPopularTrue(Pageable pageable);

    /**
     * Finds the new songs.
     *
     * @param pageable limit of songs to return
     * @return the new songs
     */
    List<Song> findByGenreNewTrue(Pageable pageable);

    /**
     * Finds the popular songs of a genre.
     *
     * @param genreId the ID of the genre
     * @return the popular songs of the genre
     */
    List<Song> findDistinctByGenresIdAndGenrePopularTrue(Long genreId);

    /**
     * Finds the classic songs of a genre.
     *
     * @param genreId the ID of the genre
     * @return the classic songs of the genre
     */
    List<Song> findDistinctByGenresIdAndGenreClassicTrue(Long genreId);

    /**
     * Finds the new songs of a genre.
     *
     * @param genreId the ID of the genre
     * @return the new songs of the genre
     */
    List<Song> findDistinctByGenresIdAndGenreNewTrue(Long genreId);

    /**
     * Finds the songs of a genre that feature any of the given instruments, excluding some songs.
     *
     * @param genreId     the ID of the genre
     * @param instruments the instruments (any of them)
     * @param excludedIds the IDs of the songs to exclude
     * @param pageable    limit of songs to return
     * @return the matching songs
     */
    List<Song> findDistinctByGenresIdAndInstrumentsInAndIdNotIn(Long genreId, Collection<Instrument> instruments, Collection<Long> excludedIds, Pageable pageable);

    /**
     * Finds the songs of a genre, excluding some songs.
     *
     * @param genreId     the ID of the genre
     * @param excludedIds the IDs of the songs to exclude
     * @param pageable    limit of songs to return
     * @return the matching songs
     */
    List<Song> findDistinctByGenresIdAndIdNotIn(Long genreId, Collection<Long> excludedIds, Pageable pageable);

    /**
     * Finds the songs that feature any of the given instruments, excluding some songs.
     *
     * @param instruments the instruments (any of them)
     * @param excludedIds the IDs of the songs to exclude
     * @param pageable    limit of songs to return
     * @return the matching songs
     */
    List<Song> findDistinctByInstrumentsInAndIdNotIn(Collection<Instrument> instruments, Collection<Long> excludedIds, Pageable pageable);

    /**
     * Finds the songs of any of the given genres that feature any of the given instruments, excluding some songs.
     *
     * @param genres      the genres (any of them)
     * @param instruments the instruments (any of them)
     * @param excludedIds the IDs of the songs to exclude
     * @param pageable    limit of songs to return
     * @return the matching songs
     */
    List<Song> findDistinctByGenresInAndInstrumentsInAndIdNotIn(Collection<Genre> genres, Collection<Instrument> instruments, Collection<Long> excludedIds, Pageable pageable);
}
