package com.demo.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.demo.model.Lesson;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

}
