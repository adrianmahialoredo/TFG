import { useState, useEffect, useMemo } from 'react';
import { useSongEntitiesStore } from '../stores/entities';
import * as songRepository from '../repositories/song.repository';
import { CarouselItemDTO } from '../types';
import { songsToCarouselItems } from '../utils/mappers.utils';

export const useGenreSongs = (genreId: string) => {
  // Suscripción reactiva a la Store de Entidades
  const allSongs = useSongEntitiesStore((state) => state.songs);

  // Estados locales para los IDs (la "lista de llaves")
  const [ids, setIds] = useState({
    popular: [] as string[],
    classic: [] as string[],
    news: [] as string[]
  });

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchIds = async () => {
      setLoading(true);
      try {
        // El repositorio ya inyecta los DTOs en la Store internamente
        const [popular, classic, news] = await Promise.all([
          songRepository.getPopularByGenreId(genreId),
          songRepository.getClassicByGenreId(genreId),
          songRepository.getNewByGenreId(genreId),
        ]);

        setIds({ popular, classic, news });
        setError(null);
      } catch (err) {
        console.error("[useGenreSongs] Error:", err);
        setError("Error al sincronizar canciones");
      } finally {
        setLoading(false);
      }
    };

    fetchIds();
  }, [genreId]);

  // HIDRATACIÓN REACTIVA CON MAPEO:
  // Si una canción cambia en la Store, estos useMemo se disparan automáticamente.
  // Los DTOs se transforman a CarouselItemDTO para reutilizar en carruseles.
  
  const populars = useMemo(() => {
    const songs = ids.popular.map(id => allSongs[id]).filter(Boolean);
    return songsToCarouselItems(songs);
  }, [ids.popular, allSongs]);

  const classics = useMemo(() => {
    const songs = ids.classic.map(id => allSongs[id]).filter(Boolean);
    return songsToCarouselItems(songs);
  }, [ids.classic, allSongs]);

  const news = useMemo(() => {
    const songs = ids.news.map(id => allSongs[id]).filter(Boolean);
    return songsToCarouselItems(songs);
  }, [ids.news, allSongs]);

  return { 
    populars, 
    classics, 
    news, 
    loading, 
    error 
  } as const;
};



