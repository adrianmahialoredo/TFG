package com.demo.demo.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.demo.demo.model.Item;

import java.util.List;

/**
 * Repository interface for managing Item entities.
 *
 * This interface extends JpaRepository to provide CRUD operations
 * for Item entities in the database.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByTitleContainingIgnoreCase(String title);

    /**
     * Finds the most recently created items, used as "recommended" content
     * on the Add-to-list screen.
     *
     * @return a list with the 20 newest items ordered from newest to oldest
     */
    List<Item> findTop20ByOrderByCreatedAtDesc();

    /**
     * Finds the items that have been accessed at least once
     * (i.e. with a non-null {@code lasttimeaccess}), ordered from the most
     * recently accessed to the oldest. The amount of items returned is limited
     * through the {@link Pageable} argument.
     *
     * @param pageable pagination/limit information (page 0, size = item count)
     * @return a list with the most recently accessed items
     */
    List<Item> findByLastTimeAccessedIsNotNullOrderByLastTimeAccessedDesc(Pageable pageable);

    /**
     * Returns the most recently accessed items for the Home "Recientes" section.
     *
     * @param count the maximum number of recent items to return
     * @return a list with at most {@code count} recently accessed items
     */
    default List<Item> findRecentItems(int count) {
        return findByLastTimeAccessedIsNotNullOrderByLastTimeAccessedDesc(PageRequest.of(0, count));
    }

    /**
     * Returns a list of recommended items for the Home "Recomendaciones" section.
     *
     * @todo (esqueleto) Implementar el algoritmo real de recomendación
     *       basado en las preferencias / historial del usuario.
     *
     * @param count the maximum number of recommended items to return
     * @return a list of recommended items (currently empty)
     */
    default List<Item> findRecommendations(int count) {
        // TODO: lógica de recomendación pendiente de implementar.
        return List.of();
    }
}
