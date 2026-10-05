package com.demo.demo.service.recommendation;

import com.demo.demo.model.ItemList;
import com.demo.demo.model.Lesson;
import com.demo.demo.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.SequencedSet;

import static com.demo.demo.utils.EntityResolver.resolveItemList;
import static com.demo.demo.utils.Utils.resolvePageable;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonRecommendationServiceImpl implements LessonRecommendationService {

    private final LessonRepository lessonRepository;

    @Override
    public SequencedSet<Lesson> findForItemList(Long id, int limit) {
        ItemList itemList = resolveItemList(id);
        SequencedSet<Lesson> lessons = new LinkedHashSet<>();

        // Nivel 1 - Obtener el top genre de la lista y sacar 2/3 de las lecciones de ese género
        itemList.getTopGenresFromItems(1).stream()
                .findFirst()
                .ifPresent(topGenre -> {
                    lessons.addAll(lessonRepository.findByGenreIdAndIdNotIn(
                            topGenre.getId(),
                            itemList.getItemsOfType(Lesson.class).stream().map(Lesson::getId).toList(),
                            resolvePageable((int) (limit * 2L / 3))
                    ));
                });

        final int[] missing = {limit - lessons.size()};
        SequencedSet<Lesson> excludingLessons = new LinkedHashSet<>(lessons);

        // Nivel 2 - Sacar los generos favoritos y usar cualquiera que no sea el anterior
        itemList.getUser().getPreferences().getFavoriteGenres().stream()
                .filter(genre -> itemList.getTopGenresFromItems(1).stream().noneMatch(topGenre -> topGenre.equals(genre)))
                .forEach(favoriteGenre -> {
                    if (missing[0] > 0) {
                        lessons.addAll(lessonRepository.findByGenreIdAndIdNotIn(
                                favoriteGenre.getId(),
                                excludingLessons.stream().map(Lesson::getId).toList(),
                                resolvePageable(missing[0])
                        ));
                        missing[0] = limit - lessons.size();
                        excludingLessons.addAll(lessons);
                    }
                });

        // Nivel 3 - Completar con aleatorias
        if (missing[0] > 0) {
            lessons.addAll(lessonRepository.findByIdNotIn(
                    excludingLessons.stream().map(Lesson::getId).toList(),
                    resolvePageable(missing[0])
            ));
        }

        return lessons;
    }
}
