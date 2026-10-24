import { act, renderHook, waitFor } from '@testing-library/react-native';
import { useGenreSongs } from '../../hooks/useGenreSongs';
import { useItemEntitiesStore } from '../../stores/entities';
import * as songRepository from '../../repositories/song.repository';
import { ItemType } from '../../types';

jest.mock('../../repositories/song.repository', () => ({
  getPopularByGenreId: jest.fn(),
  getClassicByGenreId: jest.fn(),
  getNewByGenreId: jest.fn(),
}));

const mocked = songRepository as jest.Mocked<typeof songRepository>;

describe('useGenreSongs', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    useItemEntitiesStore.setState({
      items: Object.fromEntries(
        ['1', '2', '3', '4'].map((id) => [id, { id, title: `T${id}`, type: ItemType.SONG, imageUrl: '' } as any])
      ),
    });
    mocked.getPopularByGenreId.mockResolvedValue(['1']);
    mocked.getClassicByGenreId.mockResolvedValue(['2', 'falta']);
    mocked.getNewByGenreId.mockResolvedValue(['3']);
  });

  it('carga las tres secciones del género', async () => {
    const { result } = await renderHook(() => useGenreSongs('g1'));

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.populars.map((s) => s.id)).toEqual(['1']);
    expect(result.current.classics.map((s) => s.id)).toEqual(['2']);
    expect(result.current.news.map((s) => s.id)).toEqual(['3']);
    expect(result.current.error).toBeNull();
    expect(mocked.getPopularByGenreId).toHaveBeenCalledWith('g1', expect.any(Function));
  });

  it('con secciones vacías termina de cargar igualmente', async () => {
    mocked.getPopularByGenreId.mockResolvedValue([]);
    mocked.getClassicByGenreId.mockResolvedValue([]);
    mocked.getNewByGenreId.mockResolvedValue([]);
    const { result } = await renderHook(() => useGenreSongs('g1'));

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.populars).toEqual([]);
  });

  it('aplica los IDs frescos de onFresh', async () => {
    mocked.getNewByGenreId.mockImplementation(async (_g: string, onFresh: any) => {
      setTimeout(() => onFresh(['4']), 0);
      return ['3'];
    });
    const { result } = await renderHook(() => useGenreSongs('g1'));
    await waitFor(() => expect(result.current.news.map((s) => s.id)).toEqual(['4']));
  });

  it('muestra un error si falla una sección', async () => {
    mocked.getClassicByGenreId.mockRejectedValue(new Error('red'));
    const { result } = await renderHook(() => useGenreSongs('g1'));

    await waitFor(() => expect(result.current.error).toBe('Error al sincronizar canciones'));
    expect(result.current.loading).toBe(false);
  });

  it('ignora respuestas tras desmontar', async () => {
    const resolvers: ((v: string[]) => void)[] = [];
    const pending = () => new Promise<string[]>((r) => resolvers.push(r));
    mocked.getPopularByGenreId.mockImplementation(pending as any);
    mocked.getClassicByGenreId.mockImplementation(pending as any);
    mocked.getNewByGenreId.mockImplementation(pending as any);

    const { result, unmount } = await renderHook(() => useGenreSongs('g1'));
    const before = result.current.populars;
    await unmount();
    await act(async () => resolvers.forEach((r) => r(['1'])));
    expect(result.current.populars).toBe(before);
  });
});
