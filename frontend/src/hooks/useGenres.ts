import { useEffect, useMemo, useState } from "react";
import * as genreRepository from "../repositories/genre.repository";
import { useGenreEntitiesStore } from "../stores/entities";

export const useGenres = (query: string = "") => {
    const [loading, setLoading] = useState(true);

    const genresMap = useGenreEntitiesStore(state => state.genres);
    const allGenres = useMemo(() => Object.values(genresMap), [genresMap]);

    useEffect(() => {
        const load = async () => {
            setLoading(true);
            await genreRepository.getAll();
            setLoading(false);
        };
        load();
    }, []);

    // Hacemos que la query sea opcional
    const genres = useMemo(() => {
        // Si no hay query, devolvemos todos los géneros
        if (!query.trim()) return allGenres;

        return allGenres.filter(g =>
            g.title.toLowerCase().includes(query.toLowerCase())
        );
    }, [allGenres, query]);

    return { genres, loading };
};

/**
 * Resuelve una lista de IDs de géneros contra la Entity Store,
 * trayendo los que falten vía Repository (Disco -> API).
 */
export const useGenresByIds = (ids: (string | number)[]) => {
    const genresMap = useGenreEntitiesStore(state => state.genres);
    const key = ids.map(String).join(',');

    useEffect(() => {
        const missing = ids.map(String).filter(id => !genresMap[id]);
        if (missing.length > 0) genreRepository.getAllById(missing).catch(console.error);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [key]);

    return useMemo(
        () => ids.map(id => genresMap[String(id)]).filter(Boolean),
        // eslint-disable-next-line react-hooks/exhaustive-deps
        [key, genresMap]
    );
};
