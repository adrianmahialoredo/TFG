package com.demo.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.demo.model.ItemList;

/**
 * Repository interface for managing ItemList entities.
 *
 * This interface extends JpaRepository to provide CRUD operations
 * for ItemList entities. The lists of a user are read from {@code User.itemLists}.
 *
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Repository
public interface ItemListRepository extends JpaRepository<ItemList, Long> {
}
