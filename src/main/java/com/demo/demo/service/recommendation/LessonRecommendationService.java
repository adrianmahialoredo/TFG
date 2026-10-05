package com.demo.demo.service.recommendation;

import com.demo.demo.model.Lesson;

import java.util.SequencedSet;

public interface LessonRecommendationService {
    SequencedSet<Lesson> findForItemList(Long id, int limit);
}

