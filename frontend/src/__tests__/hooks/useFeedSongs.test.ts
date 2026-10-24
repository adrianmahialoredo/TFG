import { act, renderHook, waitFor } from '@testing-library/react-native';
import { useFeedSongs } from '../../hooks/useFeedSongs';
import {
  useAuthStore,
  useGenreEntitiesStore,
  useItemEntitiesStore,
  useUserEntitiesStore,
} from '../../stores/entities';
import * as songRepository from '../../repositories/song.repository';
import { ItemType } from '../../types';

jest.mock('../../repositories/song.repository', () => ({
  getPopular: jest.fn(),
  getNew: jest.fn(),
  getRecommended: jest.fn(),
  getFromSpotify: jest.fn(),
  getPopularByGenreId: jest.fn(),
}));

const mocked = songRepository as jest.Mocked<typeof songRepository>;

const song = (id: string) => ({ id, title: `Canción ${id}`, artist: `Art ${id}`, imageUrl: `img${id}`, type: ItemType.SONG });

describe('useFeedSongs', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    useItemEntitiesStore.setState({
      items: Object.fromEntries(['1', '2', '3', '4', '5', '6'].map((id) => [id, song(id) as any])),
    });
    useAuthStore.setState({ token: null, currentUserId: null, isAuthenticated: false });
    useUserEntitiesStore.setState({ users: {} });
    useGenreEntitiesStore.setState({ genres: { g1: { id: 'g1', title: 'Rock' } as any } });
    mocked.getPopular.mockResolvedValue(['1', 'falta']);
    mocked.getNew.mockResolvedValue(['2']);
    mocked.getRecommended.mockResolvedValue(['3']);
    mocked.getFromSpotify.mockResolvedValue(['4']);
    mocked.getPopularByGenreId.mockResolvedValue(['5']);
  });

  it('sin sesión carga populares y novedades pero no recomendaciones', async () => {
    const { result } = await renderHook(() => useFeedSongs());

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.populars).toEqual([
      { id: '1', title: 'Canción 1', imgUrl: 'img1', artist: 'Art 1', description: 'Art 1' },
    ]);
    expect(result.current.news.map((s) => s.id)).toEqual(['2']);
    expect(result.current.recommendations).toEqual([]);
    expect(result.current.spotify).toEqual([]);
    expect(result.current.favouriteGenre).toBeNull();
    expect(result.current.spotifyConnected).toBeUndefined();
    expect(result.current.error).toBeNull();
    expect(mocked.getRecommended).not.toHaveBeenCalled();
    expect(mocked.getFromSpotify).not.toHaveBeenCalled();
  });

  it('con sesión pide recomendaciones, Spotify y el género favorito', async () => {
    useAuthStore.setState({ currentUserId: 'u1', token: 'tk' });
    useUserEntitiesStore.setState({
      users: { u1: { id: 'u1', spotifyConnected: true, favouriteGenresIds: ['g1'] } as any },
    });

    const { result } = await renderHook(() => useFeedSongs());

    await waitFor(() => expect(result.current.spotify.map((s) => s.id)).toEqual(['4']));
    expect(result.current.recommendations.map((s) => s.id)).toEqual(['3']);
    expect(result.current.spotifyConnected).toBe(true);
    await waitFor(() => expect(result.current.favouriteGenre?.data.map((s) => s.id)).toEqual(['5']));
    expect(result.current.favouriteGenre?.title).toBe('Rock');
    expect(mocked.getRecommended).toHaveBeenCalledWith('u1', expect.any(Function));
    expect(mocked.getFromSpotify).toHaveBeenCalledWith('u1', expect.any(Function));
    expect(mocked.getPopularByGenreId).toHaveBeenCalledWith('g1', expect.any(Function));
  });

  it('el género favorito sin título en la Store usa cadena vacía', async () => {
    useAuthStore.setState({ currentUserId: 'u1' });
    useUserEntitiesStore.setState({ users: { u1: { id: 'u1', favouriteGenresIds: ['gx'] } as any } });

    const { result } = await renderHook(() => useFeedSongs());
    await waitFor(() => expect(result.current.favouriteGenre?.data.length).toBe(1));
    expect(result.current.favouriteGenre?.title).toBe('');
  });

  it('sin Spotify vinculado no pide sus recomendaciones', async () => {
    useAuthStore.setState({ currentUserId: 'u1' });
    useUserEntitiesStore.setState({ users: { u1: { id: 'u1', spotifyConnected: false } as any } });

    const { result } = await renderHook(() => useFeedSongs());
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(mocked.getFromSpotify).not.toHaveBeenCalled();
    expect(result.current.spotifyConnected).toBe(false);
  });

  it('aplica primero la caché y después los IDs frescos de cada sección', async () => {
    mocked.getPopular.mockImplementation(async (onFresh: any) => {
      setTimeout(() => onFresh(['6']), 0);
      return ['1'];
    });
    const { result } = await renderHook(() => useFeedSongs());

    await waitFor(() => expect(result.current.populars.map((s) => s.id)).toEqual(['6']));
  });

  it('muestra un error si falla alguna sección', async () => {
    mocked.getNew.mockRejectedValue(new Error('red'));
    const { result } = await renderHook(() => useFeedSongs());

    await waitFor(() => expect(result.current.error).toBe('Error al sincronizar canciones'));
    expect(result.current.loading).toBe(false);
  });

  it('registra los errores de Spotify y del género favorito sin romper', async () => {
    useAuthStore.setState({ currentUserId: 'u1' });
    useUserEntitiesStore.setState({
      users: { u1: { id: 'u1', spotifyConnected: true, favouriteGenresIds: ['g1'] } as any },
    });
    mocked.getFromSpotify.mockRejectedValue(new Error('spotify'));
    mocked.getPopularByGenreId.mockRejectedValue(new Error('genero'));

    const { result } = await renderHook(() => useFeedSongs());
    await waitFor(() =>
      expect(console.error).toHaveBeenCalledWith('[useFeedSongs] Error en Spotify:', expect.any(Error))
    );
    await waitFor(() =>
      expect(console.error).toHaveBeenCalledWith('[useCategorizedSongs] Error en el género favorito:', expect.any(Error))
    );
    expect(result.current.spotify).toEqual([]);
    expect(result.current.favouriteGenre?.data).toEqual([]);
  });

  it('ignora respuestas que llegan tras desmontar', async () => {
    useAuthStore.setState({ currentUserId: 'u1' });
    useUserEntitiesStore.setState({
      users: { u1: { id: 'u1', spotifyConnected: true, favouriteGenresIds: ['g1'] } as any },
    });
    const resolvers: ((v: string[]) => void)[] = [];
    const pending = () => new Promise<string[]>((r) => resolvers.push(r));
    mocked.getPopular.mockImplementation(pending as any);
    mocked.getNew.mockImplementation(pending as any);
    mocked.getRecommended.mockImplementation(pending as any);
    mocked.getFromSpotify.mockImplementation(pending as any);
    mocked.getPopularByGenreId.mockImplementation(pending as any);

    const { unmount } = await renderHook(() => useFeedSongs());
    await unmount();
    await act(async () => resolvers.forEach((r) => r(['1'])));
    expect(console.error).not.toHaveBeenCalled();
  });

  it('vuelve a pedir Spotify cuando el usuario lo vincula', async () => {
    useAuthStore.setState({ currentUserId: 'u1' });
    useUserEntitiesStore.setState({ users: { u1: { id: 'u1', spotifyConnected: false } as any } });

    const { result } = await renderHook(() => useFeedSongs());
    await waitFor(() => expect(result.current.loading).toBe(false));

    await act(async () => {
      useUserEntitiesStore.getState().updateUser('u1', { spotifyConnected: true } as any);
    });
    await waitFor(() => expect(result.current.spotify.map((s) => s.id)).toEqual(['4']));
  });
});
