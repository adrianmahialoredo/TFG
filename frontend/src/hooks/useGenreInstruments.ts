import { useMemo, useState } from 'react';
import { useInstrumentGenreEntitiesStore } from '../stores/entities';
import { InstrumentGenreDTO } from '../types';

export const useGenreInstruments = (genreId: string) => {
    // 1. Acceso a la RAM (Entity Store)
    const allInstrumentGenres = useInstrumentGenreEntitiesStore((state) => state.instrumentGenres);

    // 2. Estado local de la UI (Selección interna)
    const [selectedIGId, setSelectedIGId] = useState<string | null>(null);

    // 3. Lógica Derivada: Filtrar InstrumentGenres por el Género actual
    const instrumentGenres = useMemo(() => {
        return Object.values(allInstrumentGenres)
            .filter((ig) => ig.genreId === genreId)
            .sort((a, b) => (a.name ?? '').localeCompare(b.name ?? ''));
    }, [allInstrumentGenres, genreId]);

    // 4. Obtener el objeto seleccionado (si existe)
    const selectedInstrumentGenre = useMemo(() => {
        return selectedIGId ? allInstrumentGenres[selectedIGId] : null;
    }, [selectedIGId, allInstrumentGenres]);

    return {
        instrumentGenres,
        selectedInstrumentGenre,
        selectInstrument: setSelectedIGId,
        unselectInstrument: () => setSelectedIGId(null),
    };
};