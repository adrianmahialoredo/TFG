import { useCallback } from 'react';
import { useGenreEntitiesStore, useInstrumentEntitiesStore } from '../stores/entities';
import { ItemSummaryDTO } from '../types';
import { itemToListItem } from '../utils/mappers.utils';

/**
 * `itemToListItem` con los nombres de géneros e instrumentos de sus Stores (se cargan al arrancar),
 * para que las lecciones lleven su instrumento o género en el subtítulo.
 */
export const useItemToListItem = () => {
  const genres = useGenreEntitiesStore((state) => state.genres);
  const instruments = useInstrumentEntitiesStore((state) => state.instruments);

  return useCallback(
    (item: ItemSummaryDTO) => itemToListItem(item, { genres, instruments }),
    [genres, instruments]
  );
};
