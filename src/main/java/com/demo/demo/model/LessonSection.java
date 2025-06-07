package com.demo.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Represents a section within a lesson in the system.
 * 
 * The LessonSection entity is used to store information about different sections
 * of a lesson, including the title, HTML content, and the order of the section.
 * 
 * Each LessonSection is associated with a specific Lesson, allowing for structured
 * content delivery within lessons.
 * 
 * This class is annotated with JPA annotations to map it to a database table,
 * and it uses Lombok annotations to reduce boilerplate code for getters, setters,
 * constructors, and other common methods.
 * 
 * @author Adrián Mahía
 * @version 1.0
 * @since 2025-06-06
 */
@Entity
@Table(name = "lesson_sections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonSection {
    
    /**
     * Unique identifier for the lesson section.
     * This is the primary key of the LessonSection entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Title of the lesson section.
     * This field is required and cannot be blank.
     */
    @NotBlank
    private String title; 

    /**
     * HTML content of the lesson section.
     * This field is stored as a large object (LOB) in the database.
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String htmlContent;

    /**
     * The lesson to which this section belongs.
     * This is a many-to-one relationship, meaning that many sections can belong to the same lesson.
     */
    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    /**
     * The order index of the section within the lesson.
     * This field is used to determine the display order of sections in a lesson.
     */
    private int orderIndex;
    
}
