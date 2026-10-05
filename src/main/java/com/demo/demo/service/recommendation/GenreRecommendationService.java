package com.demo.demo.service.recommendation;

import com.demo.demo.model.Genre;

import java.util.SequencedSet;

public interface GenreRecommendationService {
    SequencedSet<Genre> findForItemList(Long id, int limit);
}

