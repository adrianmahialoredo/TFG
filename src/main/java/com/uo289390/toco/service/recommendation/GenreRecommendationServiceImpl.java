package com.uo289390.toco.service.recommendation;

import com.uo289390.toco.model.Genre;
import com.uo289390.toco.model.ItemList;
import com.uo289390.toco.model.User;
import com.uo289390.toco.repository.GenreRepository;
import com.uo289390.toco.utils.RecommendationConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.uo289390.toco.utils.EntityResolver.resolveUser;
import static com.uo289390.toco.utils.Utils.resolvePageable;
import static com.uo289390.toco.utils.EntityResolver.resolveItemList;
import static com.uo289390.toco.utils.Utils.idsToExclude;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GenreRecommendationServiceImpl implements GenreRecommendationService {

    private final GenreRepository genreRepository;

    @Override
    public SequencedSet<Genre> findForItemList(Long id, int limit) {
        ItemList itemList = resolveItemList(id);
        SequencedSet<Genre> genres = new LinkedHashSet<>();

        // Nivel 1: se añaden el género principal de las canciones, los intrumentGenres y las lecciones
        itemList.getMainGenreFromSongs().ifPresent(genre -> addCandidate(genres, genre, itemList, limit));
        itemList.getMainGenreFromInstrumentGenres().ifPresent(genre -> addCandidate(genres, genre, itemList, limit));
        itemList.getMainGenreFromLessons().ifPresent(genre -> addCandidate(genres, genre, itemList, limit));
        if (genres.size() >= limit) {
            return genres;
        }

        // Nivel 2: completar primero con los géneros principales de las canciones, luego con los de los intrumentGenres y finalmente con los de las lecciones
        int topN = RecommendationConstants.N_GENRES_USED_IN_GENRE_RECOMMENDATION + 1;
        addCandidates(genres, itemList.getTopGenresFromSongs(topN), itemList, limit);
        addCandidates(genres, itemList.getTopGenresFromInstrumentGenres(topN), itemList, limit);
        addCandidates(genres, itemList.getTopGenresFromLessons(topN), itemList, limit);
        if (genres.size() >= limit) {
            return genres;
        }

        // Nivel 3: sacamos los generos favoritos del usuario que no esten en la playlist y que no esten ya en la lista de generos recomendados
        addCandidates(genres, itemList.getUser().getPreferences().getFavoriteGenres(), itemList, limit);
        int missing = limit - genres.size();
        if (missing <= 0) {
            return genres;
        }

        // Nivel 4: completar con aleatorios
        Set<Genre> excluding = new HashSet<>(genres);
        excluding.addAll(itemList.getGenres());
        genres.addAll(genreRepository.findByIdNotIn(
                idsToExclude(excluding.stream().map(Genre::getId).collect(Collectors.toSet())),
                resolvePageable(missing)
        ));

        return genres;
    }

    @Override
    public SequencedSet<Genre> findForUser(Long id, int limit) {
        User user = resolveUser(id);
        SequencedSet<Genre> genres = new LinkedHashSet<>();

        if (limit <= 0) {
            return genres;
        }

        // Se excluyen los generos que el usuario ya tiene en demasiadas listas (N_ITEM_LISTS_TO_EXCLUDE_FROM_RECOMMENDATIONS)
        Set<Long> excluding = user.getIdsExcludedFromRecommendationsOfType(Genre.class);

        // Candidatos de las dos fuentes, sin los excluidos y en orden de prioridad
        List<Genre> favourites = user.getPreferences().getFavoriteGenres().stream()
                .filter(genre -> !excluding.contains(genre.getId()))
                .toList();
        List<Genre> fromItemLists = user.getTopGenresFromItemLists(Integer.MAX_VALUE).stream()
                .filter(genre -> !excluding.contains(genre.getId()))
                .toList();

        // Nivel 1: la mitad de los favoritos (con limite impar, la mitad grande: con limite 1 sale un favorito)
        addCandidates(genres, favourites, genres.size() + (limit - limit / 2));

        // Nivel 2: la otra mitad de los generos mas usados en las listas del usuario
        addCandidates(genres, fromItemLists, limit);

        // Nivel 3: si una de las dos fuentes se ha quedado corta, se completa con la otra
        addCandidates(genres, favourites, limit);
        addCandidates(genres, fromItemLists, limit);

        int missing = limit - genres.size();
        if (missing <= 0) {
            return genres;
        }

        // Nivel 4: completar con aleatorios
        genres.forEach(genre -> excluding.add(genre.getId()));
        genres.addAll(genreRepository.findByIdNotIn(idsToExclude(excluding), resolvePageable(missing)));

        return genres;
    }

    @Override
    public SequencedSet<Genre> findMostUsedInItemListsForUser(Long id, int limit) {
        User user = resolveUser(id);
        SequencedSet<Genre> genres = new LinkedHashSet<>();

        if (limit <= 0) {
            return genres;
        }

        // Se excluyen los generos que el usuario ya tiene guardados en demasiadas listas
        Set<Long> excluding = user.getIdsExcludedFromRecommendationsOfType(Genre.class);

        // Nivel 1: los generos mas usados en las listas
        List<Genre> fromItemLists = user.getTopGenresFromItemLists(Integer.MAX_VALUE).stream()
                .filter(genre -> !excluding.contains(genre.getId()))
                .toList();
        addCandidates(genres, fromItemLists, limit);

        // Nivel 2 (respaldo, p.ej. sin listas): los generos favoritos del usuario
        List<Genre> favourites = user.getPreferences().getFavoriteGenres().stream()
                .filter(genre -> !excluding.contains(genre.getId()))
                .toList();
        addCandidates(genres, favourites, limit);

        return genres;
    }

    /**
     * Adds each candidate (in order) until the set reaches {@code upTo}.
     * Genres already recommended are ignored by the set itself.
     */
    private static void addCandidates(SequencedSet<Genre> genres, List<Genre> candidates, int upTo) {
        for (Genre candidate : candidates) {
            if (genres.size() >= upTo) {
                return;
            }
            genres.add(candidate);
        }
    }

    /**
     * Adds each candidate (in order) that is not already in the playlist, until the limit is reached.
     */
    private static void addCandidates(SequencedSet<Genre> genres, Collection<Genre> candidates, ItemList itemList, int limit) {
        for (Genre candidate : candidates) {
            addCandidate(genres, candidate, itemList, limit);
        }
    }

    /**
     * Adds the genre if it is not null, is not already in the playlist and the limit has not been reached.
     * Genres already recommended are ignored by the set itself.
     */
    private static void addCandidate(SequencedSet<Genre> genres, Genre candidate, ItemList itemList, int limit) {
        if (genres.size() < limit && candidate != null && !itemList.containsItem(candidate)) {
            genres.add(candidate);
        }
    }
}
