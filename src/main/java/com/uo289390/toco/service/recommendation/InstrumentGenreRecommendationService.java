package com.uo289390.toco.service.recommendation;

import com.uo289390.toco.model.InstrumentGenre;

import java.util.SequencedSet;

public interface InstrumentGenreRecommendationService {
    SequencedSet<InstrumentGenre> findForItemList(Long id, int limit);

    SequencedSet<InstrumentGenre> findForUser(Long id, int recommendedInstrumentGenresCount);

    /**
     * Instrument-genres that combine one of the user's favourite genres with one of the user's favourite
     * instruments and are not in too many of the user's lists (the Home "Prueba esta combinación" card).
     * Those up to the user's level come first.
     *
     * @param id the ID of the user
     * @param limit the maximum number of instrument-genres
     * @return the combinations, in priority order
     */
    SequencedSet<InstrumentGenre> findFavouriteCombinationsForUser(Long id, int limit);
}

