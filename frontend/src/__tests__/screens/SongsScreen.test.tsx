import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react-native';
import SongsScreen from '../../screens/SongsScreen';
import { ItemType } from '../../types';

// El primer render de cada suite carga muchos módulos y puede superar los 5 s por defecto
jest.setTimeout(30000);

let mockFeedState: any;
jest.mock('../../hooks/useFeedSongs', () => ({
  useFeedSongs: () => mockFeedState,
}));

const mockUseCatalogSearch = jest.fn();
jest.mock('../../hooks/useCatalogSearch', () => ({
  useCatalogSearch: (...args: any[]) => mockUseCatalogSearch(...args),
}));

jest.mock('../../components/ProfileHeader', () => {
  const { Text } = require('react-native');
  return ({ title }: any) => <Text>{title}</Text>;
});

jest.mock('../../components/carousels/SongsCarousel', () => {
  const { Text } = require('react-native');
  return ({ title, data }: any) => <Text>{`Carrusel ${title} (${data.length})`}</Text>;
});

jest.mock('../../components/buttons/SpotifyButton', () => {
  const { Text } = require('react-native');
  return { SpotifyButton: () => <Text>Conectar Spotify</Text> };
});

jest.mock('../../components/SearchResults', () => {
  const { Text } = require('react-native');
  return ({ sections, emptyText }: any) => (
    <Text>{sections.length ? `Resultados: ${sections.length}` : emptyText}</Text>
  );
});

const song = (id: string) => ({ id, title: `Canción ${id}`, imageUrl: '', type: ItemType.SONG });
const PLACEHOLDER = 'Busca por título, artista, género, instrumento o década';

describe('SongsScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockUseCatalogSearch.mockReturnValue([]);
    mockFeedState = {
      populars: [song('1')],
      news: [song('2')],
      recommendations: [],
      spotify: [],
      spotifyConnected: true,
      favouriteGenre: null,
      loading: false,
      error: null,
    };
  });

  it('muestra los carruseles con canciones', async () => {
    await render(<SongsScreen />);
    expect(screen.getByText('Canciones')).toBeTruthy();
    expect(screen.getByText('Carrusel Populares (1)')).toBeTruthy();
    expect(screen.getByText('Carrusel Nuevas (1)')).toBeTruthy();
    expect(screen.queryByText(/Según tus gustos/)).toBeNull();
    expect(screen.queryByText('Conectar Spotify')).toBeNull();
  });

  it('sin canciones en ninguna categoría muestra el estado vacío', async () => {
    mockFeedState = { ...mockFeedState, populars: [], news: [] };
    await render(<SongsScreen />);
    expect(screen.getByText('Todavía no hay canciones disponibles')).toBeTruthy();
    expect(screen.queryByText(/Carrusel/)).toBeNull();
  });

  it('con el estado vacío sigue ofreciendo conectar Spotify', async () => {
    mockFeedState = { ...mockFeedState, populars: [], news: [], spotifyConnected: false };
    await render(<SongsScreen />);
    expect(screen.getByText('Todavía no hay canciones disponibles')).toBeTruthy();
    expect(screen.getByText('Conectar Spotify')).toBeTruthy();
  });

  it('con alguna canción no muestra el estado vacío', async () => {
    await render(<SongsScreen />);
    expect(screen.queryByText('Todavía no hay canciones disponibles')).toBeNull();
  });

  it('ofrece conectar Spotify si la cuenta no está vinculada', async () => {
    mockFeedState.spotifyConnected = false;
    await render(<SongsScreen />);
    expect(screen.getByText('Según tu Spotify')).toBeTruthy();
    expect(screen.getByText('Conectar Spotify')).toBeTruthy();
  });

  it('muestra el carrusel de Spotify si tiene canciones', async () => {
    mockFeedState.spotify = [song('9')];
    await render(<SongsScreen />);
    expect(screen.getByText('Carrusel Según tu Spotify (1)')).toBeTruthy();
  });

  it('muestra el carrusel del género favorito con su nombre o uno genérico', async () => {
    mockFeedState.favouriteGenre = { title: 'Rock', data: [song('5')] };
    const { rerender } = await render(<SongsScreen />);
    expect(screen.getByText('Carrusel Populares en Rock (1)')).toBeTruthy();

    mockFeedState = { ...mockFeedState, favouriteGenre: { title: '', data: [song('5')] } };
    await rerender(<SongsScreen />);
    expect(screen.getByText('Carrusel Populares en tu género favorito (1)')).toBeTruthy();
  });

  it('muestra el error de carga', async () => {
    mockFeedState.error = 'Error cargando canciones';
    await render(<SongsScreen />);
    expect(screen.getByText('Error cargando canciones')).toBeTruthy();
  });

  it('mientras carga no muestra los carruseles', async () => {
    mockFeedState.loading = true;
    await render(<SongsScreen />);
    expect(screen.queryByText(/Carrusel/)).toBeNull();
  });

  it('busca canciones en el catálogo al escribir', async () => {
    mockUseCatalogSearch.mockImplementation((q: string) => (q ? [{ title: 'a', data: [] }, { title: 'b', data: [] }] : []));
    await render(<SongsScreen />);
    await fireEvent.changeText(screen.getByPlaceholderText(PLACEHOLDER), 'queen');
    expect(mockUseCatalogSearch).toHaveBeenLastCalledWith('queen', ItemType.SONG, ['title', 'artist', 'genre', 'instrument', 'decade']);
    expect(screen.getByText('Resultados: 2')).toBeTruthy();
  });

  it('muestra el texto vacío si la búsqueda no encuentra canciones', async () => {
    await render(<SongsScreen />);
    await fireEvent.changeText(screen.getByPlaceholderText(PLACEHOLDER), 'zzz');
    expect(screen.getByText('No hay canciones que coincidan con tu búsqueda')).toBeTruthy();
  });
});
