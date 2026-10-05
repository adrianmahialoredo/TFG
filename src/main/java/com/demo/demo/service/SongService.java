package com.demo.demo.service;

import com.demo.demo.dto.SongDTO;

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

    List<SongDTO> findByIds(List<Long> ids);
}
