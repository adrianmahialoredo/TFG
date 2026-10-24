import { useState, useEffect, useMemo, useCallback } from 'react';
import { useAuthStore, useItemEntitiesStore, useItemListEntitiesStore, useRecentItemsStore, useUserEntitiesStore } from '../stores/entities';
import * as itemFeedRepository from '../repositories/itemFeed.repository';
import * as instrumentGenreRepository from '../repositories/instrumentGenre.repository';
import * as genreRepository from '../repositories/genre.repository';
import * as songRepository from '../repositories/song.repository';
import { itemsToCarouselItems } from '../utils/mappers.utils';
import { ItemSummaryDTO, ItemType } from '../types';
import { OnFresh } from '../utils/entityCache.utils';

/**
 * IDs de una tarjeta de sugerencia del Home (Cache-First + refresco vía `onFresh`).
 * Se vuelven a pedir cuando cambia `refreshKey`.
 */
const useSuggestionIds = (
  userId: string | null,
  fetchIds: (userId: string, onFresh: OnFresh) => Promise<string[]>,
  refreshKey: string,
  label: string,
): string[] => {
  const [suggestionIds, setSuggestionIds] = useState<string[]>([]);

  useEffect(() => {
    setSuggestionIds([]);
    if (!userId) return;

    let cancelled = false;
    const apply = (ids: string[]) => {
      if (!cancelled) setSuggestionIds(ids);
    };
    fetchIds(userId, apply)
      .then(apply)
      .catch((err) => console.error(`[useHomeItems] Error en ${label}:`, err));

    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userId, refreshKey]);

  return suggestionIds;
};

/**
 * El primer candidato del tipo indicado que no se vea ya en el Home; si todos se ven, el primero.
 */
const pickSuggestion = (
  candidateIds: string[],
  allItems: Record<string, ItemSummaryDTO>,
  type: ItemType,
  shownIds: Set<string>,
): ItemSummaryDTO | null => {
  const candidates = candidateIds
    .map((id) => allItems[id])
    .filter((item): item is ItemSummaryDTO => item?.type === type);
  return candidates.find((item) => !shownIds.has(item.id.toString())) ?? candidates[0] ?? null;
};

/**
 * Nivel 6: Hook de hidratación para el Home (HomeScreen).
 *
 * Orquesta el repositorio de items (que inyecta los DTOs en la Entity Store)
 * y mapea los IDs a objetos reactivos listos para los carruseles:
 *  - "Recientes": items que ha abierto el usuario actual, del más reciente al más antiguo.
 *    Salen de la RecentItemsStore (solo del front) y se pintan con el catálogo de items.
 *  - "Recomendaciones": items recomendados al usuario (`/recommendations/items/by-user`).
 *  - "Prueba esta combinación": un género-instrumento que combina un género y un instrumento
 *    favoritos del usuario y no está en sus listas; el primero que no salga ya en los carruseles.
 *  - "Explora este género": uno de los géneros más usados en las listas del usuario (o, si no hay,
 *    uno de sus favoritos) que no está guardado en ellas; el primero que no salga ya en los carruseles.
 *  - Artistas favoritos (los más repetidos en las canciones de sus listas): una tarjeta y un carrusel
 *    con sus canciones nuevas, y otra tarjeta y otro carrusel con las populares. La tarjeta destaca la
 *    primera canción que no salga ya en Recientes o Recomendaciones, y su carrusel muestra el resto.
 *
 * También expone `getItemById` para que la UI pueda resolver el item completo
 * (con su `type`) y decidir la navegación al pulsarlo.
 */
export const useHomeItems = () => {
  // Suscripción reactiva a la Store de Entidades de Items
  const allItems = useItemEntitiesStore((state) => state.items);
  const currentUserId = useAuthStore((state) => state.currentUserId);
  const recentIds = useRecentItemsStore((state) => (currentUserId ? state.byUser[currentUserId] : undefined));

  // Estados locales para los IDs (la "lista de llaves")
  const [ids, setIds] = useState({
    recommendations: [] as string[],
  });

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    // Cada sección se aplica por separado en cuanto llega: primero la caché
    // (al instante) y, si la hubo, después los IDs frescos de la red (`onFresh`).
    const setSection = (section: keyof typeof ids) => (sectionIds: string[]) => {
      if (cancelled) return;
      setIds((prev) => ({ ...prev, [section]: sectionIds }));
      // En cuanto hay algo que pintar se quita el spinner, sin esperar al resto.
      if (sectionIds.length > 0) setLoading(false);
    };

    const fetchIds = async () => {
      setLoading(true);
      try {
        // Los resúmenes ya están en la Store (catálogo); aquí solo guardamos los IDs
        await Promise.all([
          // Las recomendaciones son por usuario: sin sesión no se piden
          currentUserId
            ? itemFeedRepository.getRecommendedForUser(currentUserId, setSection('recommendations')).then(setSection('recommendations'))
            : Promise.resolve(),
        ]);

        setError(null);
      } catch (err) {
        console.error('[useHomeItems] Error:', err);
        setError('Error al sincronizar el inicio');
      } finally {
        setLoading(false);
      }
    };

    fetchIds();
    return () => {
      cancelled = true;
    };
  }, [currentUserId]);

  // HIDRATACIÓN REACTIVA CON MAPEO:
  // Si un item cambia en la Store, estos useMemo se disparan automáticamente.

  const recentItems = useMemo(() => {
    // Un item que ya no está en el catálogo (p. ej. borrado) simplemente no se pinta
    const items = (recentIds ?? []).map((id) => allItems[id]).filter(Boolean) as ItemSummaryDTO[];
    return itemsToCarouselItems(items);
  }, [recentIds, allItems]);

  const recommendations = useMemo(() => {
    const items = ids.recommendations.map((id) => allItems[id]).filter(Boolean) as ItemSummaryDTO[];
    return itemsToCarouselItems(items);
  }, [ids.recommendations, allItems]);

  // --- Tarjetas de sugerencia ---
  // Se vuelven a pedir si cambian los favoritos o el contenido de las listas del usuario
  // (las sugerencias excluyen lo que ya está en las listas)
  const favouritesKey = useUserEntitiesStore((state) => {
    const user = currentUserId ? state.users[currentUserId] : undefined;
    return [user?.favouriteGenresIds ?? [], user?.favouriteInstrumentsIds ?? []].map((ids) => ids.join(',')).join('|');
  });
  const itemListsKey = useItemListEntitiesStore((state) =>
    Object.values(state.itemLists)
      .filter((list) => currentUserId && list.userId?.toString() === currentUserId.toString())
      .map((list) => `${list.id}:${(list.itemIds ?? []).join(',')}`)
      .sort()
      .join('|')
  );

  const combinationIds = useSuggestionIds(
    currentUserId, instrumentGenreRepository.getFavouriteCombinations,
    `${favouritesKey}#${itemListsKey}`, 'la combinación de favoritos');
  const mostUsedGenreIds = useSuggestionIds(
    currentUserId, genreRepository.getMostUsedInItemLists,
    itemListsKey, 'el género más usado en las listas');

  // --- Artistas favoritos ---
  // Se vuelven a pedir si cambia el contenido de las listas (de ahí salen los artistas)
  const [favouriteArtistSongIds, setFavouriteArtistSongIds] = useState({
    newSongs: [] as string[],
    popularSongs: [] as string[],
  });

  useEffect(() => {
    setFavouriteArtistSongIds({ newSongs: [], popularSongs: [] });
    if (!currentUserId) return;

    let cancelled = false;
    const apply = (section: 'newSongs' | 'popularSongs') => (sectionIds: string[]) => {
      if (!cancelled) setFavouriteArtistSongIds((prev) => ({ ...prev, [section]: sectionIds }));
    };
    songRepository
      .getByFavouriteArtists(currentUserId, { newSongs: apply('newSongs'), popularSongs: apply('popularSongs') })
      .then(({ newSongs, popularSongs }) => {
        apply('newSongs')(newSongs);
        apply('popularSongs')(popularSongs);
      })
      .catch((err) => console.error('[useHomeItems] Error en las canciones de los artistas favoritos:', err));

    return () => {
      cancelled = true;
    };
  }, [currentUserId, itemListsKey]);

  // Mejor una sugerencia que no se vea ya en Recientes o Recomendaciones
  const shownIds = useMemo(
    () => new Set([...(recentIds ?? []), ...ids.recommendations]),
    [recentIds, ids.recommendations]
  );

  const newFavouriteArtistSong = useMemo(
    () => pickSuggestion(favouriteArtistSongIds.newSongs, allItems, ItemType.SONG, shownIds),
    [favouriteArtistSongIds.newSongs, allItems, shownIds]
  );

  // Una canción puede ser nueva y popular a la vez: las dos tarjetas no muestran la misma
  const popularFavouriteArtistSong = useMemo(() => {
    const candidateIds = favouriteArtistSongIds.popularSongs.filter((id) => id !== newFavouriteArtistSong?.id.toString());
    return pickSuggestion(candidateIds, allItems, ItemType.SONG, shownIds);
  }, [favouriteArtistSongIds.popularSongs, newFavouriteArtistSong, allItems, shownIds]);

  /** Carrusel de canciones sin la que ya destaca su tarjeta. */
  const toCarouselWithout = (songIds: string[], highlighted: ItemSummaryDTO | null) =>
    itemsToCarouselItems(
      songIds
        .filter((id) => id !== highlighted?.id.toString())
        .map((id) => allItems[id])
        .filter(Boolean) as ItemSummaryDTO[]
    );

  const newFavouriteArtistSongs = useMemo(
    () => toCarouselWithout(favouriteArtistSongIds.newSongs, newFavouriteArtistSong),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [favouriteArtistSongIds.newSongs, newFavouriteArtistSong, allItems]
  );

  const popularFavouriteArtistSongs = useMemo(
    () => toCarouselWithout(favouriteArtistSongIds.popularSongs, popularFavouriteArtistSong),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [favouriteArtistSongIds.popularSongs, popularFavouriteArtistSong, allItems]
  );

  const favouriteCombination = useMemo(
    () => pickSuggestion(combinationIds, allItems, ItemType.INSTRUMENT_GENRE, shownIds),
    [combinationIds, allItems, shownIds]
  );

  const mostUsedGenre = useMemo(
    () => pickSuggestion(mostUsedGenreIds, allItems, ItemType.GENRE, shownIds),
    [mostUsedGenreIds, allItems, shownIds]
  );

  // Permite a la UI resolver el item completo (con su `type`) para navegar.
  const getItemById = useCallback(
    (id: string): ItemSummaryDTO | undefined => allItems[id],
    [allItems]
  );

  return {
    recentItems,
    recommendations,
    favouriteCombination,
    mostUsedGenre,
    newFavouriteArtistSong,
    newFavouriteArtistSongs,
    popularFavouriteArtistSong,
    popularFavouriteArtistSongs,
    getItemById,
    loading,
    error,
  } as const;
};


