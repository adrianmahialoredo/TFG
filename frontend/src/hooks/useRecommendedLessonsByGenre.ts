import { useState, useEffect, useMemo } from 'react';
import { useLessonEntitiesStore } from '../stores/entities';
import * as lessonRepository from '../repositories/lesson.repository';
import { LessonDTO } from '../types';

/**
 * Lecciones recomendadas de un género (endpoint de recomendaciones).
 * Guarda solo los IDs y los hidrata desde la LessonEntitiesStore, respetando el orden del backend.
 */
export const useRecommendedLessonsByGenre = (genreId: string) => {
    const allLessons = useLessonEntitiesStore((state) => state.lessons);
    const [ids, setIds] = useState<string[]>([]);

    useEffect(() => {
        setIds([]);
        if (!genreId) return;

        let cancelled = false;
        // Primero la caché (al instante) y, si la hubo, después los IDs frescos de la red
        const apply = (lessonIds: string[]) => {
            if (!cancelled) setIds(lessonIds);
        };
        lessonRepository.getRecommendedByGenreId(genreId, apply)
            .then(apply)
            .catch((err) => console.error('[useRecommendedLessonsByGenre] Error:', err));

        return () => {
            cancelled = true;
        };
    }, [genreId]);

    const lessons = useMemo(
        () => ids.map((id) => allLessons[id]).filter(Boolean) as LessonDTO[],
        [ids, allLessons]
    );

    return { lessons };
};
