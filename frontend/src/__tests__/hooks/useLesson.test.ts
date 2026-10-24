import { act, renderHook, waitFor } from '@testing-library/react-native';
import { useLesson } from '../../hooks/useLesson';
import {
  useGenreEntitiesStore,
  useLessonEntitiesStore,
  useLessonSectionEntitiesStore,
} from '../../stores/entities';
import * as lessonRepository from '../../repositories/lesson.repository';
import * as genreRepository from '../../repositories/genre.repository';
import * as itemRepository from '../../repositories/item.repository';

jest.mock('../../repositories/lesson.repository', () => ({ getById: jest.fn() }));
jest.mock('../../repositories/genre.repository', () => ({ getById: jest.fn() }));
jest.mock('../../repositories/item.repository', () => ({ markAsAccessed: jest.fn() }));

const mockedLesson = lessonRepository as jest.Mocked<typeof lessonRepository>;
const mockedGenre = genreRepository as jest.Mocked<typeof genreRepository>;
const mockedItem = itemRepository as jest.Mocked<typeof itemRepository>;

describe('useLesson', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    useLessonEntitiesStore.setState({
      lessons: {
        l1: { id: 'l1', genreId: 'g1', sectionIds: ['s2', 's1', 's3', 'falta'] } as any,
        l2: { id: 'l2', sectionIds: ['s1'] } as any,
        l3: { id: 'l3' } as any,
      },
    });
    useLessonSectionEntitiesStore.setState({
      lessonSections: {
        s1: { id: 's1', orderIndex: 0 } as any,
        s2: { id: 's2', orderIndex: 1 } as any,
        s3: { id: 's3', orderIndex: 2 } as any,
      },
    });
    useGenreEntitiesStore.setState({ genres: { g1: { id: 'g1', title: 'Rock' } as any } });
    mockedLesson.getById.mockImplementation(async (id: string) => useLessonEntitiesStore.getState().lessons[id] as any);
    mockedGenre.getById.mockResolvedValue({} as any);
  });

  it('carga la lección y su género, ordena las secciones y marca como accedida', async () => {
    const { result } = await renderHook(() => useLesson('l1'));

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(mockedLesson.getById).toHaveBeenCalledWith('l1');
    expect(mockedGenre.getById).toHaveBeenCalledWith('g1');
    expect(mockedItem.markAsAccessed).toHaveBeenCalledWith('l1');
    expect(result.current.genre).toEqual({ id: 'g1', title: 'Rock' });
    expect(result.current.sections.map((s) => s.id)).toEqual(['s1', 's2', 's3']);
    expect(result.current.currentSection?.id).toBe('s1');
    expect(result.current.hasPrevious).toBe(false);
    expect(result.current.hasNext).toBe(true);
  });

  it('navega entre secciones respetando los límites', async () => {
    const { result } = await renderHook(() => useLesson('l1'));

    await act(async () => { result.current.goToPrevious(); });
    expect(result.current.sectionIndex).toBe(0);

    await act(async () => { result.current.goToNext(); });
    await act(async () => { result.current.goToNext(); });
    expect(result.current.sectionIndex).toBe(2);
    expect(result.current.hasNext).toBe(false);

    await act(async () => { result.current.goToNext(); });
    expect(result.current.sectionIndex).toBe(2);

    await act(async () => { result.current.goToPrevious(); });
    expect(result.current.currentSection?.id).toBe('s2');
    expect(result.current.hasPrevious).toBe(true);
  });

  it('vuelve a la primera sección al cambiar de lección', async () => {
    const { result, rerender } = await renderHook(({ id }: { id: string }) => useLesson(id), {
      initialProps: { id: 'l1' },
    });
    await act(async () => { result.current.goToNext(); });
    expect(result.current.sectionIndex).toBe(1);

    await rerender({ id: 'l2' });
    expect(result.current.sectionIndex).toBe(0);
    expect(result.current.sections.map((s) => s.id)).toEqual(['s1']);
    expect(result.current.genre).toBeUndefined();
  });

  it('no pide el género si la lección no pertenece a uno y no marca si markAccessed es false', async () => {
    const { result } = await renderHook(() => useLesson('l2', { markAccessed: false }));
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(mockedGenre.getById).not.toHaveBeenCalled();
    expect(mockedItem.markAsAccessed).not.toHaveBeenCalled();
  });

  it('sin secciones devuelve una lista vacía', async () => {
    const { result } = await renderHook(() => useLesson('l3'));
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.sections).toEqual([]);
    expect(result.current.currentSection).toBeUndefined();
  });

  it('sin id no carga nada y no se queda cargando', async () => {
    const { result } = await renderHook(() => useLesson(''));
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(mockedLesson.getById).not.toHaveBeenCalled();
  });

  it('si el repositorio falla deja de cargar y registra el error', async () => {
    mockedLesson.getById.mockRejectedValue(new Error('red caída'));
    const { result } = await renderHook(() => useLesson('l1', { markAccessed: false }));
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(console.error).toHaveBeenCalledWith('[useLesson] Error cargando la lección:', expect.any(Error));
  });

  it('la carga cancelada no toca el estado', async () => {
    let resolve: (v: any) => void = () => {};
    mockedLesson.getById.mockReturnValue(new Promise((r) => { resolve = r; }));
    const { result, unmount } = await renderHook(() => useLesson('l2'));
    await unmount();
    await act(async () => resolve(null));
    expect(result.current.loading).toBe(true);
  });
});
