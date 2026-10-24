import React from 'react';
import { render, screen } from '@testing-library/react-native';
import GenreSongsScreen from '../../screens/GenreSongsScreen';

// El primer render de cada suite carga muchos módulos y puede superar los 5 s por defecto
jest.setTimeout(30000);

let mockState: any;
const mockUseGenreSongs = jest.fn();
jest.mock('../../hooks/useGenreSongs', () => ({
  useGenreSongs: (id: string) => {
    mockUseGenreSongs(id);
    return mockState;
  },
}));

jest.mock('../../components/carousels/SongsCarousel', () => {
  const { Text } = require('react-native');
  return ({ title, data }: any) => <Text>{`Carrusel ${title} (${data.length})`}</Text>;
});

const song = (id: string) => ({ id, title: id, imageUrl: '', type: 'Song' });

describe('GenreSongsScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockState = { populars: [], classics: [], news: [], loading: false, error: null };
  });

  it('muestra solo las categorías con canciones', async () => {
    mockState.populars = [song('a'), song('b')];
    mockState.news = [song('c')];
    await render(<GenreSongsScreen genreId="g1" />);
    expect(mockUseGenreSongs).toHaveBeenCalledWith('g1');
    expect(screen.getByText('Carrusel Populares (2)')).toBeTruthy();
    expect(screen.getByText('Carrusel Novedades (1)')).toBeTruthy();
    expect(screen.queryByText(/Clásicos/)).toBeNull();
    expect(screen.queryByText('Próximamente más canciones...')).toBeNull();
  });

  it('muestra un aviso si no hay canciones', async () => {
    await render(<GenreSongsScreen genreId="g1" />);
    expect(screen.getByText('Próximamente más canciones...')).toBeTruthy();
  });

  it('no muestra canciones mientras carga', async () => {
    mockState.loading = true;
    mockState.populars = [song('a')];
    await render(<GenreSongsScreen genreId="g1" />);
    expect(screen.queryByText(/Carrusel/)).toBeNull();
    expect(screen.queryByText('Próximamente más canciones...')).toBeNull();
  });

  it('muestra el error', async () => {
    mockState.error = 'No se pudieron cargar';
    await render(<GenreSongsScreen genreId="g1" />);
    expect(screen.getByText('No se pudieron cargar')).toBeTruthy();
  });
});
