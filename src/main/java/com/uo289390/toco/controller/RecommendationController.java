package com.uo289390.toco.controller;

import com.uo289390.toco.dto.ItemDTO;
import com.uo289390.toco.dto.LessonDTO;
import com.uo289390.toco.dto.SongDTO;
import com.uo289390.toco.exception.GlobalExceptionHandler;
import com.uo289390.toco.model.Genre;
import com.uo289390.toco.model.InstrumentGenre;
import com.uo289390.toco.service.recommendation.GenreRecommendationService;
import com.uo289390.toco.service.recommendation.InstrumentGenreRecommendationService;
import com.uo289390.toco.service.recommendation.ItemRecommendationService;
import com.uo289390.toco.service.recommendation.LessonRecommendationService;
import com.uo289390.toco.service.recommendation.SongRecommendationService;
import com.uo289390.toco.utils.RecommendationConstants;
import com.uo289390.toco.utils.mapper.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for recommendations.
 *
 * Base path: {@code /api/recommendations}
 *
 * Endpoints:
 * - GET    /api/recommendations/items/by-item-list/{id}    Retrieve the full DTOs of the items recommended to add to an item list
 * - GET    /api/recommendations/instrument-genres/favourite-combinations/by-user/{id}/ids
 *                                                          Retrieve the IDs of the instrument-genres combining the user's favourites
 * - GET    /api/recommendations/genres/most-used-in-item-lists/by-user/{id}/ids
 *                                                          Retrieve the IDs of the genres most used in the user's lists
 *
 * Responses: 200 OK. Errors are translated by {@link GlobalExceptionHandler}
 * (404 Not Found when the item list does not exist).
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2026-10-04
 */
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final ItemRecommendationService itemRecommendationService;
    private final LessonRecommendationService lessonRecommendationService;
    private final SongRecommendationService songRecommendationService;
    private final InstrumentGenreRecommendationService instrumentGenreRecommendationService;
    private final GenreRecommendationService genreRecommendationService;
    private final DtoMapper dtoMapper;

    /**
     * Retrieves the items recommended to add to an item list (the "Para ti" section of the
     * add-item screen). Returns full DTOs because the user is expected to open the items
     * right after adding them.
     *
     * @param id the ID of the item list
     * @return 200 OK with the full DTOs of the recommended items (each one of its own subtype,
     *         with {@code type}), in priority order
     */
    @GetMapping("/items/by-item-list/{id}")
    @PreAuthorize("hasRole('ADMIN') or @ownership.ownsItemList(#id, authentication)")
    public ResponseEntity<List<ItemDTO>> getForItemList(@PathVariable Long id) {
        return ResponseEntity.ok(itemRecommendationService.findForItemList(id));
    }

    @GetMapping("/items/by-user/{id}")
    @PreAuthorize("hasRole('ADMIN') or @ownership.isSelf(#id, authentication)")
    public ResponseEntity<List<ItemDTO>> getForUser(@PathVariable Long id) {
        return ResponseEntity.ok(itemRecommendationService.findForUser(id));
    }

    @GetMapping("/lessons/by-user/{id}")
    @PreAuthorize("hasRole('ADMIN') or @ownership.isSelf(#id, authentication)")
    public ResponseEntity<List<LessonDTO>> getLessonsForUser(@PathVariable Long id) {
        return ResponseEntity.ok(lessonRecommendationService.findForUser(id, RecommendationConstants.RECOMMENDED_LESSONS_COUNT)
                .stream().map(dtoMapper::lessonToLessonDto).toList());
    }

    @GetMapping("/lessons/by-genre/{id}")
    public ResponseEntity<List<LessonDTO>> getForGenre(@PathVariable Long id) {
        return ResponseEntity.ok(lessonRecommendationService.findForGenre(id, RecommendationConstants.BY_GENRE_RECOMMENDED_LESSONS_COUNT));
    }

    @GetMapping("/songs/by-user/{id}")
    @PreAuthorize("hasRole('ADMIN') or @ownership.isSelf(#id, authentication)")
    public ResponseEntity<List<SongDTO>> getSongsForUser(@PathVariable Long id, @RequestParam int limit) {
        return ResponseEntity.ok(songRecommendationService.findForUser(id, limit)
                .stream().map(dtoMapper::songToSongDto).toList());
    }

    /**
     * Retrieves the instrument-genres that combine one of the user's favourite genres with one of the
     * user's favourite instruments and are not in the user's lists (the Home "Prueba esta combinación" card).
     * Returns only IDs: the cards are drawn with the item catalog.
     *
     * @param id the ID of the user
     * @return 200 OK with the IDs of the combinations, in priority order (those up to the user's level first)
     */
    @GetMapping("/instrument-genres/favourite-combinations/by-user/{id}/ids")
    @PreAuthorize("hasRole('ADMIN') or @ownership.isSelf(#id, authentication)")
    public ResponseEntity<List<Long>> getFavouriteInstrumentGenreCombinationIds(@PathVariable Long id) {
        return ResponseEntity.ok(instrumentGenreRecommendationService
                .findFavouriteCombinationsForUser(id, RecommendationConstants.FAVOURITE_INSTRUMENT_GENRE_COMBINATIONS_COUNT)
                .stream().map(InstrumentGenre::getId).toList());
    }

    /**
     * Retrieves the genres most used across the user's lists (by the genres of their songs, instrument-genres
     * and lessons), completed with the user's favourite genres, that are not themselves in any of the user's
     * lists (the Home "Explora este género" card). Returns only IDs: the cards are drawn with the item catalog.
     *
     * @param id the ID of the user
     * @return 200 OK with the IDs of the genres: first those of the lists, from most to least used, then the favourites
     */
    @GetMapping("/genres/most-used-in-item-lists/by-user/{id}/ids")
    @PreAuthorize("hasRole('ADMIN') or @ownership.isSelf(#id, authentication)")
    public ResponseEntity<List<Long>> getMostUsedGenreInItemListsIds(@PathVariable Long id) {
        return ResponseEntity.ok(genreRecommendationService
                .findMostUsedInItemListsForUser(id, RecommendationConstants.MOST_USED_GENRES_IN_ITEM_LISTS_COUNT)
                .stream().map(Genre::getId).toList());
    }
}
