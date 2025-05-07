package com.demo.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.demo.model.Item;
import com.demo.demo.model.Item.Type;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // Obtener todos los Items ordenados por nombre
    List<Item> findAllByOrderByNameAsc();

    List<Item> findAllByOrderByNameDesc();

    List<Item> findAllByTypeOrderByNameAsc(Type song);

    List<Item> findAllByTypeOrderByNameDesc(Type song);
}

