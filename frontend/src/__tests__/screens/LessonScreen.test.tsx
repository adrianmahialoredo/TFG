import React from 'react';
import { render, screen } from '@testing-library/react-native';
import LessonScreen from '../../screens/LessonScreen';

// El primer render de cada suite carga muchos módulos y puede superar los 5 s por defecto
jest.setTimeout(30000);

jest.mock('@react-navigation/native', () => ({
  ...jest.requireActual('@react-navigation/native'),
  useRoute: () => ({ params: { lessonId: 'l1' } }),
}));

let mockLessonState: any;
const mockUseLesson = jest.fn();
jest.mock('../../hooks/useLesson', () => ({
  useLesson: (id: string) => {
    mockUseLesson(id);
    return mockLessonState;
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
jest.mock('../../components/LessonSectionViewer', () => {
  const { Text } = require('react-native');
  return ({ sections, sectionIndex, loading }: any) => (
    <Text>{`Visor ${sectionIndex + 1}/${sections.length} cargando=${loading}`}</Text>
  );
});

describe('LessonScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockLessonState = {
      lesson: { id: 'l1', title: 'Escalas mayores', imageUrl: 'https://img/l1.png', level: 3 },
      genre: { id: 'g1', title: 'Jazz' },
      loading: false,
      sections: [{ id: 's1' }, { id: 's2' }],
      currentSection: { id: 's1' },
      sectionIndex: 0,
      hasPrevious: false,
      hasNext: true,
      goToNext: jest.fn(),
      goToPrevious: jest.fn(),
    };
  });

  it('muestra la lección, su género, su dificultad y el visor de secciones', async () => {
    await render(<LessonScreen />);
    expect(mockUseLesson).toHaveBeenCalledWith('l1');
    expect(screen.getByText('Escalas mayores')).toBeTruthy();
    expect(screen.getByText('Jazz')).toBeTruthy();
    expect(screen.getByText('Nivel 3')).toBeTruthy();
    expect(screen.getByText('Visor 1/2 cargando=false')).toBeTruthy();
  });

  it('no muestra el género si no se conoce', async () => {
    mockLessonState.genre = undefined;
    await render(<LessonScreen />);
    expect(screen.queryByText('Jazz')).toBeNull();
    expect(screen.getByText('Escalas mayores')).toBeTruthy();
  });

  it('mientras carga sin lección no muestra contenido', async () => {
    mockLessonState = { ...mockLessonState, lesson: undefined, loading: true };
    await render(<LessonScreen />);
    expect(screen.queryByText('Lección no encontrada')).toBeNull();
    expect(screen.queryByText(/Visor/)).toBeNull();
  });

  it('si la lección ya está en memoria la muestra aunque se esté sincronizando', async () => {
    mockLessonState.loading = true;
    await render(<LessonScreen />);
    expect(screen.getByText('Visor 1/2 cargando=true')).toBeTruthy();
  });

  it('muestra un aviso si la lección no existe', async () => {
    mockLessonState = { ...mockLessonState, lesson: undefined };
    await render(<LessonScreen />);
    expect(screen.getByText('Lección no encontrada')).toBeTruthy();
  });
});
