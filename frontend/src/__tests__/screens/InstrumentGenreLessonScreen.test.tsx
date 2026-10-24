import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react-native';
import InstrumentGenreLessonScreen from '../../screens/InstrumentGenreLessonScreen';

// El primer render de cada suite carga muchos módulos y puede superar los 5 s por defecto
jest.setTimeout(30000);

const mockNavigation = { navigate: jest.fn(), goBack: jest.fn(), canGoBack: jest.fn() };
let mockRoute: any = { params: undefined };
jest.mock('@react-navigation/native', () => ({
  ...jest.requireActual('@react-navigation/native'),
  useNavigation: () => mockNavigation,
  useRoute: () => mockRoute,
}));

let mockIgState: any;
const mockUseInstrumentGenreLesson = jest.fn();
jest.mock('../../hooks/useInstrumentGenreLesson', () => ({
  useInstrumentGenreLesson: (id: string) => {
    mockUseInstrumentGenreLesson(id);
    return mockIgState;
  },
}));

const mockUseLesson = jest.fn();
jest.mock('../../hooks/useLesson', () => ({
  useLesson: (...args: any[]) => {
    mockUseLesson(...args);
    return { lesson: undefined, sections: [], sectionIndex: 0, loading: false };
  },
}));

jest.mock('../../components/DifficultyStars', () => {
  const { Text } = require('react-native');
  return ({ level }: any) => <Text>{`Nivel ${level}`}</Text>;
});
jest.mock('../../components/LessonSectionViewer', () => {
  const { Text } = require('react-native');
  return () => <Text>Visor de secciones</Text>;
});
jest.mock('../../components/EdgePanel', () => {
  const { View, Text } = require('react-native');
  return ({ lessons, activeLessonId, onSelectLessonId }: any) => (
    <View>
      <Text>{`Activa ${activeLessonId}`}</Text>
      {lessons.map((l: any) => (
        <Text key={l.id} onPress={() => onSelectLessonId(l.id)}>{`Panel ${l.title}`}</Text>
      ))}
    </View>
  );
});

const lessons = [
  { id: 'l1', title: 'Intro', level: 1, imageUrl: 'https://img/l1.png' },
  { id: 'l2', title: 'Riffs', level: 2 },
];

describe('InstrumentGenreLessonScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockRoute = { params: undefined };
    mockIgState = {
      instrumentGenre: { id: 'ig1', title: 'Guitarra Rock' },
      lessons,
      activeLesson: lessons[0],
      setActiveLessonId: jest.fn(),
      loading: false,
    };
  });

  it('usa las props cuando se embebe y muestra la lección activa', async () => {
    await render(<InstrumentGenreLessonScreen instrumentGenreId="ig1" instrumentName="Guitarra" />);
    expect(mockUseInstrumentGenreLesson).toHaveBeenCalledWith('ig1');
    expect(mockUseLesson).toHaveBeenCalledWith('l1', { markAccessed: false });
    expect(screen.getByText('Guitarra')).toBeTruthy();
    expect(screen.getByText('Intro')).toBeTruthy();
    expect(screen.getByText('Guitarra Rock')).toBeTruthy();
    expect(screen.getByText('Nivel 1')).toBeTruthy();
    expect(screen.getByText('Visor de secciones')).toBeTruthy();
    expect(screen.getByText('Activa l1')).toBeTruthy();
  });

  it('usa los parámetros de la ruta si no recibe props', async () => {
    mockRoute = { params: { instrumentGenreId: 'ig9', instrumentName: 'Bajo' } };
    await render(<InstrumentGenreLessonScreen />);
    expect(mockUseInstrumentGenreLesson).toHaveBeenCalledWith('ig9');
    expect(screen.getByText('Bajo')).toBeTruthy();
  });

  it('usa un id vacío si no hay props ni parámetros', async () => {
    await render(<InstrumentGenreLessonScreen />);
    expect(mockUseInstrumentGenreLesson).toHaveBeenCalledWith('');
  });

  it('cambia de lección desde el panel lateral', async () => {
    await render(<InstrumentGenreLessonScreen instrumentGenreId="ig1" />);
    await fireEvent.press(screen.getByText('Panel Riffs'));
    expect(mockIgState.setActiveLessonId).toHaveBeenCalledWith('l2');
  });

  it('el botón de volver llama a onBack si se recibe', async () => {
    const onBack = jest.fn();
    await render(<InstrumentGenreLessonScreen instrumentGenreId="ig1" onBack={onBack} />);
    await fireEvent.press(screen.getByLabelText('Volver'));
    expect(onBack).toHaveBeenCalled();
    expect(mockNavigation.goBack).not.toHaveBeenCalled();
  });

  it('sin onBack vuelve atrás en la navegación si se puede', async () => {
    mockNavigation.canGoBack.mockReturnValue(true);
    await render(<InstrumentGenreLessonScreen instrumentGenreId="ig1" />);
    await fireEvent.press(screen.getByLabelText('Volver'));
    expect(mockNavigation.goBack).toHaveBeenCalled();
  });

  it('sin onBack no hace nada si no se puede volver', async () => {
    mockNavigation.canGoBack.mockReturnValue(false);
    await render(<InstrumentGenreLessonScreen instrumentGenreId="ig1" />);
    await fireEvent.press(screen.getByLabelText('Volver'));
    expect(mockNavigation.goBack).not.toHaveBeenCalled();
  });

  it('sin nombre en la ruta usa el título del género-instrumento', async () => {
    mockRoute = { params: { instrumentGenreId: 'ig1' } };
    await render(<InstrumentGenreLessonScreen />);
    // Cabecera y subtítulo de la lección
    expect(screen.getAllByText('Guitarra Rock')).toHaveLength(2);
  });

  describe('estados sin lección que mostrar', () => {
    it('mientras carga muestra un indicador y el botón de volver', async () => {
      mockRoute = { params: { instrumentGenreId: 'ig1', instrumentName: 'Bajo' } };
      mockIgState = { ...mockIgState, instrumentGenre: undefined, activeLesson: undefined, lessons: [], loading: true };
      await render(<InstrumentGenreLessonScreen />);
      expect(mockUseLesson).toHaveBeenCalledWith('', { markAccessed: false });
      expect(screen.container.queryAll((n) => n.type === 'ActivityIndicator')).toHaveLength(1);
      expect(screen.getByText('Bajo')).toBeTruthy();
      expect(screen.getByLabelText('Volver')).toBeTruthy();
    });

    it('si no se pudo cargar lo indica', async () => {
      mockIgState = { ...mockIgState, instrumentGenre: undefined, activeLesson: undefined, lessons: [] };
      await render(<InstrumentGenreLessonScreen instrumentGenreId="ig1" />);
      expect(screen.getByText('No se pudo cargar el contenido')).toBeTruthy();
    });

    it('si el itinerario no tiene lecciones lo indica', async () => {
      mockIgState = { ...mockIgState, activeLesson: undefined, lessons: [] };
      await render(<InstrumentGenreLessonScreen instrumentGenreId="ig1" />);
      expect(screen.getByText('Este itinerario aún no tiene lecciones')).toBeTruthy();
    });
  });
});
