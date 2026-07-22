package com.demo.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
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
     * Finds all item list IDs associated with a specific user ID.
     *
     * @param userId The ID of the user.
     * @return A list of item list IDs for the user.
     */
    @Query(
        "SELECT il.id " +
        "FROM ItemList il " +
        "WHERE il.user.id = :userId"
    )
    List<Long> findIdsByUserId(@Param("userId") Long userId);

    /**
     * Finds all item lists by a list of IDs.
     *
     * @param ids The list of item list IDs to search for.
     * @return A list of ItemList entities matching the provided IDs.
     */
    @NonNull
    List<ItemList> findAllById(@NonNull Iterable<Long> ids);
}