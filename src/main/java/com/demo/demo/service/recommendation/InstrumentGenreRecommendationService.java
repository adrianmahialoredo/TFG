package com.demo.demo.service.recommendation;

import com.demo.demo.model.InstrumentGenre;

import java.util.SequencedSet;

public interface InstrumentGenreRecommendationService {
    SequencedSet<InstrumentGenre> findForItemList(Long id, int limit);
}

