import { useEffect, useMemo } from 'react';
import {
  useAuthStore,
  useSongEntitiesStore,
  useSongLessonEntitiesStore,
  useChordEntitiesStore,
  useTabSectionEntitiesStore, // <-- Nueva despensa
  useUserEntitiesStore,
} from '../stores/entities';
import { useSongScreenStore } from '../stores/ui/songScreen.store';
import * as songRepository from '../repositories/song.repository';
import * as itemRepository from '../repositories/item.repository';
import { parseLyrics } from '../utils/song.utils';
import { ChordDTO, DifficultyLevel, SongLessonDTO, TabSectionDTO } from '../types';
import { ORIGINAL_LESSON_MIN_LEVEL } from '../constants/Preferences';
import { useInstrumentsByIds } from './useInstruments';

/** Orden en que se muestran las dificultades en el selector. */
const DIFFICULTY_ORDER = [DifficultyLevel.EASY, DifficultyLevel.MEDIUM, DifficultyLevel.ORIGINAL];

/** Clave del instrumento de una lección ('' si no tiene). */
const instrumentKey = (lesson: SongLessonDTO): string => (lesson.instrumentId != null ? String(lesson.instrumentId) : '');

export const useSong = (songId: string) => {
  // 1. Acceso a las Despensas (RAM)
  const song = useSongEntitiesStore((state) => state.songs[songId]);

  const allLessonsMap = useSongLessonEntitiesStore((state) => state.songLessons);
  const allChordsMap = useChordEntitiesStore((state) => state.chords);
  // Acceso a la despensa de Tabs
  const allTabsMap = useTabSectionEntitiesStore((state) => state.tabSections);

  // 2. Estado de Pantalla
  const isLoading = useSongScreenStore((state) => state.isLoading);
  const selectedInstrumentId = useSongScreenStore((state) => state.selectedInstrumentId);
  const selectedDifficulty = useSongScreenStore((state) => state.selectedDifficulty);
  const setLoading = useSongScreenStore((state) => state.setLoading);
  const setInstrument = useSongScreenStore((state) => state.setInstrument);
  const setDifficulty = useSongScreenStore((state) => state.setDifficulty);
  const resetSelection = useSongScreenStore((state) => state.reset);

  // Usuario actual: sus instrumentos favoritos y su nivel deciden la lección por defecto
  const currentUserId = useAuthStore((state) => state.currentUserId);
  const currentUser = useUserEntitiesStore((state) => (currentUserId ? state.users[currentUserId] : undefined));

  // 3. Derivados estables: IDs -> DTOs (sin snapshots nuevos en cada render)
  const allLessons = useMemo<SongLessonDTO[]>(() => {
    if (!song?.lessonIds?.length) return [];

    return song.lessonIds
        .map((id) => allLessonsMap[id])
        .filter((lesson): lesson is SongLessonDTO => Boolean(lesson));
  }, [song?.lessonIds, allLessonsMap]);

  // 4. Carga inicial
  useEffect(() => {
    // Cada canción empieza con la selección por defecto
    resetSelection();
    const loadData = async () => {
      setLoading(true);
      try {
        await songRepository.getById(songId);
      } catch (error) {
        console.error('[useSong] Error cargando la canción:', error);
      } finally {
        setLoading(false);
      }
    };
    loadData();
    // Marca el item como accedido para la sección "Recientes" del Home (fire-and-forget)
    if (songId) itemRepository.markAsAccessed(songId);
  }, [songId]);

  // 5. Lógica Derivada: Lección seleccionada (instrumento -> dificultad)

  // Instrumentos con lecciones, en el orden de las lecciones
  const availableInstrumentIds = useMemo(
    () => [...new Set(allLessons.map(instrumentKey))],
    [allLessons]
  );
  const instrumentEntities = useInstrumentsByIds(availableInstrumentIds.filter(Boolean));

  // Por defecto, el primer instrumento favorito del usuario que tenga lecciones; si no, el de la primera lección
  const favouriteInstrumentIds = currentUser?.favouriteInstrumentsIds;
  const currentInstrumentId = useMemo(() => {
    if (selectedInstrumentId !== null && availableInstrumentIds.includes(selectedInstrumentId)) {
      return selectedInstrumentId;
    }
    const favourite = (favouriteInstrumentIds ?? []).map(String).find((id) => availableInstrumentIds.includes(id));
    return favourite ?? availableInstrumentIds[0];
  }, [selectedInstrumentId, availableInstrumentIds, favouriteInstrumentIds]);

  const instrumentLessons = useMemo(
    () => allLessons.filter((lesson) => instrumentKey(lesson) === currentInstrumentId),
    [allLessons, currentInstrumentId]
  );

  const availableDifficulties = useMemo(
    () => DIFFICULTY_ORDER.filter((difficulty) => instrumentLessons.some((lesson) => lesson.difficulty === difficulty)),
    [instrumentLessons]
  );

  // Si la dificultad elegida no existe para este instrumento se usa la de por defecto:
  // la original para usuarios avanzados (si la hay) y si no, la de la primera lección
  const isAdvancedUser = (currentUser?.difficultyLevel ?? 0) >= ORIGINAL_LESSON_MIN_LEVEL;
  const currentDifficulty = useMemo(() => {
    if (selectedDifficulty && availableDifficulties.includes(selectedDifficulty)) return selectedDifficulty;
    if (isAdvancedUser && availableDifficulties.includes(DifficultyLevel.ORIGINAL)) return DifficultyLevel.ORIGINAL;
    return instrumentLessons[0]?.difficulty;
  }, [selectedDifficulty, availableDifficulties, isAdvancedUser, instrumentLessons]);

  const selectedLesson = useMemo<SongLessonDTO | undefined>(() => {
    if (instrumentLessons.length === 0) return undefined;
    return instrumentLessons.find((lesson) => lesson.difficulty === currentDifficulty) || instrumentLessons[0];
  }, [instrumentLessons, currentDifficulty]);

  // Opciones del selector de instrumento (solo tiene sentido con más de uno)
  const availableInstruments = useMemo(
    () => availableInstrumentIds.map((id) => ({
      id,
      name: instrumentEntities.find((instrument) => String(instrument.id) === id)?.name ?? 'General',
    })),
    [availableInstrumentIds, instrumentEntities]
  );

  // 6. Hidratación de Entidades relacionadas (Chords y Tabs)
  const chords = useMemo<ChordDTO[]>(() => {
    if (!selectedLesson?.chordIds?.length) return [];

    return selectedLesson.chordIds
        .map((id) => allChordsMap[id])
        .filter((chord): chord is ChordDTO => Boolean(chord));
  }, [selectedLesson?.chordIds, allChordsMap]);

  const tabSections = useMemo<TabSectionDTO[]>(() => {
    if (!selectedLesson?.tabSectionsIds?.length) return [];

    return selectedLesson.tabSectionsIds
        .map((id) => allTabsMap[id])
        .filter((tabSection): tabSection is TabSectionDTO => Boolean(tabSection));
  }, [selectedLesson?.tabSectionsIds, allTabsMap]);

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
    availableInstruments,
    selectedInstrumentId: currentInstrumentId,
    changeInstrument: setInstrument,
    availableDifficulties,
    changeDifficulty: setDifficulty,
    parsedLyrics,
  };
};