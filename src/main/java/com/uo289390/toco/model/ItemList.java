package com.uo289390.toco.model;

import java.util.*;
import java.util.function.Function;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import com.uo289390.toco.utils.Identifiable;

import static com.uo289390.toco.utils.Utils.getTopNMostRepeated;

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
     * Replaces the items of the list keeping their order and discarding duplicates.
     * The list keeps a {@link List} (instead of a {@link Set}) to preserve the order in which
     * the items were added, but it never contains the same item twice.
     *
     * @param items the new items of the list
     */
    public void setItems(Collection<? extends Item> items) {
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(new LinkedHashSet<>(items));
    }

    /**
     * Add an item to the item list if it is not already present.
     *
     * @param item the item to be added
     * @return {@code true} if the item was added, {@code false} if it was already in the list
     */
    public boolean addItem(Item item) {
        if (item == null || items.contains(item)) {
            return false;
        }
        return items.add(item);
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

    public boolean containsItem(Item item) {
        return items.contains(item);
    }

    // -------- Items by type --------

    /**
     * Returns the items of the list that are instances of the given type.
     *
     * @param type the item subclass to filter by (e.g. {@code Song.class})
     * @return a new mutable set with the matching items, in list order
     */
    public <T extends Item> Set<T> getItemsOfType(Class<T> type) {
        Set<T> result = new LinkedHashSet<>();
        for (Item item : items) {
            if (type.isInstance(item)) {
                result.add(type.cast(item));
            }
        }

        return result;
    }

    /**
     * Returns the only item of the given type if the list contains exactly one of them.
     *
     * @param type the item subclass to look for
     * @return the single matching item, or empty if there are none or more than one
     */
    public <T extends Item> Optional<T> getOnlyItemOfType(Class<T> type) {
        Set<T> matching = getItemsOfType(type);
        return matching.size() == 1 ? Optional.of(matching.iterator().next()) : Optional.empty();
    }

    public Set<Song> getSongs() {
        return getItemsOfType(Song.class);
    }

    public Set<Genre> getGenres() {
        return getItemsOfType(Genre.class);
    }

    public Optional<Genre> getOnlyGenre() {
        return getOnlyItemOfType(Genre.class);
    }

    public Optional<InstrumentGenre> getOnlyInstrumentGenre() {
        return getOnlyItemOfType(InstrumentGenre.class);
    }

    // -------- Main genres and instruments (empty when the list has none) --------

    public Optional<Genre> getMainGenreFromSongs() {
        return getTopGenresFromSongs(1).stream().findFirst();
    }

    public Optional<Genre> getMainGenreFromInstrumentGenres() {
        return getTopGenresFromInstrumentGenres(1).stream().findFirst();
    }

    public Optional<Genre> getMainGenreFromLessons() {
        return getTopGenresFromLessons(1).stream().findFirst();
    }

    public Optional<Genre> getMainGenreFromItems() {
        return getTopGenresFromItems(1).stream().findFirst();
    }

    public Optional<Instrument> getMainInstrumentFromItems() {
        return getTopInstrumentsFromItems(1).stream().findFirst();
    }

    // -------- Top genres --------

    public List<Genre> getTopGenresFromSongs(int topN) {
        return getTopN(topN, ItemList::extractGenresFromSong);
    }

    public List<Genre> getTopGenresFromInstrumentGenres(int topN) {
        return getTopN(topN, ItemList::extractGenreFromInstrumentGenre);
    }

    /**
     * Top genres of the lessons: the genre they belong to and the genre of the instrument-genre they belong to.
     */
    public List<Genre> getTopGenresFromLessons(int topN) {
        return getTopN(topN, ItemList::extractGenresFromLesson);
    }

    /**
     * Top genres counting together the genres of the songs, instrument-genres and lessons of the list.
     */
    public List<Genre> getTopGenresFromItems(int topN) {
        return getTopNMostRepeated(getGenreOccurrencesFromItems(), topN);
    }

    /**
     * Genres of the songs, instrument-genres and lessons of the list, with repetitions:
     * each genre appears once per item it comes from, so it can be used to count how much it is used.
     */
    public List<Genre> getGenreOccurrencesFromItems() {
        return extractAll(ItemList::extractGenresFromItem);
    }

    // -------- Top instruments --------

    public List<Instrument> getTopInstrumentsFromSongs(int topN) {
        return getTopN(topN, ItemList::extractInstrumentsFromSong);
    }

    /**
     * Top instruments of the lessons, taken from the instrument-genre they belong to.
     */
    public List<Instrument> getTopInstrumentsFromLessons(int topN) {
        return getTopN(topN, ItemList::extractInstrumentFromLesson);
    }

    /**
     * Top instruments counting together the instruments of the songs, instrument-genres and lessons of the list.
     * A lesson contributes the instrument of the instrument-genre it belongs to.
     */
    public List<Instrument> getTopInstrumentsFromItems(int topN) {
        return getTopNMostRepeated(getInstrumentOccurrencesFromItems(), topN);
    }

    /**
     * Instruments of the songs, instrument-genres and lessons of the list, with repetitions:
     * each instrument appears once per item it comes from, so it can be used to count how much it is used.
     */
    public List<Instrument> getInstrumentOccurrencesFromItems() {
        return extractAll(ItemList::extractInstrumentsFromItem);
    }

    // -------- Extractors --------

    private static Collection<Instrument> extractInstrumentsFromItem(Item item) {
        List<Instrument> instruments = new ArrayList<>(extractInstrumentsFromSong(item));
        instruments.addAll(extractInstrumentFromInstrumentGenre(item));
        instruments.addAll(extractInstrumentFromLesson(item));
        return instruments;
    }

    private static Collection<Genre> extractGenresFromItem(Item item) {
        List<Genre> genres = new ArrayList<>(extractGenresFromSong(item));
        genres.addAll(extractGenreFromInstrumentGenre(item));
        genres.addAll(extractGenresFromLesson(item));
        return genres;
    }

    private static Collection<Genre> extractGenresFromSong(Item item) {
        return item instanceof Song song ? song.getGenres() : List.of();
    }

    private static Collection<Genre> extractGenreFromInstrumentGenre(Item item) {
        return item instanceof InstrumentGenre instrumentGenre && instrumentGenre.getGenre() != null
                ? List.of(instrumentGenre.getGenre())
                : List.of();
    }

    /**
     * Genres of a lesson: its own genre (if it belongs to it) and the genre of its instrument-genre (if it belongs to one).
     * They are gathered in a set so a lesson whose genre and instrument-genre's genre match counts it only once.
     */
    private static Collection<Genre> extractGenresFromLesson(Item item) {
        if (!(item instanceof Lesson lesson)) {
            return List.of();
        }
        Set<Genre> genres = new LinkedHashSet<>();
        if (lesson.isBelongsToGenre() && lesson.getGenre() != null) {
            genres.add(lesson.getGenre());
        }
        instrumentGenreOf(lesson)
                .map(InstrumentGenre::getGenre)
                .ifPresent(genres::add);
        return genres;
    }

    private static Collection<Instrument> extractInstrumentFromLesson(Item item) {
        return (item instanceof Lesson lesson ? instrumentGenreOf(lesson) : Optional.<InstrumentGenre>empty())
                .map(InstrumentGenre::getInstrument)
                .map(List::of)
                .orElse(List.of());
    }

    private static Optional<InstrumentGenre> instrumentGenreOf(Lesson lesson) {
        return lesson.isBelongsToInstrumentGenre() ? Optional.ofNullable(lesson.getInstrumentGenre()) : Optional.empty();
    }

    private static Collection<Instrument> extractInstrumentsFromSong(Item item) {
        return item instanceof Song song ? song.getInstruments() : List.of();
    }

    private static Collection<Instrument> extractInstrumentFromInstrumentGenre(Item item) {
        return item instanceof InstrumentGenre instrumentGenre && instrumentGenre.getInstrument() != null
                ? List.of(instrumentGenre.getInstrument())
                : List.of();
    }

    /**
     * Ranks the values extracted from the items by how many times they appear.
     * The extracted values are gathered in a {@link List} on purpose: repetitions are what is being counted.
     * The result is ordered from most to least repeated and never contains duplicates.
     */
    private <T extends Identifiable<?>> List<T> getTopN(int topN, Function<Item, Collection<T>> extractor) {
        return getTopNMostRepeated(extractAll(extractor), topN);
    }

    /**
     * Gathers the values extracted from every item of the list, keeping repetitions.
     */
    private <T> List<T> extractAll(Function<Item, Collection<T>> extractor) {
        List<T> extracted = new ArrayList<>();
        for (Item item : items) {
            Collection<T> values = extractor.apply(item);
            if (values != null && !values.isEmpty()) {
                extracted.addAll(values);
            }
        }
        return extracted;
    }
}
