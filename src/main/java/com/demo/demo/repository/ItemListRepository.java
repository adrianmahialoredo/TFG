package com.demo.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.demo.model.ItemList;

import java.util.List;

@Repository
public interface ItemListRepository extends JpaRepository<ItemList, Long> {
    
    // Obtener todas las listas de un usuario específico
    List<ItemList> findByUserId(Long userId);
}


