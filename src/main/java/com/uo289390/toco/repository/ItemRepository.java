package com.uo289390.toco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.uo289390.toco.model.Item;

import java.util.Collection;
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
     * Finds the most recently created items leaving out the given ones (the items
     * already in a list), used as "recommended" content on the Add-to-list screen.
     *
     * @param excludedIds the IDs to leave out; must not be empty (see {@code Utils.idsToExclude})
     * @return a list with up to 20 items ordered from newest to oldest
     */
    List<Item> findTop20ByIdNotInOrderByCreatedAtDesc(Collection<Long> excludedIds);
}
