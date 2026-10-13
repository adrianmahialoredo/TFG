package com.uo289390.toco.controller;

import com.uo289390.toco.dto.ItemDTO;
import com.uo289390.toco.dto.LessonDTO;
import com.uo289390.toco.dto.SongDTO;
import com.uo289390.toco.exception.GlobalExceptionHandler;
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
}
