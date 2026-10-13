package com.uo289390.toco.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Collectors;

public class Utils {

    /**
     * Value passed to an {@code ...IdNotIn} query when there is nothing to exclude.
     * IDs are always positive, so it never matches a real entity.
     */
    private static final List<Long> NO_EXCLUDED_IDS = List.of(-1L);

    /**
     * Prepares the IDs for an {@code ...IdNotIn} derived query.
     *
     * With an empty collection Hibernate binds a single {@code NULL} ({@code id not in (null)}),
     * which in SQL is never true, so the query would return no rows at all. Passing an impossible
     * ID instead keeps the query correct when there is nothing to exclude.
     *
     * @param ids the IDs to exclude (may be empty)
     * @return the same IDs, or a collection with an impossible ID if there are none
     */
    public static Collection<Long> idsToExclude(Collection<Long> ids) {
        return ids == null || ids.isEmpty() ? NO_EXCLUDED_IDS : ids;
    }

    public static Pageable resolvePageable(Integer limit) {
        if (limit == null) {
            return Pageable.unpaged();
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be greater than 0");
        }
        return PageRequest.of(0, limit);
    }

    public static <T extends Identifiable<?>> List<T> getTopNMostRepeated(List<T> items, int n) {
        if (items == null || items.isEmpty() || n <= 0) {
            return List.of();
        }

        return items.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Identifiable::getId))
                .values().stream()
                .sorted(Comparator.comparingInt((List<T> list) -> list.size()).reversed())
                .limit(n)
                .map(List::getFirst)
                .toList();
    }

    public static <T extends Identifiable<ID>, ID> Set<T> subtract(Collection<T> candidates, Collection<T> toRemove) {
        if (candidates == null || candidates.isEmpty()) {
            return new LinkedHashSet<>();
        }
        if (toRemove == null || toRemove.isEmpty()) {
            return new LinkedHashSet<>(candidates); // Devuelve copia para evitar efectos secundarios
        }

        // 1. Extraemos los IDs que queremos eliminar en un Set O(1)
        Set<ID> idsToRemove = toRemove.stream()
                .filter(Objects::nonNull)
                .map(Identifiable::getId)
                .collect(Collectors.toSet());

        // 2. Filtramos la lista X descartando los que coincidan con esos IDs
        return candidates.stream()
                .filter(Objects::nonNull)
                .filter(item -> !idsToRemove.contains(item.getId()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
