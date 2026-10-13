package com.uo289390.toco.service.recommendation;

import com.uo289390.toco.model.Song;

import java.util.SequencedSet;

public interface SongRecommendationService {
    SequencedSet<Song> findForItemList(Long id, int limit);

    SequencedSet<Song> findForUser(Long id, int recommendedSongsCount);
}
