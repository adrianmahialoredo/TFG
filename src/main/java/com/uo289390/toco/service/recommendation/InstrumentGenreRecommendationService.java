package com.uo289390.toco.service.recommendation;

import com.uo289390.toco.model.InstrumentGenre;

import java.util.SequencedSet;

public interface InstrumentGenreRecommendationService {
    SequencedSet<InstrumentGenre> findForItemList(Long id, int limit);

    SequencedSet<InstrumentGenre> findForUser(Long id, int recommendedInstrumentGenresCount);
}

