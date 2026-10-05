import { useState, useEffect, useMemo } from 'react';
import { useItemEntitiesStore } from '../stores/entities';
import * as songRepository from '../repositories/song.repository';
import { CarouselItemDTO } from '../types';
import { itemsToCarouselItems } from '../utils/mappers.utils';

export const useGenreSongs = (genreId: string) => {
  // Suscripción reactiva a la Store de Entidades
  const allItems = useItemEntitiesStore((state) => state.items);

  // Estados locales para los IDs (la "lista de llaves")
  const [ids, setIds] = useState({
    popular: [] as string[],
    classic: [] as string[],
    news: [] as string[]
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
          songRepository.getPopularByGenreId(genreId, setSection('popular')).then(setSection('popular')),
          songRepository.getClassicByGenreId(genreId, setSection('classic')).then(setSection('classic')),
          songRepository.getNewByGenreId(genreId, setSection('news')).then(setSection('news')),
        ]);

        setError(null);
      } catch (err) {
        console.error("[useGenreSongs] Error:", err);
        setError("Error al sincronizar canciones");
      } finally {
        setLoading(false);
      }
    };

    fetchIds();
    return () => {
      cancelled = true;
    };
  }, [genreId]);

  // HIDRATACIÓN REACTIVA CON MAPEO:
  // Si una canción cambia en la Store, estos useMemo se disparan automáticamente.
  // Los DTOs se transforman a CarouselItemDTO para reutilizar en carruseles.
  
  const populars = useMemo(() => {
    const songs = ids.popular.map(id => allItems[id]).filter(Boolean);
    return itemsToCarouselItems(songs);
  }, [ids.popular, allItems]);

  const classics = useMemo(() => {
    const songs = ids.classic.map(id => allItems[id]).filter(Boolean);
    return itemsToCarouselItems(songs);
  }, [ids.classic, allItems]);

  const news = useMemo(() => {
    const songs = ids.news.map(id => allItems[id]).filter(Boolean);
    return itemsToCarouselItems(songs);
  }, [ids.news, allItems]);

  return { 
    populars, 
    classics, 
    news, 
    loading, 
    error 
  } as const;
};



