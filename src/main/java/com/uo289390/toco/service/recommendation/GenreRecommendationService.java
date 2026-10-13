package com.uo289390.toco.service.recommendation;

import com.uo289390.toco.model.Genre;

import java.util.SequencedSet;

public interface GenreRecommendationService {
    SequencedSet<Genre> findForItemList(Long id, int limit);

    SequencedSet<Genre> findForUser(Long id, int recommendedGenresCount);
}

