import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react-native';
import SongScreen from '../../screens/SongScreen';
import { DifficultyLevel } from '../../types';

// El primer render de cada suite carga muchos módulos y puede superar los 5 s por defecto
jest.setTimeout(30000);

jest.mock('@react-navigation/native', () => ({
  ...jest.requireActual('@react-navigation/native'),
  useRoute: () => ({ params: { songId: 's1' } }),
}));

let mockSongState: any;
const mockUseSong = jest.fn();
jest.mock('../../hooks/useSong', () => ({
  useSong: (id: string) => {
    mockUseSong(id);
    return mockSongState;
  },
}));

jest.mock('../../components/ProfileHeader', () => {
  const { Text } = require('react-native');
  return ({ title }: any) => <Text>{title}</Text>;
});
jest.mock('../../components/DifficultyStars', () => {
  const { Text } = require('react-native');
  return ({ level }: any) => <Text>{`Nivel ${level}`}</Text>;
});

const lesson = {
  id: 'sl1',
  difficulty: DifficultyLevel.EASY,
  videoUrl: 'https://www.youtube.com/watch?v=dQw4w9WgXcQ',
};

describe('SongScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockSongState = {
      song: { id: 's1', title: 'Wonderwall', artist: 'Oasis', imageUrl: 'https://img/s1.png', level: 2 },
      loading: false,
      selectedLesson: lesson,
      chords: [
        { id: 'c1', name: 'Em', imageUrl: 'https://img/em.png', minor: true, open: true, capoFret: 0 },
        { id: 'c2', name: 'G', imageUrl: 'https://img/g.png' },
      ],
      tabSections: [{ id: 't1', identifier: 'intro', content: 'e|--0--|' }],
      availableInstruments: [
        { id: 'i1', name: 'Guitarra' },
        { id: 'i2', name: 'Piano' },
      ],
      selectedInstrumentId: 'i1',
      changeInstrument: jest.fn(),
      availableDifficulties: [DifficultyLevel.EASY, DifficultyLevel.MEDIUM, DifficultyLevel.ORIGINAL],
      changeDifficulty: jest.fn(),
      parsedLyrics: [
        [{ chord: 'Em', text: 'Today is ' }, { chord: 'G', text: 'gonna be' }],
        [],
        [{ text: 'sin acordes' }],
      ],
    };
  });

  it('muestra la información de la canción', async () => {
    await render(<SongScreen />);
    expect(mockUseSong).toHaveBeenCalledWith('s1');
    expect(screen.getByText('Wonderwall')).toBeTruthy();
    expect(screen.getByText('Oasis')).toBeTruthy();
    expect(screen.getByText('Nivel 2')).toBeTruthy();
  });

  it('muestra el vídeo de YouTube de la lección', async () => {
    await render(<SongScreen />);
    expect(screen.getByText('YouTube dQw4w9WgXcQ')).toBeTruthy();
  });

  it('no muestra vídeo si la lección no tiene', async () => {
    mockSongState.selectedLesson = { ...lesson, videoUrl: undefined };
    await render(<SongScreen />);
    expect(screen.queryByText(/YouTube/)).toBeNull();
  });

  describe('modal de acorde', () => {
    it('está cerrado hasta que se pulsa un acorde', async () => {
      await render(<SongScreen />);
      expect(screen.queryByText('Cerrar')).toBeNull();
    });

    it('al pulsar un acorde muestra su detalle y se cierra con "Cerrar"', async () => {
      await render(<SongScreen />);
      await fireEvent.press(screen.getByLabelText('Ver acorde Em'));
      expect(screen.getByText('Menor')).toBeTruthy();
      expect(screen.getByText('Abierto')).toBeTruthy();
      expect(screen.getByText('Sin capo')).toBeTruthy();

      await fireEvent.press(screen.getByText('Cerrar'));
      expect(screen.queryByText('Menor')).toBeNull();
    });

    it('los acordes de la letra no abren el modal', async () => {
      await render(<SongScreen />);
      await fireEvent.press(screen.getAllByText('Em')[1]);
      expect(screen.queryByText('Cerrar')).toBeNull();
    });
  });

  it('muestra los acordes, la letra y las tabs', async () => {
    await render(<SongScreen />);
    expect(screen.getByText('Acordes')).toBeTruthy();
    expect(screen.getAllByText('Em').length).toBeGreaterThanOrEqual(2);
    expect(screen.getByText('Letra')).toBeTruthy();
    expect(screen.getByText('Today is ')).toBeTruthy();
    expect(screen.getByText('sin acordes')).toBeTruthy();
    expect(screen.getByText('Tabs')).toBeTruthy();
    expect(screen.getByText('INTRO')).toBeTruthy();
    expect(screen.getByText('e|--0--|')).toBeTruthy();
  });

  it('oculta acordes, letra y tabs si no hay', async () => {
    mockSongState = { ...mockSongState, chords: [], parsedLyrics: [], tabSections: [] };
    await render(<SongScreen />);
    expect(screen.queryByText('Acordes')).toBeNull();
    expect(screen.queryByText('Letra')).toBeNull();
    expect(screen.queryByText('Tabs')).toBeNull();
  });

  it('cambia de instrumento y de dificultad', async () => {
    await render(<SongScreen />);
    await fireEvent.press(screen.getByText('Piano'));
    expect(mockSongState.changeInstrument).toHaveBeenCalledWith('i2');
    await fireEvent.press(screen.getByText('Original'));
    expect(mockSongState.changeDifficulty).toHaveBeenCalledWith(DifficultyLevel.ORIGINAL);
    expect(screen.getByText('Fácil')).toBeTruthy();
    expect(screen.getByText('Intermedio')).toBeTruthy();
  });

  it('no muestra el selector de instrumento con un solo instrumento', async () => {
    mockSongState.availableInstruments = [{ id: 'i1', name: 'Guitarra' }];
    await render(<SongScreen />);
    expect(screen.queryByText('Guitarra')).toBeNull();
  });

  it('mientras carga no muestra la canción', async () => {
    mockSongState.loading = true;
    await render(<SongScreen />);
    expect(screen.queryByText('Wonderwall')).toBeNull();
  });

  it('muestra un aviso si la canción no existe', async () => {
    mockSongState.song = undefined;
    await render(<SongScreen />);
    expect(screen.getByText('Canción no encontrada')).toBeTruthy();
  });

  it('muestra un aviso si la canción no tiene lecciones', async () => {
    mockSongState.selectedLesson = undefined;
    await render(<SongScreen />);
    expect(screen.getByText('No hay lecciones disponibles para esta canción')).toBeTruthy();
  });
});
