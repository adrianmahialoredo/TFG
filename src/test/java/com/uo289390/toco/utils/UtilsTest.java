package com.uo289390.toco.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.uo289390.toco.utils.Utils;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import com.uo289390.toco.model.Genre;
import com.uo289390.toco.model.Instrument;

/**
 * Unit tests for the {@link Utils} helper class.
 * Covers the excluded-ids helper, pageable resolution, top-N computation and set subtraction.
 */
class UtilsTest {

    private static Genre genre(long id) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setTitle("Genre " + id);
        return genre;
    }

    private static Instrument instrument(long id) {
        Instrument instrument = new Instrument();
        instrument.setId(id);
        instrument.setName("Instrument " + id);
        return instrument;
    }

    // -------- idsToExclude --------

    @Test
    void testIdsToExclude_NullReturnsImpossibleId() {
        // Act
        Collection<Long> result = Utils.idsToExclude(null);

        // Assert
        assertEquals(List.of(-1L), new ArrayList<>(result));
    }

    @Test
    void testIdsToExclude_EmptyReturnsImpossibleId() {
        // Act
        Collection<Long> result = Utils.idsToExclude(List.of());

        // Assert
        assertEquals(List.of(-1L), new ArrayList<>(result));
    }

    @Test
    void testIdsToExclude_NonEmptyReturnsSameCollection() {
        // Arrange
        List<Long> ids = List.of(1L, 2L, 3L);

        // Act
        Collection<Long> result = Utils.idsToExclude(ids);

        // Assert
        assertSame(ids, result);
    }

    // -------- resolvePageable --------

    @Test
    void testResolvePageable_NullIsUnpaged() {
        // Act
        Pageable pageable = Utils.resolvePageable(null);

        // Assert
        assertTrue(pageable.isUnpaged());
    }

    @Test
    void testResolvePageable_PositiveLimit() {
        // Act
        Pageable pageable = Utils.resolvePageable(7);

        // Assert
        assertTrue(pageable.isPaged());
        assertEquals(0, pageable.getPageNumber());
        assertEquals(7, pageable.getPageSize());
    }

    @Test
    void testResolvePageable_LimitOfOne() {
        // Act
        Pageable pageable = Utils.resolvePageable(1);

        // Assert
        assertEquals(1, pageable.getPageSize());
    }

    @Test
    void testResolvePageable_ZeroThrows() {
        // Act & Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Utils.resolvePageable(0));
        assertEquals("limit must be greater than 0", ex.getMessage());
    }

    @Test
    void testResolvePageable_NegativeThrows() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> Utils.resolvePageable(-5));
    }

    // -------- getTopNMostRepeated --------

    @Test
    void testGetTopNMostRepeated_NullListReturnsEmpty() {
        // Act & Assert
        assertTrue(Utils.getTopNMostRepeated(null, 3).isEmpty());
    }

    @Test
    void testGetTopNMostRepeated_EmptyListReturnsEmpty() {
        // Act & Assert
        assertTrue(Utils.<Genre>getTopNMostRepeated(List.of(), 3).isEmpty());
    }

    @Test
    void testGetTopNMostRepeated_ZeroNReturnsEmpty() {
        // Act & Assert
        assertTrue(Utils.getTopNMostRepeated(List.of(genre(1)), 0).isEmpty());
    }

    @Test
    void testGetTopNMostRepeated_NegativeNReturnsEmpty() {
        // Act & Assert
        assertTrue(Utils.getTopNMostRepeated(List.of(genre(1)), -1).isEmpty());
    }

    @Test
    void testGetTopNMostRepeated_OrdersByFrequency() {
        // Arrange: genre 2 three times, genre 3 twice, genre 1 once
        List<Genre> genres = List.of(genre(1), genre(2), genre(3), genre(2), genre(3), genre(2));

        // Act
        List<Genre> result = Utils.getTopNMostRepeated(genres, 3);

        // Assert
        assertEquals(List.of(2L, 3L, 1L), result.stream().map(Genre::getId).toList());
    }

    @Test
    void testGetTopNMostRepeated_LimitsToN() {
        // Arrange
        List<Genre> genres = List.of(genre(1), genre(2), genre(2), genre(3), genre(3), genre(3));

        // Act
        List<Genre> result = Utils.getTopNMostRepeated(genres, 2);

        // Assert
        assertEquals(List.of(3L, 2L), result.stream().map(Genre::getId).toList());
    }

    @Test
    void testGetTopNMostRepeated_NGreaterThanDistinctReturnsAllDistinct() {
        // Arrange
        List<Instrument> instruments = List.of(instrument(1), instrument(1), instrument(2));

        // Act
        List<Instrument> result = Utils.getTopNMostRepeated(instruments, 10);

        // Assert
        assertEquals(2, result.size());
        assertEquals(1L, result.getFirst().getId());
    }

    @Test
    void testGetTopNMostRepeated_IgnoresNullElements() {
        // Arrange
        List<Genre> genres = new ArrayList<>(Arrays.asList(genre(1), null, genre(1), null, genre(2)));

        // Act
        List<Genre> result = Utils.getTopNMostRepeated(genres, 5);

        // Assert
        assertEquals(List.of(1L, 2L), result.stream().map(Genre::getId).toList());
    }

    @Test
    void testGetTopNMostRepeated_ReturnsFirstOccurrenceOfEachGroup() {
        // Arrange: two distinct instances with the same id
        Genre first = genre(5);
        Genre second = genre(5);
        second.setTitle("Other");

        // Act
        List<Genre> result = Utils.getTopNMostRepeated(List.of(first, second), 1);

        // Assert
        assertSame(first, result.getFirst());
    }

    // -------- subtract --------

    @Test
    void testSubtract_NullCandidatesReturnsEmpty() {
        // Act
        Set<Genre> result = Utils.subtract(null, List.of(genre(1)));

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testSubtract_EmptyCandidatesReturnsEmpty() {
        // Act
        Set<Genre> result = Utils.subtract(List.of(), List.of(genre(1)));

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testSubtract_NullToRemoveReturnsCopyOfCandidates() {
        // Arrange
        List<Genre> candidates = List.of(genre(1), genre(2));

        // Act
        Set<Genre> result = Utils.subtract(candidates, null);

        // Assert
        assertEquals(List.of(1L, 2L), result.stream().map(Genre::getId).toList());
        // Mutating the result must not affect the source
        result.clear();
        assertEquals(2, candidates.size());
    }

    @Test
    void testSubtract_EmptyToRemoveReturnsCopyOfCandidates() {
        // Act
        Set<Genre> result = Utils.subtract(List.of(genre(1), genre(2)), List.of());

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    void testSubtract_RemovesById() {
        // Arrange: different instances, same ids
        List<Genre> candidates = List.of(genre(1), genre(2), genre(3), genre(4));
        List<Genre> toRemove = List.of(genre(2), genre(4));

        // Act
        Set<Genre> result = Utils.subtract(candidates, toRemove);

        // Assert
        assertEquals(List.of(1L, 3L), result.stream().map(Genre::getId).toList());
    }

    @Test
    void testSubtract_PreservesOrderAndIgnoresNulls() {
        // Arrange
        List<Genre> candidates = new ArrayList<>(Arrays.asList(genre(3), null, genre(1), genre(2)));
        List<Genre> toRemove = new ArrayList<>(Arrays.asList(null, genre(1)));

        // Act
        Set<Genre> result = Utils.subtract(candidates, toRemove);

        // Assert
        assertEquals(List.of(3L, 2L), result.stream().map(Genre::getId).toList());
    }

    @Test
    void testSubtract_RemovingAllReturnsEmpty() {
        // Act
        Set<Genre> result = Utils.subtract(List.of(genre(1)), List.of(genre(1)));

        // Assert
        assertTrue(result.isEmpty());
    }
}
