package com.demo.demo.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.demo.demo.model.Item;

import java.util.List;
import java.util.Optional;

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
}
