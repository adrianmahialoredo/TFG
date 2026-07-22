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
            await instrumentRepository.getAll();
            setLoading(false);
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