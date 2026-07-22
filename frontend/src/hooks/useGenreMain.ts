import { useEffect, useState } from 'react';
import { useGenreEntitiesStore } from '../stores/entities';
import * as genreRepository from '../repositories/genre.repository';
import * as instrumentGenreRepository from '../repositories/instrumentGenre.repository';
import * as lessonRepository from '../repositories/lesson.repository';
import * as itemRepository from '../repositories/item.repository';

// Definimos los tipos de pestañas para mayor seguridad
export type GenreTab = 'Instrumentos' | 'Canciones' | 'Género';

export const useGenreMain = (genreId: string) => {
  // 1. Suscripción a la Store de Entidades (Nivel 4)
  const genre = useGenreEntitiesStore((state) => state.genres[genreId]);

  // 2. Estado Local (Ya no usamos la store externa "pocha")
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedTab, setSelectedTab] = useState<GenreTab>('Género');

  useEffect(() => {
    const loadGenreData = async () => {
      // Si ya tenemos el género y los datos (caché en RAM),
      // podríamos evaluar no poner loading, pero por seguridad hidratamos.
      setLoading(true);
      try {
        // Ejecutamos la carga inicial y la hidratación de RAM
        await Promise.allSettled([
          genreRepository.getById(genreId),
          instrumentGenreRepository.getByGenreId(genreId),
          lessonRepository.getByGenreId(genreId),
        ]);

        // Marca el género como accedido para la sección "Recientes" del Home
        itemRepository.markAsAccessed(genreId);

        setError(null);
      } catch (err) {
        console.error("[useGenreMain] Error crítico:", err);
        setError('No se pudo cargar la información del género');
      } finally {
        setLoading(false);
      }
    };

    if (genreId) loadGenreData();
  }, [genreId]);

  return {
    genre: genre || null,
    loading,
    error,
    selectedTab,
    // Renombramos para que sea más semántico en la vista
    changeTab: setSelectedTab,
  };
};