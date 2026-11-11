import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react-native';
import LearnScreen from '../../screens/LearnScreen';
import { ItemType } from '../../types';

// El primer render de cada suite carga muchos módulos y puede superar los 5 s por defecto
jest.setTimeout(30000);

let mockLessonsState: any;
jest.mock('../../hooks/useHomeLessons', () => ({
  useHomeLessons: () => mockLessonsState,
}));

const mockUseCatalogSearch = jest.fn();
jest.mock('../../hooks/useCatalogSearch', () => ({
  useCatalogSearch: (...args: any[]) => mockUseCatalogSearch(...args),
}));

jest.mock('../../components/ProfileHeader', () => {
  const { Text } = require('react-native');
  return ({ title }: any) => <Text>{title}</Text>;
});

jest.mock('../../components/carousels/GeneralLessonsCarousel', () => {
  const { Text } = require('react-native');
  return ({ title, data }: any) => <Text>{`Carrusel ${title} (${data.length})`}</Text>;
});

jest.mock('../../components/SearchResults', () => {
  const { Text } = require('react-native');
  return ({ sections, emptyText }: any) => (
    <Text>{sections.length ? `Resultados: ${sections.length}` : emptyText}</Text>
  );
});

const lesson = (id: string) => ({ id, title: `Lección ${id}`, imageUrl: '', type: ItemType.LESSON });

describe('LearnScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockUseCatalogSearch.mockReturnValue([]);
    mockLessonsState = {
      populars: [lesson('1')],
      news: [lesson('2'), lesson('3')],
      recommendations: [],
      favouriteInstrument: null,
      loading: false,
      error: null,
    };
  });

  it('muestra solo los carruseles con lecciones', async () => {
    await render(<LearnScreen />);
    expect(screen.getByText('Aprender')).toBeTruthy();
    expect(screen.getByText('Carrusel Populares (1)')).toBeTruthy();
    expect(screen.getByText('Carrusel Novedades (2)')).toBeTruthy();
    expect(screen.queryByText(/Carrusel Para ti/)).toBeNull();
  });

  it('añade el carrusel del instrumento favorito si se conoce su nombre', async () => {
    mockLessonsState.favouriteInstrument = { title: 'Piano', data: [lesson('4')] };
    await render(<LearnScreen />);
    expect(screen.getByText('Carrusel Para Piano (1)')).toBeTruthy();
  });

  it('no añade el carrusel del instrumento favorito sin título', async () => {
    mockLessonsState.favouriteInstrument = { title: '', data: [lesson('4')] };
    await render(<LearnScreen />);
    expect(screen.queryByText(/Carrusel Para /)).toBeNull();
  });

  it('muestra el error de carga', async () => {
    mockLessonsState.error = 'Error cargando lecciones';
    await render(<LearnScreen />);
    expect(screen.getByText('Error cargando lecciones')).toBeTruthy();
    expect(screen.queryByText(/Carrusel/)).toBeNull();
  });

  it('sin lecciones en ninguna categoría muestra el estado vacío', async () => {
    mockLessonsState = { ...mockLessonsState, populars: [], news: [], recommendations: [] };
    await render(<LearnScreen />);
    expect(screen.getByText('Todavía no hay lecciones disponibles')).toBeTruthy();
    expect(screen.queryByText(/Carrusel/)).toBeNull();
  });

  it('con alguna lección no muestra el estado vacío', async () => {
    await render(<LearnScreen />);
    expect(screen.queryByText('Todavía no hay lecciones disponibles')).toBeNull();
  });

  it('mientras carga no muestra los carruseles', async () => {
    mockLessonsState.loading = true;
    await render(<LearnScreen />);
    expect(screen.queryByText(/Carrusel/)).toBeNull();
  });

  it('busca en el catálogo de lecciones al escribir y muestra los resultados', async () => {
    mockUseCatalogSearch.mockImplementation((query: string) => (query ? [{ title: 'x', data: [] }] : []));
    await render(<LearnScreen />);
    await fireEvent.changeText(screen.getByPlaceholderText('Busca por título, género o instrumento'), 'rock');
    expect(mockUseCatalogSearch).toHaveBeenLastCalledWith('rock', ItemType.LESSON, ['title', 'genre', 'instrument']);
    expect(screen.getByText('Resultados: 1')).toBeTruthy();
    expect(screen.queryByText(/Carrusel/)).toBeNull();
  });

  it('muestra el texto vacío si la búsqueda no encuentra nada', async () => {
    await render(<LearnScreen />);
    await fireEvent.changeText(screen.getByPlaceholderText('Busca por título, género o instrumento'), 'zzz');
    expect(screen.getByText('No hay lecciones que coincidan con tu búsqueda')).toBeTruthy();
  });

  it('una búsqueda de solo espacios muestra los carruseles', async () => {
    await render(<LearnScreen />);
    await fireEvent.changeText(screen.getByPlaceholderText('Busca por título, género o instrumento'), '   ');
    expect(screen.getByText('Carrusel Populares (1)')).toBeTruthy();
  });
});
