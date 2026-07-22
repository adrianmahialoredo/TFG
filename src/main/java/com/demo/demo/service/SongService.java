package com.demo.demo.service;

import com.demo.demo.dto.SongDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service contract for managing songs.
 *
 * Provides methods to create, read, update, and delete songs, plus several
 * read queries used to build the genre and Home screens.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
public interface SongService {

    List<SongDTO> getAllSongs();

    Optional<SongDTO> getSongById(Long id);

    SongDTO createSong(SongDTO dto);

    Optional<SongDTO> updateSong(Long id, SongDTO dto);

    boolean deleteSong(Long id);

    List<SongDTO> getSongsByDecade(String decade);

    List<SongDTO> getPopularSongs();

    List<Long> getPopularSongsWithLimit(int limit);

    List<SongDTO> getPopularSongsByGenre(Long genreId);

    List<SongDTO> getClassicSongsByGenre(Long genreId);

    List<SongDTO> getNewSongsByGenre(Long genreId);

    List<SongDTO> getPopularByGenreId(Long genreId);

    List<SongDTO> getClassicByGenreId(Long genreId);

    List<SongDTO> getNewByGenreId(Long genreId);

    List<SongDTO> getSongsByIds(List<Long> ids);

    List<SongDTO> getHomeSongsPopular();

    List<SongDTO> getHomeSongsNew();

    List<SongDTO> getHomeSongsRecommended();

    List<SongDTO> getHomeSongsFromSpotify();
}
