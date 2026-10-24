import { act, renderHook, waitFor } from '@testing-library/react-native';
import { useGenres, useGenresByIds } from '../../hooks/useGenres';
import { useGenreEntitiesStore } from '../../stores/entities';
import * as genreRepository from '../../repositories/genre.repository';

jest.mock('../../repositories/genre.repository', () => ({ getAll: jest.fn(), getAllById: jest.fn() }));
const mocked = genreRepository as jest.Mocked<typeof genreRepository>;

describe('useGenres', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    useGenreEntitiesStore.setState({
      genres: {
        '1': { id: '1', title: 'Rock' } as any,
        '2': { id: '2', title: 'Pop Rock' } as any,
        '3': { id: '3', title: 'Jazz' } as any,
      },
    });
    mocked.getAll.mockResolvedValue([] as any);
    mocked.getAllById.mockResolvedValue([] as any);
  });

  it('carga los géneros y devuelve todos sin búsqueda', async () => {
    let resolve: (v: any) => void = () => {};
    mocked.getAll.mockReturnValue(new Promise((r) => { resolve = r; }));
    const { result } = await renderHook(() => useGenres());
    expect(result.current.loading).toBe(true);

    await act(async () => resolve([]));
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(mocked.getAll).toHaveBeenCalledTimes(1);
    expect(result.current.genres).toHaveLength(3);
  });

  it('si el repositorio falla deja de cargar y registra el error', async () => {
    mocked.getAll.mockRejectedValue(new Error('red caída'));
    const { result } = await renderHook(() => useGenres());
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(console.error).toHaveBeenCalledWith('[useGenres] Error cargando géneros:', expect.any(Error));
  });

  it('filtra por título sin distinguir mayúsculas', async () => {
    const { result } = await renderHook(() => useGenres('ROCK'));
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.genres.map((g) => g.id)).toEqual(['1', '2']);
  });

  it('una búsqueda de espacios devuelve todos', async () => {
    const { result } = await renderHook(() => useGenres('   '));
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.genres).toHaveLength(3);
  });
});

describe('useGenresByIds', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    useGenreEntitiesStore.setState({ genres: { '1': { id: '1', title: 'Rock' } as any } });
    mocked.getAllById.mockResolvedValue([] as any);
  });

  it('resuelve los que están y pide solo los que faltan', async () => {
    const { result } = await renderHook(() => useGenresByIds([1, '2']));
    expect(result.current.map((g) => g.id)).toEqual(['1']);
    expect(mocked.getAllById).toHaveBeenCalledWith(['2']);
  });

  it('no pide nada si todos están en la Store', async () => {
    await renderHook(() => useGenresByIds(['1']));
    expect(mocked.getAllById).not.toHaveBeenCalled();
  });

  it('se actualiza cuando llegan los que faltaban', async () => {
    const { result } = await renderHook(() => useGenresByIds(['1', '2']));
    await act(async () => {
      useGenreEntitiesStore.getState().setGenres([{ id: '2', title: 'Pop' } as any]);
    });
    expect(result.current.map((g) => g.id)).toEqual(['1', '2']);
  });

  it('registra el error si falla la carga', async () => {
    mocked.getAllById.mockRejectedValue(new Error('red'));
    await renderHook(() => useGenresByIds(['9']));
    await waitFor(() => expect(console.error).toHaveBeenCalledWith(expect.any(Error)));
  });
});
