import { useState, useEffect } from 'react';
import { SongDTO, SongLessonDTO, ChordDTO, TabSectionDTO } from '../types';
import { getSongById } from '../services';
import * as songStorage from '../local/song.local';
import * as songLessonRepository from '../repositories/songLesson.repository';
import * as chordRepository from '../repositories/chord.repository';
import { parseLyrics } from './song.utils';

/**
 * Hook para gestionar la lógica de SongScreen
 * Maneja: canción, lecciones, acordes, tabs y cambios de dificultad
 */
export const useSong = (songId: string) => {
  const [song, setSong] = useState<SongDTO | null>(null);
  const [songLessons, setSongLessons] = useState<SongLessonDTO[]>([]);
  const [selectedLesson, setSelectedLesson] = useState<SongLessonDTO | null>(null);
  const [chords, setChords] = useState<ChordDTO[]>([]);
  const [tabSections, setTabSections] = useState<TabSectionDTO[]>([]);
  const [availableDifficulties, setAvailableDifficulties] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadSongData();
  }, [songId]);

  /**
   * Carga todos los datos de la canción
   */
  const loadSongData = async () => {
    try {
      setLoading(true);

      // 1. Obtener la canción
      let loadedSong = await songStorage.getCachedSong(parseInt(songId));

      if (!loadedSong) {
        loadedSong = await getSongById(songId);
        if (loadedSong) {
          await songStorage.cacheSong(loadedSong);
        }
      }

      setSong(loadedSong);

      // 2. Obtener song lessons, acordes y tab sections con cache fallback
      const songScreenData = await songLessonRepository.getSongScreenData(songId);

      if (!songScreenData.defaultSongLesson) {
        console.error('No hay lecciones disponibles para esta canción');
        setLoading(false);
        return;
      }

      // 3. Obtener las dificultades disponibles
      const difficulties = songScreenData.songLessons.map(l => l.difficulty);

      setSongLessons(songScreenData.songLessons);
      setSelectedLesson(songScreenData.defaultSongLesson);
      setChords(songScreenData.chords[songScreenData.defaultSongLesson.id] || []);
      setTabSections(songScreenData.tabSections[songScreenData.defaultSongLesson.id] || []);
      setAvailableDifficulties(difficulties);

      setLoading(false);
    } catch (error) {
      console.error('Error loading song data:', error);
      setLoading(false);
    }
  };

  /**
   * Cambia la dificultad de la lección
   * @param difficulty Nueva dificultad a mostrar
   */
  const changeDifficulty = async (difficulty: string) => {
    try {
      const selectedLessonData = songLessons.find(l => l.difficulty === difficulty);

      if (!selectedLessonData) {
        console.error(`No hay lección con dificultad ${difficulty}`);
        return;
      }

      // Obtener acordes para esta lección
      const newChords = await chordRepository.getChordsByIds(selectedLessonData.chordIds);

      // Obtener tab sections para esta lección
      const newTabSections = await songLessonRepository.getTabSectionsByLesson(selectedLessonData.id);

      setSelectedLesson(selectedLessonData);
      setChords(newChords);
      setTabSections(newTabSections);
    } catch (error) {
      console.error('Error changing difficulty:', error);
    }
  };

  // Parsear letras de la lección seleccionada
  const parsedLyrics = selectedLesson ? parseLyrics(selectedLesson.chordedLyrics) : [];

  return {
    song,
    loading,
    selectedLesson,
    chords,
    tabSections,
    availableDifficulties,
    changeDifficulty,
    parsedLyrics,
  };
};


