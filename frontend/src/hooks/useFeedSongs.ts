import { useState, useEffect, useMemo } from 'react';
import { useAuthStore, useGenreEntitiesStore, useItemEntitiesStore, useUserEntitiesStore } from '../stores/entities';
import * as songRepository from '../repositories/song.repository';
import { itemsToCarouselItems } from '../utils/mappers.utils';
import { useUserTasteKey } from './useUserTasteKey';

export const useFeedSongs = () => {
  // Suscripción reactiva a la Store de Entidades
  const allItems = useItemEntitiesStore((state) => state.items);
  const currentUserId = useAuthStore((state) => state.currentUserId);
  // undefined mientras no se sabe (usuario aún sin cargar o caché antigua sin el campo)
  const spotifyConnected = useUserEntitiesStore((state) =>
    currentUserId ? state.users[currentUserId]?.spotifyConnected : undefined
  );

  // Estados locales para los IDs (la "lista de llaves")
  const [ids, setIds] = useState({
    popular: [] as string[],
    news: [] as string[],
    recommendations: [] as string[],
    spotify: [] as string[],
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
          songRepository.getPopular(setSection('popular')).then(setSection('popular')),
          songRepository.getNew(setSection('news')).then(setSection('news')),
        ]);

        setError(null);
      } catch (err) {
        console.error("[useHomeSongs] Error:", err);
        setError("Error al sincronizar canciones");
      } finally {
        setLoading(false);
      }
    };

    fetchIds();
    return () => {
      cancelled = true;
    };
  }, []);

  // Las recomendaciones son por usuario (sin sesión no se piden) y se vuelven a pedir cuando
  // cambian sus listas, sus favoritos o su nivel, sin vaciar el carrusel mientras llegan.
  const tasteKey = useUserTasteKey();

  useEffect(() => {
    setIds((prev) => ({ ...prev, recommendations: [] }));
  }, [currentUserId]);

  useEffect(() => {
    if (!currentUserId) return;

    let cancelled = false;
    const apply = (sectionIds: string[]) => {
      if (cancelled) return;
      setIds((prev) => ({ ...prev, recommendations: sectionIds }));
      if (sectionIds.length > 0) setLoading(false);
    };
    songRepository.getRecommended(currentUserId, apply)
      .then(apply)
      .catch((err) => console.error('[useHomeSongs] Error en las recomendaciones:', err));

    return () => {
      cancelled = true;
    };
  }, [currentUserId, tasteKey]);

  // Las recomendaciones de Spotify van aparte: solo se piden con la cuenta vinculada y se
  // vuelven a pedir en cuanto el usuario la vincula (cambia `spotifyConnected`).
  useEffect(() => {
    setIds((prev) => ({ ...prev, spotify: [] }));
    if (!currentUserId || !spotifyConnected) return;

    let cancelled = false;
    const apply = (sectionIds: string[]) => {
      if (!cancelled) setIds((prev) => ({ ...prev, spotify: sectionIds }));
    };
    songRepository.getFromSpotify(currentUserId, apply)
      .then(apply)
      .catch((err) => console.error('[useFeedSongs] Error en Spotify:', err));

    return () => {
      cancelled = true;
    };
  }, [currentUserId, spotifyConnected]);

  // HIDRATACIÓN REACTIVA CON MAPEO:
  // Si una canción cambia en la Store, estos useMemo se disparan automáticamente.
  
  const populars = useMemo(() => {
    const songs = ids.popular.map(id => allItems[id]).filter(Boolean);
    return itemsToCarouselItems(songs);
  }, [ids.popular, allItems]);

  const news = useMemo(() => {
    const songs = ids.news.map(id => allItems[id]).filter(Boolean);
    return itemsToCarouselItems(songs);
  }, [ids.news, allItems]);

  const recommendations = useMemo(() => {
    const songs = ids.recommendations.map(id => allItems[id]).filter(Boolean);
    return itemsToCarouselItems(songs);
  }, [ids.recommendations, allItems]);

  const spotify = useMemo(() => {
    const songs = ids.spotify.map(id => allItems[id]).filter(Boolean);
    return itemsToCarouselItems(songs);
  }, [ids.spotify, allItems]);

  // --- Carrusel "Populares en <género favorito>" ---
  // Se elige uno de los géneros favoritos del usuario al azar, estable mientras no cambien.
  const favouriteGenreIds = useUserEntitiesStore((state) =>
    currentUserId ? state.users[currentUserId]?.favouriteGenresIds : undefined
  );
  const genres = useGenreEntitiesStore((state) => state.genres);
  const favouriteKey = (favouriteGenreIds ?? []).map(String).join(',');

  const favouriteGenreId = useMemo(() => {
    const candidates = favouriteKey ? favouriteKey.split(',') : [];
    return candidates.length > 0 ? candidates[Math.floor(Math.random() * candidates.length)] : null;
  }, [favouriteKey]);

  const [favouriteGenreSongIds, setFavouriteGenreSongIds] = useState<string[]>([]);

  useEffect(() => {
    setFavouriteGenreSongIds([]);
    if (!favouriteGenreId) return;

    let cancelled = false;
    const apply = (sectionIds: string[]) => {
      if (!cancelled) setFavouriteGenreSongIds(sectionIds);
    };
    // Mismo endpoint (y misma caché) que el carrusel "Populares" de la pantalla del género
    songRepository.getPopularByGenreId(favouriteGenreId, apply)
      .then(apply)
      .catch((err) => console.error('[useCategorizedSongs] Error en el género favorito:', err));

    return () => {
      cancelled = true;
    };
  }, [favouriteGenreId]);

  const favouriteGenre = useMemo(() => {
    if (!favouriteGenreId) return null;
    const songs = favouriteGenreSongIds.map(id => allItems[id]).filter(Boolean);
    return {
      title: genres[favouriteGenreId]?.title ?? '',
      data: itemsToCarouselItems(songs),
    };
  }, [favouriteGenreId, favouriteGenreSongIds, allItems, genres]);

  return { 
    populars, 
    news, 
    recommendations,
    spotify,
    spotifyConnected,
    favouriteGenre,
    loading, 
    error 
  } as const;
};
