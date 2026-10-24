import { act, renderHook, waitFor } from '@testing-library/react-native';
import { useInstrumentGenre } from '../../hooks/useInstrumentGenre';
import { useInstrumentGenreLesson } from '../../hooks/useInstrumentGenreLesson';
import { useInstrumentGenreEntitiesStore, useLessonEntitiesStore } from '../../stores/entities';
import * as itemRepository from '../../repositories/item.repository';
import * as instrumentGenreRepository from '../../repositories/instrumentGenre.repository';
import * as lessonRepository from '../../repositories/lesson.repository';

jest.mock('../../repositories/item.repository', () => ({ markAsAccessed: jest.fn() }));
jest.mock('../../repositories/instrumentGenre.repository', () => ({ getById: jest.fn() }));
jest.mock('../../repositories/lesson.repository', () => ({ getAllById: jest.fn() }));

const mockedItem = itemRepository as jest.Mocked<typeof itemRepository>;
const mockedIG = instrumentGenreRepository as jest.Mocked<typeof instrumentGenreRepository>;
const mockedLesson = lessonRepository as jest.Mocked<typeof lessonRepository>;

const ig = { id: 'ig1', lessonIds: ['l1', 'l2', 'falta'] } as any;

describe('useInstrumentGenre', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    useInstrumentGenreEntitiesStore.setState({ instrumentGenres: {} });
    useLessonEntitiesStore.setState({
      lessons: { l1: { id: 'l1', title: 'Uno' } as any, l2: { id: 'l2', title: 'Dos' } as any },
    });
    mockedIG.getById.mockImplementation(async () => {
      useInstrumentGenreEntitiesStore.setState({ instrumentGenres: { ig1: ig } });
      return ig;
    });
    mockedLesson.getAllById.mockResolvedValue([] as any);
  });

  it('carga el género-instrumento, sus lecciones y lo marca como accedido', async () => {
    const { result } = await renderHook(() => useInstrumentGenre('ig1'));

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(mockedItem.markAsAccessed).toHaveBeenCalledWith('ig1');
    expect(mockedIG.getById).toHaveBeenCalledWith('ig1');
    expect(mockedLesson.getAllById).toHaveBeenCalledWith(['l1', 'l2', 'falta']);
    expect(result.current.instrumentGenre).toBe(ig);
    expect(result.current.lessons.map((l) => l.id)).toEqual(['l1', 'l2']);
  });

  it('empieza sin cargar si ya está en la Store', async () => {
    useInstrumentGenreEntitiesStore.setState({ instrumentGenres: { ig1: ig } });
    let resolve: (v: any) => void = () => {};
    mockedIG.getById.mockReturnValue(new Promise((r) => { resolve = r; }));

    const { result } = await renderHook(() => useInstrumentGenre('ig1'));
    expect(result.current.loading).toBe(false);
    await act(async () => resolve(ig));
  });

  it('no pide lecciones si no tiene', async () => {
    mockedIG.getById.mockResolvedValue({ id: 'ig1', lessonIds: [] } as any);
    const { result } = await renderHook(() => useInstrumentGenre('ig1'));
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(mockedLesson.getAllById).not.toHaveBeenCalled();
    expect(result.current.lessons).toEqual([]);
  });

  it('registra el error si falla la carga', async () => {
    mockedIG.getById.mockRejectedValue(new Error('red'));
    const { result } = await renderHook(() => useInstrumentGenre('ig1'));
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(console.error).toHaveBeenCalledWith(
      '[useInstrumentGenre] Error cargando el género-instrumento:',
      expect.any(Error)
    );
  });

  it('sin id no hace nada', async () => {
    const { result } = await renderHook(() => useInstrumentGenre(''));
    expect(mockedItem.markAsAccessed).not.toHaveBeenCalled();
    expect(mockedIG.getById).not.toHaveBeenCalled();
    expect(result.current.lessons).toEqual([]);
  });

  it('no actualiza el estado si se desmonta antes de terminar', async () => {
    let resolve: (v: any) => void = () => {};
    mockedIG.getById.mockReturnValue(new Promise((r) => { resolve = r; }));
    const { result, unmount } = await renderHook(() => useInstrumentGenre('ig1'));
    expect(result.current.loading).toBe(true);
    await unmount();
    await act(async () => resolve(null));
    expect(result.current.loading).toBe(true);
  });
});

describe('useInstrumentGenreLesson', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    useInstrumentGenreEntitiesStore.setState({ instrumentGenres: { ig1: ig } });
    useLessonEntitiesStore.setState({
      lessons: { l1: { id: 'l1', title: 'Uno' } as any, l2: { id: 'l2', title: 'Dos' } as any },
    });
    mockedIG.getById.mockResolvedValue(ig);
    mockedLesson.getAllById.mockResolvedValue([] as any);
  });

  it('activa la primera lección por defecto y permite cambiarla', async () => {
    const { result } = await renderHook(() => useInstrumentGenreLesson('ig1'));
    expect(result.current.instrumentGenre).toBe(ig);
    expect(result.current.lessons).toHaveLength(2);
    expect(result.current.activeLesson?.id).toBe('l1');

    await act(async () => result.current.setActiveLessonId('l2'));
    expect(result.current.activeLesson?.id).toBe('l2');

    await act(async () => result.current.setActiveLessonId('no-existe'));
    expect(result.current.activeLesson?.id).toBe('l1');
  });

  it('sin lecciones la lección activa es undefined', async () => {
    useInstrumentGenreEntitiesStore.setState({ instrumentGenres: {} });
    mockedIG.getById.mockResolvedValue(null as any);
    const { result } = await renderHook(() => useInstrumentGenreLesson('ig1'));
    expect(result.current.activeLesson).toBeUndefined();
  });

  it('expone el estado de carga del género-instrumento', async () => {
    useInstrumentGenreEntitiesStore.setState({ instrumentGenres: {} });
    let resolve: (v: any) => void = () => {};
    mockedIG.getById.mockReturnValue(new Promise((r) => { resolve = r; }));
    const { result } = await renderHook(() => useInstrumentGenreLesson('ig1'));
    expect(result.current.loading).toBe(true);

    await act(async () => resolve(null));
    expect(result.current.loading).toBe(false);
  });
});
