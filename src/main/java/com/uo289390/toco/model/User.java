package com.uo289390.toco.model;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Set;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

import static com.uo289390.toco.utils.Utils.getTopNMostRepeated;

/**
 * Entity representing a user.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be valid")
    private String email;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "Username can only contain letters, numbers, dots, hyphens and underscores")
    private String username;

    @Column(nullable = false)
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @Column(nullable = false)
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Pattern(regexp = "^https?://.+|^$", message = "Profile image URL must be a valid HTTP/HTTPS URL or empty")
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column(name = "difficulty_level", nullable = false)
    @Min(value = 0, message = "Difficulty level must be at least 0")
    @Max(value = 5, message = "Difficulty level must be at most 5")
    private int difficultyLevel = 1;

    /**
     * Musical preferences (favourite genres, favourite instruments and
     * difficulty level) owned by this user.
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Valid
    private Preferences preferences = new Preferences();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ItemList> itemLists = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "spotify_user_id")
    private SpotifyUser spotifyUser;

    // Timestamps
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // UserDetails methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    /**
     * Returns the user's preferences, creating them lazily if they do not exist yet.
     *
     * The preferences are never {@code null}, so callers do not need to perform
     * null checks before accessing the favourite genres or instruments.
     *
     * @return the preferences owned by this user
     */
    public Preferences getPreferences() {
        if (preferences == null) {
            preferences = new Preferences();
        }
        if (preferences.getUser() != this) {
            preferences.setUser(this);
        }
        return preferences;
    }

    /**
     * Sets the user's preferences, keeping the bidirectional relationship consistent.
     *
     * @param preferences the preferences to associate with this user, may be {@code null}
     */
    public void setPreferences(Preferences preferences) {
        this.preferences = preferences == null ? new Preferences() : preferences;
        this.preferences.setUser(this);
    }

    public boolean hasItemInLists(Item item) {
        for (ItemList itemList : itemLists) {
            if (itemList.containsItem(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the items of the given type contained in any of the user's lists.
     *
     * @param type the item subclass to filter by (e.g. {@code Song.class})
     * @return a new mutable set with the matching items, without duplicates and in list order
     */
    public <T extends Item> Set<T> getAllFromItemListsOfType(Class<T> type) {
        Set<T> result = new LinkedHashSet<>();
        for (ItemList itemList : itemLists) {
            result.addAll(itemList.getItemsOfType(type));
        }
        return result;
    }

    /**
     * Returns the IDs of the items of the given type contained in any of the user's lists.
     *
     * The by-user recommendations start their exclusions with these IDs, so that nothing the user
     * already has in a list is recommended. The exclusion is done in the query ({@code ...IdNotIn})
     * and not filtering afterwards, so that the excluded items do not take up room in the limit.
     *
     * @param type the item subclass to filter by (e.g. {@code Song.class})
     * @return a new mutable set with the IDs, so the caller can keep adding the ones it recommends
     */
    public <T extends Item> Set<Long> getIdsFromItemListsOfType(Class<T> type) {
        Set<Long> ids = new HashSet<>();
        for (T item : getAllFromItemListsOfType(type)) {
            ids.add(item.getId());
        }
        return ids;
    }

    /**
     * Returns the most used genres across all the user's lists, counting together the genres of the
     * songs, instrument-genres and lessons of every list.
     * An item that is in several lists counts once per list.
     *
     * @param topN the maximum number of genres to return
     * @return the genres ordered from most to least used, without duplicates
     */
    public List<Genre> getTopGenresFromItemLists(int topN) {
        List<Genre> genres = new ArrayList<>();
        for (ItemList itemList : itemLists) {
            genres.addAll(itemList.getGenreOccurrencesFromItems());
        }
        return getTopNMostRepeated(genres, topN);
    }

    /**
     * Returns the most used instruments across all the user's lists, counting together the instruments of the
     * songs, instrument-genres and lessons of every list.
     * An item that is in several lists counts once per list.
     *
     * @param topN the maximum number of instruments to return
     * @return the instruments ordered from most to least used, without duplicates
     */
    public List<Instrument> getTopInstrumentsFromItemLists(int topN) {
        List<Instrument> instruments = new ArrayList<>();
        for (ItemList itemList : itemLists) {
            instruments.addAll(itemList.getInstrumentOccurrencesFromItems());
        }
        return getTopNMostRepeated(instruments, topN);
    }
}
