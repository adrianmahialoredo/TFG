import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react-native';
import HomeScreen from '../../screens/HomeScreen';
import { ItemType } from '../../types';

// El primer render de cada suite carga muchos módulos y puede superar los 5 s por defecto
jest.setTimeout(30000);

const mockNavigation = { navigate: jest.fn(), goBack: jest.fn() };
jest.mock('@react-navigation/native', () => ({
  ...jest.requireActual('@react-navigation/native'),
  useNavigation: () => mockNavigation,
}));

jest.mock('../../context/AuthContext', () => ({
  useAuth: () => ({ user: { id: 'u1', name: 'Ana', username: 'ana' } }),
}));

let mockHomeState: any;
jest.mock('../../hooks/useHomeItems', () => ({
  useHomeItems: () => mockHomeState,
}));

jest.mock('../../components/carousels/HorizontalItemsCarousel', () => {
  const { View, Text } = require('react-native');
  return ({ title, data, onItemPress, emptyMessage }: any) => (
    <View>
      <Text>{`Carrusel ${title}`}</Text>
      {data.length === 0 && emptyMessage ? <Text>{emptyMessage}</Text> : null}
      {data.map((d: any) => (
        <Text key={d.id} onPress={() => onItemPress(d.id)}>{d.title}</Text>
      ))}
    </View>
  );
});

jest.mock('../../components/SuggestionCard', () => {
  const { Text } = require('react-native');
  return ({ label, title, onPress }: any) => <Text onPress={onPress}>{`${label}: ${title}`}</Text>;
});

jest.mock('../../components/buttons/SpotifyButton', () => {
  const { Text } = require('react-native');
  return { SpotifyButton: () => <Text>Botón Spotify</Text> };
});
jest.mock('../../components/buttons/ToolButton', () => {
  const { Text } = require('react-native');
  return { ToolButton: ({ label, onPress }: any) => <Text onPress={onPress}>{label}</Text> };
});

jest.mock('../../components/UserAvatar', () => {
  const { Text } = require('react-native');
  return () => <Text>Avatar</Text>;
});

const song = { id: 's1', title: 'Wonderwall', imageUrl: '', type: ItemType.SONG };
const lesson = { id: 'l1', title: 'Acordes básicos', imageUrl: '', type: ItemType.LESSON };

const baseState = () => ({
  recentItems: [],
  recommendations: [],
  spotifyItems: [] as any[],
  spotifyConnected: undefined as boolean | undefined,
  favouriteCombination: null,
  mostUsedGenre: null,
  newFavouriteArtistSong: null,
  newFavouriteArtistSongs: [],
  popularFavouriteArtistSong: null,
  popularFavouriteArtistSongs: [],
  getItemById: jest.fn(),
  loading: false,
  error: null,
});

describe('HomeScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockHomeState = baseState();
  });

  it('saluda al usuario y muestra los carruseles vacíos con su mensaje', async () => {
    await render(<HomeScreen />);
    expect(screen.getByText('¡Bienvenido Ana!')).toBeTruthy();
    expect(screen.getByText('Carrusel Recientes')).toBeTruthy();
    expect(screen.getByText('Carrusel Para ti')).toBeTruthy();
    expect(screen.getByText(/se muestren tus recientes/)).toBeTruthy();
    expect(screen.getByText(/se muestren tus recomendaciones/)).toBeTruthy();
  });

  it('mientras no se sabe si hay Spotify vinculado no muestra ni carrusel ni botón', async () => {
    await render(<HomeScreen />);
    expect(screen.queryByText('Carrusel Basado en tu Spotify')).toBeNull();
    expect(screen.queryByText('Botón Spotify')).toBeNull();
  });

  it('sin Spotify vinculado ofrece conectarlo en vez del carrusel', async () => {
    mockHomeState.spotifyConnected = false;
    await render(<HomeScreen />);
    expect(screen.queryByText('Carrusel Basado en tu Spotify')).toBeNull();
    expect(screen.getByText('Basado en tu Spotify')).toBeTruthy();
    expect(screen.getByText('Botón Spotify')).toBeTruthy();
  });

  it('con Spotify vinculado muestra su carrusel (con mensaje si está vacío)', async () => {
    mockHomeState.spotifyConnected = true;
    await render(<HomeScreen />);
    expect(screen.getByText('Carrusel Basado en tu Spotify')).toBeTruthy();
    expect(screen.getByText(/Escucha música en Spotify/)).toBeTruthy();
  });

  it('mientras carga no muestra los mensajes de vacío', async () => {
    mockHomeState.loading = true;
    mockHomeState.error = 'fallo';
    await render(<HomeScreen />);
    expect(screen.queryByText(/se muestren tus recientes/)).toBeNull();
    // El error no se pinta mientras se está cargando
    expect(screen.queryByText('fallo')).toBeNull();
  });

  it('muestra el error cuando no está cargando', async () => {
    mockHomeState.error = 'No se pudieron cargar los items';
    await render(<HomeScreen />);
    expect(screen.getByText('No se pudieron cargar los items')).toBeTruthy();
  });

  it('navega al detalle del item pulsado según su tipo', async () => {
    mockHomeState.recentItems = [song];
    mockHomeState.recommendations = [lesson];
    mockHomeState.getItemById = (id: string) => [song, lesson].find((i) => i.id === id);
    await render(<HomeScreen />);
    await fireEvent.press(screen.getByText('Wonderwall'));
    expect(mockNavigation.navigate).toHaveBeenCalledWith('SongScreen', { songId: 's1' });
    await fireEvent.press(screen.getByText('Acordes básicos'));
    expect(mockNavigation.navigate).toHaveBeenCalledWith('LessonScreen', { lessonId: 'l1' });
  });

  it('no navega si el item no se encuentra', async () => {
    mockHomeState.recentItems = [song];
    mockHomeState.getItemById = () => undefined;
    await render(<HomeScreen />);
    await fireEvent.press(screen.getByText('Wonderwall'));
    expect(mockNavigation.navigate).not.toHaveBeenCalled();
  });

  it('muestra las sugerencias y navega al pulsarlas', async () => {
    mockHomeState.favouriteCombination = { id: 'ig1', title: 'Guitarra Rock', imageUrl: '', type: ItemType.INSTRUMENT_GENRE };
    mockHomeState.mostUsedGenre = { id: 'g1', title: 'Jazz', imageUrl: 'x', type: ItemType.GENRE };
    await render(<HomeScreen />);
    await fireEvent.press(screen.getByText('Prueba esta combinación: Guitarra Rock'));
    expect(mockNavigation.navigate).toHaveBeenCalledWith('InstrumentGenreLesson', {
      instrumentGenreId: 'ig1',
      instrumentName: 'Guitarra Rock',
    });
    await fireEvent.press(screen.getByText('Explora este género: Jazz'));
    expect(mockNavigation.navigate).toHaveBeenCalledWith('GenreMainScreen', { genreId: 'g1' });
  });

  it('navega al perfil al pulsar el avatar', async () => {
    await render(<HomeScreen />);
    await fireEvent.press(screen.getByText('Avatar'));
    expect(mockNavigation.navigate).toHaveBeenCalledWith('ProfileScreen');
  });

  it('no muestra sugerencias si no hay', async () => {
    await render(<HomeScreen />);
    expect(screen.queryByText(/Prueba esta combinación/)).toBeNull();
    expect(screen.queryByText(/Explora este género/)).toBeNull();
  });

  it('navega al metrónomo desde su botón de herramienta', async () => {
    await render(<HomeScreen />);
    await fireEvent.press(screen.getByText('Metrónomo'));
    expect(mockNavigation.navigate).toHaveBeenCalledWith('MetronomeScreen');
    expect(screen.getByText('Afinador')).toBeTruthy();
    // El Identificador aún no hace nada: está oculto
    expect(screen.queryByText('Identificador')).toBeNull();
  });

  it('navega al afinador desde su botón de herramienta', async () => {
    await render(<HomeScreen />);
    await fireEvent.press(screen.getByText('Afinador'));
    expect(mockNavigation.navigate).toHaveBeenCalledWith('TunerScreen');
  });

  it('sin canciones de artistas favoritos muestra sus carruseles vacíos con su mensaje y sin tarjetas', async () => {
    await render(<HomeScreen />);
    expect(screen.getByText('Carrusel Novedades de tus artistas')).toBeTruthy();
    expect(screen.getByText('Carrusel Populares de tus artistas')).toBeTruthy();
    expect(screen.getByText(/descubrir las novedades de tus artistas favoritos/)).toBeTruthy();
    expect(screen.getByText(/ver los éxitos de tus artistas favoritos/)).toBeTruthy();
    expect(screen.queryByText(/Lo nuevo de/)).toBeNull();
    expect(screen.queryByText(/Lo más popular de/)).toBeNull();
  });

  it('con solo una canción popular de sus artistas muestra su tarjeta sin carruseles vacíos ni avisos', async () => {
    mockHomeState = {
      ...baseState(),
      popularFavouriteArtistSong: { id: 'p1', title: 'Basket Case', imageUrl: '', type: ItemType.SONG, artist: 'Green Day' },
    };
    await render(<HomeScreen />);
    expect(screen.getByText('Lo más popular de Green Day: Basket Case')).toBeTruthy();
    expect(screen.queryByText('Carrusel Novedades de tus artistas')).toBeNull();
    expect(screen.queryByText('Carrusel Populares de tus artistas')).toBeNull();
    expect(screen.queryByText(/Añade canciones a tus listas/)).toBeNull();
  });

  it('muestra las tarjetas y carruseles de los artistas favoritos y navega desde ellos', async () => {
    const newSong = { id: 'n1', title: 'Under Pressure', imageUrl: '', type: ItemType.SONG, artist: 'Queen' };
    const popularSong = { id: 'p1', title: 'Heroes', imageUrl: '', type: ItemType.SONG, artist: 'Bowie' };
    mockHomeState = {
      ...baseState(),
      newFavouriteArtistSong: newSong,
      newFavouriteArtistSongs: [{ id: 'n2', title: 'Radio Ga Ga' }],
      popularFavouriteArtistSong: popularSong,
      popularFavouriteArtistSongs: [{ id: 'p2', title: 'Starman' }],
      getItemById: jest.fn((id: string) => (id === 'n2' ? { ...newSong, id: 'n2' } : undefined)),
    };
    await render(<HomeScreen />);

    await fireEvent.press(screen.getByText('Lo nuevo de Queen: Under Pressure'));
    expect(mockNavigation.navigate).toHaveBeenLastCalledWith('SongScreen', { songId: 'n1' });

    await fireEvent.press(screen.getByText('Lo más popular de Bowie: Heroes'));
    expect(mockNavigation.navigate).toHaveBeenLastCalledWith('SongScreen', { songId: 'p1' });

    expect(screen.getByText('Starman')).toBeTruthy();
    await fireEvent.press(screen.getByText('Radio Ga Ga'));
    expect(mockNavigation.navigate).toHaveBeenLastCalledWith('SongScreen', { songId: 'n2' });
  });
});
