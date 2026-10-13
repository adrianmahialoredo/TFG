package com.uo289390.toco.repository;

import com.uo289390.toco.model.Genre;
import com.uo289390.toco.model.Instrument;
import com.uo289390.toco.model.Song;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.Predicate;

import java.util.Collection;
import java.util.List;

/**
 * Repository interface for managing Song entities.
 *
 * All the queries are derived from the method names, except {@link #findByTitleAndArtistIn}, which
 * is built with a {@code Specification}. {@code Distinct} avoids repeated songs when
 * filtering a collection with {@code In} (a song matching several of the given genres / instruments
 * would appear once per match); filtering by a single {@code GenresId} cannot repeat songs, because
 * the join table has a primary key. {@code IdNotIn} receives the IDs
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
    List<Song> findByGenresIdAndGenrePopularTrue(Long genreId);

    /**
     * Finds the classic songs of a genre.
     *
     * @param genreId the ID of the genre
     * @return the classic songs of the genre
     */
    List<Song> findByGenresIdAndGenreClassicTrue(Long genreId);

    /**
     * Finds the new songs of a genre.
     *
     * @param genreId the ID of the genre
     * @return the new songs of the genre
     */
    List<Song> findByGenresIdAndGenreNewTrue(Long genreId);

    /**
     * Finds the popular songs of an artist (ignoring case), excluding some songs.
     *
     * @param artist      the name of the artist
     * @param excludedIds the IDs of the songs to exclude
     * @param pageable    limit of songs to return
     * @return the matching songs
     */
    List<Song> findByArtistIgnoreCaseAndArtistPopularTrueAndIdNotIn(String artist, Collection<Long> excludedIds, Pageable pageable);

    /**
     * Finds the classic songs of an artist (ignoring case), excluding some songs.
     *
     * @param artist      the name of the artist
     * @param excludedIds the IDs of the songs to exclude
     * @param pageable    limit of songs to return
     * @return the matching songs
     */
    List<Song> findByArtistIgnoreCaseAndArtistClassicTrueAndIdNotIn(String artist, Collection<Long> excludedIds, Pageable pageable);

    /**
     * Finds the new songs of an artist (ignoring case), excluding some songs.
     *
     * @param artist      the name of the artist
     * @param excludedIds the IDs of the songs to exclude
     * @param pageable    limit of songs to return
     * @return the matching songs
     */
    List<Song> findByArtistIgnoreCaseAndArtistNewTrueAndIdNotIn(String artist, Collection<Long> excludedIds, Pageable pageable);

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
    List<Song> findByGenresIdAndIdNotIn(Long genreId, Collection<Long> excludedIds, Pageable pageable);

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
     * Finds the songs that feature the given instrument, excluding some songs.
     *
     * @param instrumentId the ID of the instrument
     * @param excludedIds  the IDs of the songs to exclude
     * @param pageable     limit of songs to return
     * @return the matching songs
     */
    List<Song> findByInstrumentsIdAndIdNotIn(Long instrumentId, Collection<Long> excludedIds, Pageable pageable);

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

    List<Song> findByGenresIdAndLevelLessThanEqualAndIdNotIn(Long id, int level, Collection<Long> ids, Pageable pageable);

    List<Song> findByInstrumentsIdAndLevelLessThanEqualAndIdNotIn(Long id, int level, Collection<Long> ids, Pageable pageable);

    /**
     * Title and artist of a song, as Spotify identifies it.
     */
    record TitleArtist(String title, String artist) {}

    /**
     * Finds in a single query the songs that match any of the (title, artist) pairs, ignoring case.
     *
     * It cannot be derived from the method name, because it is an OR of a variable number of
     * pairs, so it is built with a {@code Specification}.
     *
     * @param pairs the pairs to look for
     * @return the songs found, in no particular order; empty if there are no pairs (no query is run)
     */
    default List<Song> findByTitleAndArtistIn(Collection<TitleArtist> pairs) {
        if (pairs.isEmpty()) {
            return List.of();
        }
        return findAll((root, query, cb) -> cb.or(pairs.stream()
                .map(pair -> cb.and(
                        cb.equal(cb.lower(root.get("title")), pair.title().toLowerCase()),
                        cb.equal(cb.lower(root.get("artist")), pair.artist().toLowerCase())))
                .toArray(Predicate[]::new)));
    }
}
