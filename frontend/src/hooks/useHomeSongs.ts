import { useState, useEffect, useMemo } from 'react';
import { useSongEntitiesStore } from '../stores/entities';
import * as songRepository from '../repositories/song.repository';
import { songsToCarouselItems } from '../utils/mappers.utils';

export const useHomeSongs = () => {
  // Suscripción reactiva a la Store de Entidades
  const allSongs = useSongEntitiesStore((state) => state.songs);

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
    const fetchIds = async () => {
      setLoading(true);
      try {
        // El repositorio ya inyecta los DTOs en la Store internamente
        const [popular, news, recommendations, spotify] = await Promise.all([
          songRepository.getPopular(),
          songRepository.getNew(),
          songRepository.getRecommended(),
          songRepository.getFromSpotify(),
        ]);

        setIds({ popular, news, recommendations, spotify });
        setError(null);
      } catch (err) {
        console.error("[useHomeSongs] Error:", err);
        setError("Error al sincronizar canciones");
      } finally {
        setLoading(false);
      }
    };

    fetchIds();
  }, []);

  // HIDRATACIÓN REACTIVA CON MAPEO:
  // Si una canción cambia en la Store, estos useMemo se disparan automáticamente.
  
  const populars = useMemo(() => {
    const songs = ids.popular.map(id => allSongs[id]).filter(Boolean);
    return songsToCarouselItems(songs);
  }, [ids.popular, allSongs]);

  const news = useMemo(() => {
    const songs = ids.news.map(id => allSongs[id]).filter(Boolean);
    return songsToCarouselItems(songs);
  }, [ids.news, allSongs]);

  const recommendations = useMemo(() => {
    const songs = ids.recommendations.map(id => allSongs[id]).filter(Boolean);
    return songsToCarouselItems(songs);
  }, [ids.recommendations, allSongs]);

  const spotify = useMemo(() => {
    const songs = ids.spotify.map(id => allSongs[id]).filter(Boolean);
    return songsToCarouselItems(songs);
  }, [ids.spotify, allSongs]);

  return { 
    populars, 
    news, 
    recommendations,
    spotify,
    loading, 
    error 
  } as const;
};
