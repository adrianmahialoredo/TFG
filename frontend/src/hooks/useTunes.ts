import { useEffect, useMemo, useState } from 'react';
import * as tuneRepository from '../repositories/tune.repository';
import { useTuneEntitiesStore } from '../stores/entities';
import { useInstrumentsByIds } from './useInstruments';
import { useAuth } from '../context/AuthContext';
import { TuneDTO } from '../types';

/** Afinación lista para pintar: "Guitarra (Drop D)". */
export type TuneOption = TuneDTO & {
  instrumentName: string;
  label: string;
};

/**
 * Afinaciones del afinador, ordenadas por instrumento (alfabéticamente) y, dentro de cada uno,
 * en el orden del catálogo (la estándar primero).
 *
 * `defaultTuneId`: la primera afinación de uno de los instrumentos favoritos del usuario
 * (o la primera de la lista si no tiene ninguno con afinaciones).
 */
export const useTunes = () => {
  const [tuneIds, setTuneIds] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);

  const tunesMap = useTuneEntitiesStore((state) => state.tunes);
  const { user } = useAuth();

  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      setLoading(true);
      try {
        const ids = await tuneRepository.getAll((fresh) => {
          if (!cancelled) setTuneIds(fresh);
        });
        if (!cancelled) setTuneIds(ids);
      } catch (error) {
        console.error('[useTunes] Error cargando afinaciones:', error);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };
    load();
    return () => { cancelled = true; };
  }, []);

  const tunes = useMemo(
    () => tuneIds.map((id) => tunesMap[id]).filter((tune): tune is TuneDTO => !!tune),
    [tuneIds, tunesMap]
  );

  const instrumentIds = useMemo(() => [...new Set(tunes.map((tune) => tune.instrumentId))], [tunes]);
  const instruments = useInstrumentsByIds(instrumentIds);

  const options = useMemo<TuneOption[]>(() => {
    const names = new Map(instruments.map((instrument) => [String(instrument.id), instrument.name]));
    return tunes
      // Sin el nombre del instrumento no se sabe dónde ordenarla: aparece en cuanto llega
      .filter((tune) => names.has(tune.instrumentId))
      .map((tune) => {
        const instrumentName = names.get(tune.instrumentId)!;
        return { ...tune, instrumentName, label: `${instrumentName} (${tune.title})` };
      })
      .sort((a, b) =>
        a.instrumentName.localeCompare(b.instrumentName, 'es') || Number(a.id) - Number(b.id)
      );
  }, [tunes, instruments]);

  const favouriteInstrumentIds = (user?.favouriteInstrumentsIds ?? []).map(String);
  const favouritesKey = favouriteInstrumentIds.join(',');
  const defaultTuneId = useMemo(() => {
    const favourite = options.find((option) => favouriteInstrumentIds.includes(option.instrumentId));
    return (favourite ?? options[0])?.id ?? null;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [options, favouritesKey]);

  return { tunes: options, defaultTuneId, loading } as const;
};
