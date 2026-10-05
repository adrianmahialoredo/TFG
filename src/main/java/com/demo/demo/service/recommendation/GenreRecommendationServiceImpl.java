package com.demo.demo.service.recommendation;

import com.demo.demo.model.Genre;
import com.demo.demo.model.ItemList;
import com.demo.demo.repository.GenreRepository;
import com.demo.demo.utils.RecommendationConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.SequencedSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.demo.demo.utils.Utils.resolvePageable;
import static com.demo.demo.utils.EntityResolver.resolveItemList;

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
        int topN = RecommendationConstants.NUMBER_OF_GENRES_USED_IN_GENRE_RECOMMENDATION + 1;
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
                excluding.stream().map(Genre::getId).collect(Collectors.toSet()),
                resolvePageable(missing)
        ));

        return genres;
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
