package com.demo.demo.model;

import java.util.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Represents a list of items associated with a user.
 * 
 * The ItemList entity is used to store information about a user's collection of items,
 * such as genres, songs, lessons or instrument-genre. It supports many-to-many relationships
 * with the Item entity, allowing users to create custom lists of items.
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
@Table(name = "item_lists")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor
public class ItemList {

    /**
     * Unique identifier for the item list.
     * This is the primary key of the ItemList entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the item list.
     * This field is required and cannot be null.
     */
    @Column(nullable = false)
    @NotBlank(message = "Item list name cannot be blank")
    @Size(min = 2, max = 100, message = "Item list name must be between 2 and 100 characters")
    private String name;

    /**
     * URL of the image representing the item list.
     * This field is optional and can be null.
     */
    @Column(nullable = true)
    @Pattern(regexp = "^https?://.+|^$", message = "Image URL must be a valid HTTP/HTTPS URL or empty")
    private String imageUrl;

    /**
     * User associated with the item list.
     * This is a many-to-one relationship, meaning that many item lists can belong to the same user.
     * An item list can be associated with one user, but a user can have multiple item lists.
     */
    @NotNull(message = "User cannot be null")
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * List of items in the item list.
     * This is a many-to-many relationship, meaning that an item list can contain multiple items,
     * and an item can belong to multiple item lists.
     * The items are stored in a separate table to manage the many-to-many relationship.
     */
    @ManyToMany
    @JoinTable(
        name = "itemlist_items",
        joinColumns = @JoinColumn(name = "itemlist_id"),
        inverseJoinColumns = @JoinColumn(name = "item_id")
    )
    private List<Item> items = new ArrayList<>();

    /**
     * Add an item to the item list.
     * 
     * @param item the item to be added
     */
    public void addItem(Item item) {
        items.add(item);
    }

    /**
     * Remove an item from the item list.
     * 
     * @param item the item to be removed
     */
    public void removeItem(Item item) {
        items.remove(item);
    }

    /**
     * Get the size of the item list.
     * 
     * @return the number of items in the list
     */
    public int getSize() {
        return items.size();
    }

}
