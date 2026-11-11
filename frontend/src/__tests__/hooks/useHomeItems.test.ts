import { act, renderHook, waitFor } from '@testing-library/react-native';
import { useHomeItems } from '../../hooks/useHomeItems';
import {
  useAuthStore,
  useItemEntitiesStore,
  useItemListEntitiesStore,
  useRecentItemsStore,
  useUserEntitiesStore,
} from '../../stores/entities';
import * as itemFeedRepository from '../../repositories/itemFeed.repository';
import * as instrumentGenreRepository from '../../repositories/instrumentGenre.repository';
import * as genreRepository from '../../repositories/genre.repository';
import * as songRepository from '../../repositories/song.repository';
import { ItemType } from '../../types';

jest.mock('../../repositories/itemFeed.repository', () => ({ getRecommendedForUser: jest.fn(), getFromSpotifyForUser: jest.fn() }));
jest.mock('../../repositories/instrumentGenre.repository', () => ({ getFavouriteCombinations: jest.fn() }));
jest.mock('../../repositories/genre.repository', () => ({ getMostUsedInItemLists: jest.fn() }));
jest.mock('../../repositories/song.repository', () => ({ getByFavouriteArtists: jest.fn() }));

const mockedFeed = itemFeedRepository as jest.Mocked<typeof itemFeedRepository>;
const mockedIG = instrumentGenreRepository as jest.Mocked<typeof instrumentGenreRepository>;
const mockedGenre = genreRepository as jest.Mocked<typeof genreRepository>;
const mockedSong = songRepository as jest.Mocked<typeof songRepository>;

const it_ = (id: string, type: ItemType) => ({ id, title: `T${id}`, type, imageUrl: '' }) as any;

describe('useHomeItems', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    useItemEntitiesStore.setState({
      items: {
        s1: it_('s1', ItemType.SONG),
        s2: it_('s2', ItemType.SONG),
        ig1: it_('ig1', ItemType.INSTRUMENT_GENRE),
        ig2: it_('ig2', ItemType.INSTRUMENT_GENRE),
        g1: it_('g1', ItemType.GENRE),
        g2: it_('g2', ItemType.GENRE),
        n1: { ...it_('n1', ItemType.SONG), artist: 'Queen' },
        n2: { ...it_('n2', ItemType.SONG), artist: 'Bowie' },
        n3: { ...it_('n3', ItemType.SONG), artist: 'Queen' },
        p1: { ...it_('p1', ItemType.SONG), artist: 'Queen' },
      },
    });
    useAuthStore.setState({ token: 'tk', currentUserId: 'u1', isAuthenticated: true });
    useUserEntitiesStore.setState({
      users: { u1: { id: 'u1', favouriteGenresIds: ['1'], favouriteInstrumentsIds: ['2'] } as any },
    });
    useItemListEntitiesStore.setState({ itemLists: {} });
    useRecentItemsStore.setState({ byUser: { u1: ['s2', 'borrado'] } });
    mockedFeed.getRecommendedForUser.mockResolvedValue(['s1', 'ig1']);
    mockedIG.getFavouriteCombinations.mockResolvedValue(['s1', 'ig1', 'ig2']);
    mockedGenre.getMostUsedInItemLists.mockResolvedValue(['g1', 'g2']);
    mockedSong.getByFavouriteArtists.mockResolvedValue({ newSongs: [], popularSongs: [] });
    mockedFeed.getFromSpotifyForUser.mockResolvedValue([]);
  });

  describe('desde tu Spotify', () => {
    const connectSpotify = (connected: boolean) =>
      useUserEntitiesStore.setState({
        users: { u1: { id: 'u1', favouriteGenresIds: ['1'], favouriteInstrumentsIds: ['2'], spotifyConnected: connected } as any },
      });

    it('sin Spotify vinculado no se pide', async () => {
      const { result } = await renderHook(() => useHomeItems());
      await waitFor(() => expect(result.current.loading).toBe(false));
      expect(mockedFeed.getFromSpotifyForUser).not.toHaveBeenCalled();
      expect(result.current.spotifyItems).toEqual([]);
      expect(result.current.spotifyConnected).toBeUndefined();
    });

    it('con Spotify vinculado carga sus items y no los repite en las sugerencias', async () => {
      connectSpotify(true);
      mockedFeed.getFromSpotifyForUser.mockResolvedValue(['s1', 'g1']);
      const { result } = await renderHook(() => useHomeItems());

      await waitFor(() => expect(result.current.spotifyItems.map((i) => i.id)).toEqual(['s1', 'g1']));
      expect(mockedFeed.getFromSpotifyForUser).toHaveBeenCalledWith('u1', expect.any(Function));
      expect(result.current.spotifyConnected).toBe(true);
      // g1 ya sale en el carrusel de Spotify: la tarjeta de género sugiere el siguiente
      await waitFor(() => expect(result.current.mostUsedGenre?.id).toBe('g2'));
    });

    it('se pide en cuanto el usuario vincula la cuenta', async () => {
      connectSpotify(false);
      await renderHook(() => useHomeItems());
      expect(mockedFeed.getFromSpotifyForUser).not.toHaveBeenCalled();

      await act(async () => connectSpotify(true));
      await waitFor(() => expect(mockedFeed.getFromSpotifyForUser).toHaveBeenCalledTimes(1));
    });

    it('si falla solo se registra el error, sin afectar al resto del Home', async () => {
      connectSpotify(true);
      mockedFeed.getFromSpotifyForUser.mockRejectedValue(new Error('401'));
      const { result } = await renderHook(() => useHomeItems());

      await waitFor(() => expect(console.error).toHaveBeenCalledWith('[useHomeItems] Error en Spotify:', expect.any(Error)));
      expect(result.current.error).toBeNull();
      expect(result.current.spotifyItems).toEqual([]);
    });
  });

  it('carga recientes, recomendaciones y sugerencias que no se ven ya en el Home', async () => {
    const { result } = await renderHook(() => useHomeItems());

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.recentItems.map((i) => i.id)).toEqual(['s2']);
    expect(result.current.recommendations.map((i) => i.id)).toEqual(['s1', 'ig1']);
    await waitFor(() => expect(result.current.favouriteCombination?.id).toBe('ig2'));
    expect(result.current.mostUsedGenre?.id).toBe('g1');
    expect(result.current.error).toBeNull();
    expect(result.current.getItemById('s1')).toEqual(expect.objectContaining({ id: 's1' }));
    expect(result.current.getItemById('nope')).toBeUndefined();
  });

  it('si todas las sugerencias ya se ven, usa la primera', async () => {
    mockedIG.getFavouriteCombinations.mockResolvedValue(['ig1']);
    const { result } = await renderHook(() => useHomeItems());
    await waitFor(() => expect(result.current.favouriteCombination?.id).toBe('ig1'));
  });

  it('sin candidatos del tipo adecuado la sugerencia es null', async () => {
    mockedGenre.getMostUsedInItemLists.mockResolvedValue(['s1']);
    const { result } = await renderHook(() => useHomeItems());
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.mostUsedGenre).toBeNull();
  });

  it('sin sesión no pide nada y las secciones están vacías', async () => {
    useAuthStore.setState({ token: null, currentUserId: null, isAuthenticated: false });
    const { result } = await renderHook(() => useHomeItems());

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(mockedFeed.getRecommendedForUser).not.toHaveBeenCalled();
    expect(mockedIG.getFavouriteCombinations).not.toHaveBeenCalled();
    expect(mockedGenre.getMostUsedInItemLists).not.toHaveBeenCalled();
    expect(result.current.recentItems).toEqual([]);
    expect(result.current.favouriteCombination).toBeNull();
    expect(result.current.mostUsedGenre).toBeNull();
  });

  it('muestra un error si fallan las recomendaciones', async () => {
    mockedFeed.getRecommendedForUser.mockRejectedValue(new Error('red'));
    const { result } = await renderHook(() => useHomeItems());

    await waitFor(() => expect(result.current.error).toBe('Error al sincronizar el inicio'));
    expect(result.current.loading).toBe(false);
  });

  it('registra los errores de las sugerencias', async () => {
    mockedIG.getFavouriteCombinations.mockRejectedValue(new Error('x'));
    await renderHook(() => useHomeItems());
    await waitFor(() =>
      expect(console.error).toHaveBeenCalledWith(
        '[useHomeItems] Error en la combinación de favoritos:',
        expect.any(Error)
      )
    );
  });

  it('aplica las recomendaciones frescas de onFresh', async () => {
    mockedFeed.getRecommendedForUser.mockImplementation(async (_u: string, onFresh: any) => {
      setTimeout(() => onFresh(['s2']), 0);
      return ['s1'];
    });
    const { result } = await renderHook(() => useHomeItems());
    await waitFor(() => expect(result.current.recommendations.map((i) => i.id)).toEqual(['s2']));
  });

  it('vuelve a pedir las sugerencias cuando cambian las listas del usuario', async () => {
    const { result } = await renderHook(() => useHomeItems());
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(mockedGenre.getMostUsedInItemLists).toHaveBeenCalledTimes(1);

    await act(async () => {
      useItemListEntitiesStore.setState({
        itemLists: {
          L1: { id: 'L1', userId: 'u1', itemIds: ['s1'] } as any,
          L2: { id: 'L2', userId: 'otro', itemIds: ['s2'] } as any,
          L3: { id: 'L3', userId: 'u1' } as any,
        },
      });
    });
    await waitFor(() => expect(mockedGenre.getMostUsedInItemLists).toHaveBeenCalledTimes(2));
    expect(mockedIG.getFavouriteCombinations).toHaveBeenCalledTimes(2);
  });

  it('usa claves vacías si el usuario no está en la Store', async () => {
    useUserEntitiesStore.setState({ users: {} });
    useRecentItemsStore.setState({ byUser: {} });
    const { result } = await renderHook(() => useHomeItems());
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.recentItems).toEqual([]);
  });

  it('ignora respuestas tras desmontar', async () => {
    const resolvers: ((v: string[]) => void)[] = [];
    const pending = () => new Promise<string[]>((r) => resolvers.push(r));
    mockedFeed.getRecommendedForUser.mockImplementation(pending as any);
    mockedIG.getFavouriteCombinations.mockImplementation(pending as any);
    mockedGenre.getMostUsedInItemLists.mockImplementation(pending as any);
    let resolveArtists!: (v: any) => void;
    mockedSong.getByFavouriteArtists.mockReturnValue(new Promise((r) => (resolveArtists = r)));

    const { unmount } = await renderHook(() => useHomeItems());
    await unmount();
    await act(async () => {
      resolvers.forEach((r) => r(['s1']));
      resolveArtists({ newSongs: ['n1'], popularSongs: ['p1'] });
    });
    expect(console.error).not.toHaveBeenCalled();
  });

  describe('artistas favoritos', () => {
    it('la tarjeta destaca la primera canción y el carrusel muestra el resto', async () => {
      mockedSong.getByFavouriteArtists.mockResolvedValue({ newSongs: ['n1', 'n2', 'n3'], popularSongs: ['p1', 'n3'] });
      const { result } = await renderHook(() => useHomeItems());

      await waitFor(() => expect(result.current.newFavouriteArtistSong?.id).toBe('n1'));
      expect(mockedSong.getByFavouriteArtists).toHaveBeenCalledWith('u1', expect.any(Object));
      expect(result.current.newFavouriteArtistSongs.map((i) => i.id)).toEqual(['n2', 'n3']);
      expect(result.current.popularFavouriteArtistSong?.id).toBe('p1');
      expect(result.current.popularFavouriteArtistSongs.map((i) => i.id)).toEqual(['n3']);
    });

    it('la tarjeta evita una canción que ya sale en Recientes o Para ti', async () => {
      // s2 está en Recientes
      mockedSong.getByFavouriteArtists.mockResolvedValue({ newSongs: ['s2', 'n2'], popularSongs: [] });
      const { result } = await renderHook(() => useHomeItems());

      await waitFor(() => expect(result.current.newFavouriteArtistSong?.id).toBe('n2'));
      expect(result.current.newFavouriteArtistSongs.map((i) => i.id)).toEqual(['s2']);
    });

    it('las dos tarjetas no muestran la misma canción aunque sea nueva y popular', async () => {
      mockedSong.getByFavouriteArtists.mockResolvedValue({ newSongs: ['n1'], popularSongs: ['n1', 'p1'] });
      const { result } = await renderHook(() => useHomeItems());

      await waitFor(() => expect(result.current.newFavouriteArtistSong?.id).toBe('n1'));
      expect(result.current.popularFavouriteArtistSong?.id).toBe('p1');
      // En el carrusel de populares sí puede aparecer (solo se quita la de su propia tarjeta)
      expect(result.current.popularFavouriteArtistSongs.map((i) => i.id)).toEqual(['n1']);
    });

    it('aplica los IDs frescos de cada carrusel cuando llega la red', async () => {
      let onFresh: any;
      mockedSong.getByFavouriteArtists.mockImplementation(async (_userId: string, callbacks: any) => {
        onFresh = callbacks;
        return { newSongs: ['n1'], popularSongs: [] };
      });
      const { result } = await renderHook(() => useHomeItems());
      await waitFor(() => expect(result.current.newFavouriteArtistSong?.id).toBe('n1'));

      await act(async () => {
        onFresh.newSongs(['n2', 'n3']);
        onFresh.popularSongs(['p1']);
      });
      expect(result.current.newFavouriteArtistSong?.id).toBe('n2');
      expect(result.current.popularFavouriteArtistSong?.id).toBe('p1');
    });

    it('sin canciones no hay tarjetas y los carruseles quedan vacíos', async () => {
      const { result } = await renderHook(() => useHomeItems());
      await waitFor(() => expect(result.current.loading).toBe(false));

      expect(result.current.newFavouriteArtistSong).toBeNull();
      expect(result.current.newFavouriteArtistSongs).toEqual([]);
      expect(result.current.popularFavouriteArtistSong).toBeNull();
      expect(result.current.popularFavouriteArtistSongs).toEqual([]);
    });

    it('se vuelve a pedir cuando cambian las listas del usuario', async () => {
      await renderHook(() => useHomeItems());
      await waitFor(() => expect(mockedSong.getByFavouriteArtists).toHaveBeenCalledTimes(1));

      await act(async () => {
        useItemListEntitiesStore.setState({ itemLists: { l1: { id: 'l1', userId: 'u1', itemIds: ['n1'] } as any } });
      });
      await waitFor(() => expect(mockedSong.getByFavouriteArtists).toHaveBeenCalledTimes(2));
    });

    it('sin sesión no se pide nada', async () => {
      useAuthStore.setState({ token: null, currentUserId: null, isAuthenticated: false } as any);
      await renderHook(() => useHomeItems());
      expect(mockedSong.getByFavouriteArtists).not.toHaveBeenCalled();
    });

    it('si falla registra el error', async () => {
      mockedSong.getByFavouriteArtists.mockRejectedValue(new Error('red'));
      await renderHook(() => useHomeItems());
      await waitFor(() =>
        expect(console.error).toHaveBeenCalledWith('[useHomeItems] Error en las canciones de los artistas favoritos:', expect.any(Error))
      );
    });
  });
});
