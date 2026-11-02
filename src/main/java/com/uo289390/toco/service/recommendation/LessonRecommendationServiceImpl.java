package com.uo289390.toco.service.recommendation;

import com.uo289390.toco.dto.LessonDTO;
import com.uo289390.toco.model.Genre;
import com.uo289390.toco.model.ItemList;
import com.uo289390.toco.model.Lesson;
import com.uo289390.toco.model.User;
import com.uo289390.toco.repository.LessonRepository;
import com.uo289390.toco.utils.Identifiable;
import com.uo289390.toco.utils.RecommendationConstants;
import com.uo289390.toco.utils.mapper.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.uo289390.toco.utils.EntityResolver.resolveItemList;
import static com.uo289390.toco.utils.EntityResolver.resolveUser;
import static com.uo289390.toco.utils.Utils.resolvePageable;
import static com.uo289390.toco.utils.Utils.idsToExclude;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonRecommendationServiceImpl implements LessonRecommendationService {

    private final LessonRepository lessonRepository;
    private final DtoMapper dtoMapper;

    @Override
    public SequencedSet<Lesson> findForItemList(Long id, int limit) {
        ItemList itemList = resolveItemList(id);
        SequencedSet<Lesson> lessons = new LinkedHashSet<>();

        if (limit <= 0) {
            return lessons;
        }

        // Se excluyen las lecciones de la playlist y las ya recomendadas (acumuladas como IDs)
        Set<Long> excluding = idsOf(itemList.getItemsOfType(Lesson.class));
        Optional<Genre> topGenre = itemList.getTopGenresFromItems(1).stream().findFirst();

        // Nivel 1 - Obtener el top genre de la lista y sacar 2/3 de las lecciones de ese género
        int byTopGenreCount = (int) (limit * 2L / 3);
        if (byTopGenreCount > 0) {
            topGenre.ifPresent(genre -> addLessons(lessons, excluding,
                    lessonRepository.findByGenreIdAndIdNotIn(genre.getId(), idsToExclude(excluding), resolvePageable(byTopGenreCount))));
        }

        // Nivel 2 - Sacar los generos favoritos y usar cualquiera que no sea el anterior
        for (Genre favoriteGenre : itemList.getUser().getPreferences().getFavoriteGenres()) {
            int missing = limit - lessons.size();
            if (missing <= 0) {
                return lessons;
            }
            if (topGenre.isPresent() && topGenre.get().equals(favoriteGenre)) {
                continue;
            }
            addLessons(lessons, excluding,
                    lessonRepository.findByGenreIdAndIdNotIn(favoriteGenre.getId(), idsToExclude(excluding), resolvePageable(missing)));
        }

        // Nivel 3 - Completar con aleatorias
        int missing = limit - lessons.size();
        if (missing > 0) {
            lessons.addAll(lessonRepository.findByIdNotIn(idsToExclude(excluding), resolvePageable(missing)));
        }

        return lessons;
    }

    @Override
    public List<LessonDTO> findForGenre(Long id, int limit) {
        return lessonRepository.findByGenreIdAndGenrePopularTrueAndBelongsToInstrumentGenreFalse(id, resolvePageable(limit))
                .stream()
                .map(dtoMapper::lessonToLessonDto)
                .toList();
    }

    @Override
    public SequencedSet<Lesson> findForUser(Long id, int limit) {
        User user = resolveUser(id);
        SequencedSet<Lesson> lessons = new LinkedHashSet<>();

        if (limit <= 0) {
            return lessons;
        }

        // Ninguna leccion que el usuario ya tenga en demasiadas listas
        Set<Long> excluding = user.getIdsExcludedFromRecommendationsOfType(Lesson.class);
        int level = user.getDifficultyLevel();

        // Candidatos: el genero/instrumento favorito principal y los mas usados en las listas del usuario
        Set<Long> genreIds = candidateIds(
                user.getPreferences().getFavoriteGenres(),
                user.getTopGenresFromItemLists(RecommendationConstants.N_ITEM_LISTS_GENRES_USED_IN_BY_USER_LESSON_RECOMMENDATION)
        );
        Set<Long> instrumentIds = candidateIds(
                user.getPreferences().getFavoriteInstruments(),
                user.getTopInstrumentsFromItemLists(RecommendationConstants.N_ITEM_LISTS_INSTRUMENTS_USED_IN_BY_USER_LESSON_RECOMMENDATION)
        );

        // La mitad del limite son lecciones de esos generos/instrumentos (repartida entre ambos, la parte
        // grande para generos) y el resto genericas, para que siempre salga alguna
        int matching = limit / 2;
        int genreQuota = genreIds.isEmpty() ? 0 : matching - matching / 2;
        int instrumentQuota = instrumentIds.isEmpty() ? 0 : matching / 2;
        int genericQuota = limit - matching;

        // Nivel 1: todas con nivel menor o igual que el del usuario
        int byGenre = fetch(lessons, excluding, genreQuota, (excluded, pageable) ->
                lessonRepository.findByGenreIdInAndBelongsToGenreTrueAndLevelLessThanEqualAndIdNotIn(genreIds, level, excluded, pageable));
        int byInstrument = fetch(lessons, excluding, instrumentQuota, (excluded, pageable) ->
                lessonRepository.findByInstrumentIdInAndBelongsToInstrumentTrueAndLevelLessThanEqualAndIdNotIn(instrumentIds, level, excluded, pageable));
        int generic = fetch(lessons, excluding, genericQuota, (excluded, pageable) ->
                lessonRepository.findByBelongsToGenreFalseAndBelongsToInstrumentFalseAndBelongsToInstrumentGenreFalseAndLevelLessThanEqualAndIdNotIn(level, excluded, pageable));

        if (lessons.size() >= limit) {
            return lessons;
        }

        // Nivel 2: lo que le falte a cada parte, sin comprobar el nivel
        fetch(lessons, excluding, genreQuota - byGenre, (excluded, pageable) ->
                lessonRepository.findByGenreIdInAndBelongsToGenreTrueAndIdNotIn(genreIds, excluded, pageable));
        fetch(lessons, excluding, instrumentQuota - byInstrument, (excluded, pageable) ->
                lessonRepository.findByInstrumentIdInAndBelongsToInstrumentTrueAndIdNotIn(instrumentIds, excluded, pageable));
        fetch(lessons, excluding, genericQuota - generic, lessonRepository::findByBelongsToGenreFalseAndBelongsToInstrumentFalseAndBelongsToInstrumentGenreFalseAndIdNotIn);

        // Nivel 3: si aun faltan (p. ej. sin candidatos de genero/instrumento sus cuotas son 0), completar
        // con cualquier leccion, como en el resto de recomendaciones
        fetch(lessons, excluding, limit - lessons.size(), lessonRepository::findByIdNotIn);

        return lessons;
    }

    /**
     * A query of lessons that receives the IDs to exclude and the limit.
     */
    @FunctionalInterface
    private interface LessonQuery {
        List<Lesson> find(Collection<Long> excludedIds, Pageable pageable);
    }

    /**
     * Runs the query for up to {@code count} lessons (if any are needed), adds them to the recommendation
     * and to the exclusions, and returns how many it added.
     */
    private static int fetch(SequencedSet<Lesson> lessons, Set<Long> excluding, int count, LessonQuery query) {
        if (count <= 0) {
            return 0;
        }
        List<Lesson> found = query.find(idsToExclude(excluding), resolvePageable(count));
        addLessons(lessons, excluding, found);
        return found.size();
    }

    /**
     * IDs of the first favourite followed by the most used ones in the user's lists, without duplicates and in that order.
     */
    private static <T extends Identifiable<Long>> Set<Long> candidateIds(Collection<T> favourites, List<T> fromItemLists) {
        Set<Long> ids = new LinkedHashSet<>();
        favourites.stream().findFirst().ifPresent(favourite -> ids.add(favourite.getId()));
        fromItemLists.forEach(entity -> ids.add(entity.getId()));
        return ids;
    }

    /**
     * Adds the found lessons to the recommendation and to the exclusions.
     */
    private static void addLessons(SequencedSet<Lesson> lessons, Set<Long> excluding, List<Lesson> found) {
        lessons.addAll(found);
        found.forEach(lesson -> excluding.add(lesson.getId()));
    }

    /**
     * The exclusions are accumulated as IDs (what the queries receive) instead of entities, so each
     * query does not have to map the whole set again.
     */
    private static Set<Long> idsOf(Collection<Lesson> lessons) {
        return lessons.stream().map(Lesson::getId).collect(Collectors.toCollection(HashSet::new));
    }
}
