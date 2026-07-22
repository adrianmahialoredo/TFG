import { useEffect, useMemo } from 'react';
import {
  useSongEntitiesStore,
  useSongLessonEntitiesStore,
  useChordEntitiesStore,
  useTabSectionEntitiesStore // <-- Nueva despensa
} from '../stores/entities';
import { useSongScreenStore } from '../stores/ui/songScreen.store';
import * as songRepository from '../repositories/song.repository';
import * as itemRepository from '../repositories/item.repository';
import { parseLyrics } from '../utils/song.utils';
import { ChordDTO, SongLessonDTO, TabSectionDTO } from '../types';

export const useSong = (songId: string) => {
  // 1. Acceso a las Despensas (RAM)
  const song = useSongEntitiesStore((state) => state.songs[songId]);

  const allLessonsMap = useSongLessonEntitiesStore((state) => state.songLessons);
  const allChordsMap = useChordEntitiesStore((state) => state.chords);
  // Acceso a la despensa de Tabs
  const allTabsMap = useTabSectionEntitiesStore((state) => state.tabSections);

  // 2. Estado de Pantalla
  const isLoading = useSongScreenStore((state) => state.isLoading);
  const selectedDifficulty = useSongScreenStore((state) => state.selectedDifficulty);
  const setLoading = useSongScreenStore((state) => state.setLoading);
  const setDifficulty = useSongScreenStore((state) => state.setDifficulty);

  // 3. Derivados estables: IDs -> DTOs (sin snapshots nuevos en cada render)
  const allLessons = useMemo<SongLessonDTO[]>(() => {
    if (!song?.lessonIds?.length) return [];

    return song.lessonIds
        .map((id) => allLessonsMap[id])
        .filter((lesson): lesson is SongLessonDTO => Boolean(lesson));
  }, [song?.lessonIds, allLessonsMap]);

  // 4. Carga inicial
  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      await songRepository.getById(songId);
      setLoading(false);
    };
    loadData();
    // Marca el item como accedido para la sección "Recientes" del Home (fire-and-forget)
    if (songId) itemRepository.markAsAccessed(songId);
  }, [songId]);

  // 5. Lógica Derivada: Lección seleccionada
  const currentDifficulty = selectedDifficulty || allLessons[0]?.difficulty;

  const selectedLesson = useMemo<SongLessonDTO | undefined>(() => {
    if (allLessons.length === 0) return undefined;
    return allLessons.find((lesson) => lesson.difficulty === currentDifficulty) || allLessons[0];
  }, [allLessons, currentDifficulty]);

  // 6. Hidratación de Entidades relacionadas (Chords y Tabs)
  const chords = useMemo<ChordDTO[]>(() => {
    if (!selectedLesson?.chordIds?.length) return [];

    return selectedLesson.chordIds
        .map((id) => allChordsMap[id])
        .filter((chord): chord is ChordDTO => Boolean(chord));
  }, [selectedLesson?.chordIds, allChordsMap]);

  const tabSections = useMemo<TabSectionDTO[]>(() => {
    if (!selectedLesson?.tabSectionIds?.length) return [];

    return selectedLesson.tabSectionIds
        .map((id) => allTabsMap[id])
        .filter((tabSection): tabSection is TabSectionDTO => Boolean(tabSection));
  }, [selectedLesson?.tabSectionIds, allTabsMap]);

  // 7. Parseo de letras
  const parsedLyrics = useMemo(() => {
    if (!selectedLesson?.chordedLyrics) return [];
    return parseLyrics(selectedLesson.chordedLyrics);
  }, [selectedLesson?.chordedLyrics]);

  return {
    song,
    loading: isLoading,
    selectedLesson,
    chords,
    tabSections,
    availableDifficulties: [...new Set(allLessons.map((lesson) => lesson.difficulty))],
    changeDifficulty: setDifficulty,
    parsedLyrics,
  };
};