import { useMemo } from 'react';
import { useLessonEntitiesStore } from '../stores/entities';

export const useLessonsByGenre = (genreId: string) => {
    const allLessons = useLessonEntitiesStore((state) => state.lessons);

    const lessons = useMemo(() => {
        return Object.values(allLessons)
            .filter((lesson) => lesson.genreId === genreId)
        // Podrías filtrar aquí si son lecciones de "intro", etc.
    }, [allLessons, genreId]);

    return { lessons };
};