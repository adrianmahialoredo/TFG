package com.demo.demo.service.recommendation;

import com.demo.demo.model.Song;

import java.util.SequencedSet;

public interface SongRecommendationService {
    SequencedSet<Song> findForItemList(Long id, int limit);
}
