package com.uo289390.toco.service.recommendation;

import com.uo289390.toco.model.Genre;

import java.util.SequencedSet;

public interface GenreRecommendationService {
    SequencedSet<Genre> findForItemList(Long id, int limit);

    SequencedSet<Genre> findForUser(Long id, int recommendedGenresCount);

    /**
     * The genres most used across the user's lists (counting the genres of their songs, instrument-genres
     * and lessons), completed with the user's favourite genres, that are not themselves in any of the
     * user's lists (the Home "Explora este género" card).
     *
     * @param id the ID of the user
     * @param limit the maximum number of genres
     * @return the genres: first those of the lists, from most to least used, then the favourites
     */
    SequencedSet<Genre> findMostUsedInItemListsForUser(Long id, int limit);
}

