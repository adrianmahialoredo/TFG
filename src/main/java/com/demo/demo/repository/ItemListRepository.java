package com.demo.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.demo.model.ItemList;

import java.util.List;

/**
 * Repository interface for managing ItemList entities.
 * 
 * This interface extends JpaRepository to provide CRUD operations
 * for ItemList entities, including methods to find ItemLists by user ID.
 * 
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Repository
public interface ItemListRepository extends JpaRepository<ItemList, Long> {
    
    /**
     * Finds all ItemLists associated with a specific user ID.
     * 
     * @param userId the ID of the user whose ItemLists are to be retrieved
     * @return a list of ItemLists associated with the given user ID
     */
    List<ItemList> findByUserId(Long userId);

}


