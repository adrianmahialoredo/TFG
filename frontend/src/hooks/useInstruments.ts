import { useEffect, useMemo, useState } from "react";
import * as instrumentRepository from "../repositories/instrument.repository";
import { useInstrumentEntitiesStore } from "../stores/entities";

export const useInstruments = (query: string = "") => {
    const [loading, setLoading] = useState(true);

    const instrumentsMap = useInstrumentEntitiesStore(state => state.instruments);
    const allInstruments = useMemo(() => Object.values(instrumentsMap), [instrumentsMap]);

    useEffect(() => {
        const load = async () => {
            setLoading(true);
            try {
                await instrumentRepository.getAll();
            } catch (error) {
                console.error('[useInstruments] Error cargando instrumentos:', error);
            } finally {
                setLoading(false);
            }
        };
        load();
    }, []);

    const instruments = useMemo(() => {
        if (!query.trim()) return allInstruments;
        return allInstruments.filter(i =>
            i.name.toLowerCase().includes(query.toLowerCase())
        );
    }, [allInstruments, query]);

    return { instruments, loading };
};

/**
 * Resuelve una lista de IDs de instrumentos contra la Entity Store,
 * trayendo los que falten vía Repository (Disco -> API).
 */
export const useInstrumentsByIds = (ids: (string | number)[]) => {
    const instrumentsMap = useInstrumentEntitiesStore(state => state.instruments);
    const key = ids.map(String).join(',');

    useEffect(() => {
        const missing = ids.map(String).filter(id => !instrumentsMap[id]);
        if (missing.length > 0) instrumentRepository.getAllById(missing).catch(console.error);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [key]);

    return useMemo(
        () => ids.map(id => instrumentsMap[String(id)]).filter(Boolean),
        // eslint-disable-next-line react-hooks/exhaustive-deps
        [key, instrumentsMap]
    );
};
