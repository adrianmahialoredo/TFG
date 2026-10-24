import { act, renderHook, waitFor } from '@testing-library/react-native';
import { useGenreMain } from '../../hooks/useGenreMain';
import { useGenreEntitiesStore } from '../../stores/entities';
import * as genreRepository from '../../repositories/genre.repository';
import * as instrumentGenreRepository from '../../repositories/instrumentGenre.repository';
import * as lessonRepository from '../../repositories/lesson.repository';
import * as itemRepository from '../../repositories/item.repository';

jest.mock('../../repositories/genre.repository', () => ({ getById: jest.fn() }));
jest.mock('../../repositories/instrumentGenre.repository', () => ({ getByGenreId: jest.fn() }));
jest.mock('../../repositories/lesson.repository', () => ({ getByGenreId: jest.fn() }));
jest.mock('../../repositories/item.repository', () => ({ markAsAccessed: jest.fn() }));

const mockedGenre = genreRepository as jest.Mocked<typeof genreRepository>;
const mockedIG = instrumentGenreRepository as jest.Mocked<typeof instrumentGenreRepository>;
const mockedLesson = lessonRepository as jest.Mocked<typeof lessonRepository>;
const mockedItem = itemRepository as jest.Mocked<typeof itemRepository>;

describe('useGenreMain', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    useGenreEntitiesStore.setState({ genres: { g1: { id: 'g1', title: 'Rock' } as any } });
    mockedGenre.getById.mockResolvedValue({} as any);
    mockedIG.getByGenreId.mockResolvedValue([] as any);
    mockedLesson.getByGenreId.mockResolvedValue([] as any);
    mockedItem.markAsAccessed.mockReturnValue(undefined as any);
  });

  it('carga los datos del género y lo marca como accedido', async () => {
    const { result } = await renderHook(() => useGenreMain('g1'));

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(mockedGenre.getById).toHaveBeenCalledWith('g1');
    expect(mockedIG.getByGenreId).toHaveBeenCalledWith('g1');
    expect(mockedLesson.getByGenreId).toHaveBeenCalledWith('g1');
    expect(mockedItem.markAsAccessed).toHaveBeenCalledWith('g1');
    expect(result.current.genre).toEqual({ id: 'g1', title: 'Rock' });
    expect(result.current.error).toBeNull();
    expect(result.current.selectedTab).toBe('Género');
  });

  it('tolera que fallen algunas cargas (allSettled)', async () => {
    mockedIG.getByGenreId.mockRejectedValue(new Error('x'));
    const { result } = await renderHook(() => useGenreMain('g1'));

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.error).toBeNull();
  });

  it('muestra un error si falla algo crítico', async () => {
    mockedItem.markAsAccessed.mockImplementation(() => {
      throw new Error('crítico');
    });
    const { result } = await renderHook(() => useGenreMain('g1'));

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.error).toBe('No se pudo cargar la información del género');
  });

  it('devuelve null si el género no está y no carga sin id', async () => {
    const { result } = await renderHook(() => useGenreMain(''));
    expect(result.current.genre).toBeNull();
    expect(result.current.loading).toBe(true);
    expect(mockedGenre.getById).not.toHaveBeenCalled();
  });

  it('changeTab cambia la pestaña seleccionada', async () => {
    const { result } = await renderHook(() => useGenreMain('g1'));
    await act(async () => result.current.changeTab('Canciones'));
    expect(result.current.selectedTab).toBe('Canciones');
  });
});
