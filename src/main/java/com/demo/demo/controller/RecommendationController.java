package com.demo.demo.controller;

import com.demo.demo.dto.ItemDTO;
import com.demo.demo.service.recommendation.ItemRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for recommendations.
 *
 * Base path: {@code /api/recommendations}
 *
 * Endpoints:
 * - GET    /api/recommendations/items/by-item-list/{id}    Retrieve the full DTOs of the items recommended to add to an item list
 *
 * Responses: 200 OK. Errors are translated by {@link com.demo.demo.exception.GlobalExceptionHandler}
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
    public ResponseEntity<List<ItemDTO>> getForItemList(@PathVariable Long id) {
        return ResponseEntity.ok(itemRecommendationService.findForItemList(id));
    }
}
