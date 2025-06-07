package com.demo.demo.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.demo.demo.model.Genre;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
    
}
