package com.demo.demo.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "item_lists")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor
public class ItemList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String imageUrl;
    private boolean favourite;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // Relación con User
    private User user;

    @ManyToMany
    @JoinTable(
        name = "itemlist_items",
        joinColumns = @JoinColumn(name = "itemlist_id"),
        inverseJoinColumns = @JoinColumn(name = "item_id")
    )
    private List<Item> items = new ArrayList<>();

    public void addItem(Item item) {
        items.add(item);
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    public List<Item> getAllSongs() {
        List<Item> songs = new ArrayList<>();
        for (Item item : items) {
            if (item.getType() == Item.Type.Song)
                songs.add(item);
        }
        return songs;
    }

    public List<Item> orderByName() {
        List<Item> orderedItems = new ArrayList<>(items);
        orderedItems.sort(Comparator.comparing(Item::getName));
        return orderedItems;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public int getSize() {
        return items.size();
    }

}
