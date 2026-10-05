package com.demo.demo.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Collectors;

public class Utils {

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
