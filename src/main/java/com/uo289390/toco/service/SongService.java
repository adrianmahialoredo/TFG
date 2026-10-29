package com.uo289390.toco.service;

import com.uo289390.toco.dto.SongDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service contract for managing songs.
 *
 * Provides methods to create, read, update, and delete songs, plus several
 * read queries used to build global and genre song categories.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
public interface SongService {

    List<SongDTO> findAll();

    Optional<SongDTO> findById(Long id);

    SongDTO create(SongDTO dto);

    Optional<SongDTO> update(Long id, SongDTO dto);

    boolean delete(Long id);

    List<SongDTO> findByDecade(String decade);

    List<SongDTO> findPopular(Integer limit);

    List<SongDTO> findNew(Integer limit);

    List<SongDTO> findPopularForGenre(Long genreId);

    List<SongDTO> findClassicForGenre(Long genreId);

    List<SongDTO> findNewForGenre(Long genreId);

    /**
     * Retrieves the new songs of some artists (songs flagged {@code artistNew}), mixing the artists:
     * the first song of each artist, then the second of each one, and so on, in the order the
     * artists are given (the most important first).
     *
     * @param artists the artist names (1 to {@code MAX_N_ARTISTS_IN_SONGS_BY_ARTISTS}, compared ignoring case)
     * @param limit   maximum number of songs ({@code DEFAULT_N_SONGS_BY_ARTISTS} if null,
     *                at most {@code MAX_N_SONGS_BY_ARTISTS})
     * @return the songs, without duplicates
     * @throws IllegalArgumentException if there are no artists, too many artists or the limit is out of range
     */
    List<SongDTO> findNewForArtists(List<String> artists, Integer limit);

    /**
     * Retrieves the popular songs of some artists (songs flagged {@code artistPopular}), mixing the
     * artists like {@link #findNewForArtists(List, Integer)}.
     *
     * @param artists the artist names (1 to {@code MAX_N_ARTISTS_IN_SONGS_BY_ARTISTS}, compared ignoring case)
     * @param limit   maximum number of songs ({@code DEFAULT_N_SONGS_BY_ARTISTS} if null,
     *                at most {@code MAX_N_SONGS_BY_ARTISTS})
     * @return the songs, without duplicates
     * @throws IllegalArgumentException if there are no artists, too many artists or the limit is out of range
     */
    List<SongDTO> findPopularForArtists(List<String> artists, Integer limit);

    List<SongDTO> findByIds(List<Long> ids);
}
