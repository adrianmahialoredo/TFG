import { useEffect, useMemo } from 'react';
import { useGenreEntitiesStore, useInstrumentEntitiesStore, useItemEntitiesStore } from '../stores/entities';
import * as itemRepository from '../repositories/item.repository';
import * as genreRepository from '../repositories/genre.repository';
import * as instrumentRepository from '../repositories/instrument.repository';
import { getActiveSearchVocabularies } from '../constants/search';
import { ItemSummaryDTO, ItemType } from '../types';
import { createSearchMatcher, SearchField } from '../utils/search.utils';

/** Resultados que coinciden por un mismo campo. */
export interface SearchSection {
  field: SearchField;
  items: ItemSummaryDTO[];
}

/**
 * Busca en todo el catálogo de items de un tipo (canciones o lecciones).
 *
 * Los resultados se agrupan por el campo con el que coinciden, en el orden de prioridad de
 * `fields`; cada item aparece una sola vez, en el grupo de más prioridad. Dentro de cada grupo
 * van primero los que empiezan por la búsqueda y después por título.
 *
 * Los metadatos de cada item (géneros, instrumentos, década) salen de su resumen en la
 * ItemEntitiesStore, y los nombres de los géneros e instrumentos de sus Stores.
 *
 * @returns Los grupos con resultados, en orden de prioridad (vacío si no hay búsqueda)
 */
export const useCatalogSearch = (query: string, type: ItemType, fields: SearchField[]): SearchSection[] => {
  const summaries = useItemEntitiesStore((state) => state.items);
  const genres = useGenreEntitiesStore((state) => state.genres);
  const instruments = useInstrumentEntitiesStore((state) => state.instruments);
  const vocabularies = useMemo(getActiveSearchVocabularies, []);
  const fieldsKey = fields.join(',');

  // Catálogo y nombres de géneros e instrumentos (se cargan al arrancar; aquí por si aún no están)
  useEffect(() => {
    itemRepository.getCatalog().catch((err) => console.error('[useCatalogSearch] Error cargando el catálogo:', err));
    genreRepository.getAll().catch((err) => console.error('[useCatalogSearch] Error cargando géneros:', err));
    instrumentRepository.getAll().catch((err) => console.error('[useCatalogSearch] Error cargando instrumentos:', err));
  }, []);

  return useMemo(() => {
    const sectionFields = fieldsKey.split(',') as SearchField[];
    const match = createSearchMatcher(query, sectionFields, vocabularies);
    if (!match) return [];

    const matches = Object.values(summaries)
      .filter((item) => item.type === type)
      .map((item) => ({
        item,
        match: match({
          title: item.title,
          artist: item.artist,
          genres: (item.genreIds ?? []).map((id) => genres[String(id)]?.title).filter(Boolean),
          instruments: (item.instrumentIds ?? []).map((id) => instruments[String(id)]?.name).filter(Boolean),
          decade: item.decade,
        }),
      }));

    return sectionFields
      .map((field) => ({
        field,
        items: matches
          .filter(({ match }) => match?.field === field)
          .sort((a, b) => b.match!.strength - a.match!.strength || a.item.title.localeCompare(b.item.title))
          .map(({ item }) => item),
      }))
      .filter((section) => section.items.length > 0);
  }, [query, type, fieldsKey, vocabularies, summaries, genres, instruments]);
};
