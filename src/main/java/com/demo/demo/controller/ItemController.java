package com.demo.demo.controller;

import com.demo.demo.model.Item;
import com.demo.demo.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        Item item = itemService.getItemById(id);
        if (item != null) {
            return ResponseEntity.ok(item);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/batch")
    public ResponseEntity<List<Item>> getItemsByIds(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(itemService.getItemsByIds(ids));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Item>> searchItems(@RequestParam("q") String query) {
        return ResponseEntity.ok(itemService.searchItems(query));
    }

    @GetMapping("/recommended")
    public ResponseEntity<List<Item>> getRecommendedItems() {
        return ResponseEntity.ok(itemService.getRecommendedItems());
    }

    /**
     * Returns the most recently accessed items for the Home "Recientes" section.
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Item>> getRecentItems() {
        return ResponseEntity.ok(itemService.getRecentItems());
    }

    /**
     * Returns the recommended items for the Home "Recomendaciones" section.
     */
    @GetMapping("/recommendations")
    public ResponseEntity<List<Item>> getRecommendations() {
        return ResponseEntity.ok(itemService.getRecommendations());
    }

    /**
     * Marks an item as accessed (updates its {@code lasttimeaccess}).
     * Triggered by the frontend whenever an item's detail screen is opened.
     *
     * @param id the id of the accessed item.
     * @return 200 with the updated item, or 404 if the item does not exist.
     */
    @PutMapping("/{id}/access")
    public ResponseEntity<Item> markItemAsAccessed(@PathVariable Long id) {
        Item updated = itemService.markAsAccessed(id);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }
}