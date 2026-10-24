import { useState, useMemo } from 'react';
import { useInstrumentGenre } from './useInstrumentGenre';

export const useInstrumentGenreLesson = (instrumentGenreId: string) => {
    // Aprovechamos el hook que ya teníamos para obtener las lecciones de la RAM
    const { instrumentGenre, lessons, loading } = useInstrumentGenre(instrumentGenreId);

    // Estado para la lección activa
    const [activeLessonId, setActiveLessonId] = useState<string | null>(
        lessons[0]?.id || null
    );

    const activeLesson = useMemo(() =>
            lessons.find(l => l.id === activeLessonId) || lessons[0]
        , [activeLessonId, lessons]);

    return {
        instrumentGenre,
        lessons,
        activeLesson,
        setActiveLessonId,
        loading
    };
};