package com.demo.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.demo.demo.model.Item;

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

}

