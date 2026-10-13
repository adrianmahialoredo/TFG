package com.uo289390.toco.service.recommendation;

import com.uo289390.toco.dto.LessonDTO;
import com.uo289390.toco.model.Lesson;

import java.util.List;
import java.util.SequencedSet;

public interface LessonRecommendationService {
    SequencedSet<Lesson> findForItemList(Long id, int limit);

    List<LessonDTO> findForGenre(Long id, int limit);

    SequencedSet<Lesson> findForUser(Long id, int recommendedLessonsCount);
}

