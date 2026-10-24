import { act, renderHook, waitFor } from '@testing-library/react-native';
import { useSong } from '../../hooks/useSong';
import {
  useAuthStore,
  useChordEntitiesStore,
  useInstrumentEntitiesStore,
  useSongEntitiesStore,
  useSongLessonEntitiesStore,
  useTabSectionEntitiesStore,
  useUserEntitiesStore,
} from '../../stores/entities';
import { useSongScreenStore } from '../../stores/ui/songScreen.store';
import * as songRepository from '../../repositories/song.repository';
import * as itemRepository from '../../repositories/item.repository';
import * as instrumentRepository from '../../repositories/instrument.repository';
import { DifficultyLevel } from '../../types';

jest.mock('../../repositories/song.repository', () => ({ getById: jest.fn() }));
jest.mock('../../repositories/item.repository', () => ({ markAsAccessed: jest.fn() }));
jest.mock('../../repositories/instrument.repository', () => ({ getAll: jest.fn(), getAllById: jest.fn() }));

const mockedSong = songRepository as jest.Mocked<typeof songRepository>;
const mockedItem = itemRepository as jest.Mocked<typeof itemRepository>;
const mockedInstrument = instrumentRepository as jest.Mocked<typeof instrumentRepository>;

const lesson = (id: string, instrumentId: string | undefined, difficulty: DifficultyLevel, extra: any = {}) =>
  ({ id, songId: 's1', instrumentId, difficulty, chordedLyrics: '', chordIds: [], ...extra }) as any;

const lessons = [
  lesson('gE', 'i1', DifficultyLevel.EASY, { chordIds: ['c1', 'falta'], tabSectionsIds: ['t1', 'falta'], chordedLyrics: '[C]Hola mundo' }),
  lesson('gO', 'i1', DifficultyLevel.ORIGINAL),
  lesson('pM', 'i2', DifficultyLevel.MEDIUM),
  lesson('pE', 'i2', DifficultyLevel.EASY),
  lesson('x', undefined, DifficultyLevel.EASY),
];

describe('useSong', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    useSongEntitiesStore.setState({
      songs: { s1: { id: 's1', title: 'Canción', lessonIds: [...lessons.map((l) => l.id), 'falta'] } as any, s2: { id: 's2' } as any },
    });
    useSongLessonEntitiesStore.setState({ songLessons: Object.fromEntries(lessons.map((l) => [l.id, l])) });
    useChordEntitiesStore.setState({ chords: { c1: { id: 'c1', name: 'C' } as any } });
    useTabSectionEntitiesStore.setState({ tabSections: { t1: { id: 't1' } as any } });
    useInstrumentEntitiesStore.setState({ instruments: { i1: { id: 'i1', name: 'Guitarra' } as any } });
    useAuthStore.setState({ token: null, currentUserId: null });
    useUserEntitiesStore.setState({ users: {} });
    useSongScreenStore.getState().reset();
    mockedSong.getById.mockResolvedValue({} as any);
    mockedInstrument.getAllById.mockResolvedValue([] as any);
  });

  it('carga la canción, la marca como accedida y elige la lección por defecto', async () => {
    const { result } = await renderHook(() => useSong('s1'));

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(mockedSong.getById).toHaveBeenCalledWith('s1');
    expect(mockedItem.markAsAccessed).toHaveBeenCalledWith('s1');
    expect(result.current.song).toEqual(expect.objectContaining({ id: 's1' }));
    expect(result.current.selectedInstrumentId).toBe('i1');
    expect(result.current.selectedLesson?.id).toBe('gE');
    expect(result.current.availableDifficulties).toEqual([DifficultyLevel.EASY, DifficultyLevel.ORIGINAL]);
    expect(result.current.chords.map((c) => c.id)).toEqual(['c1']);
    expect(result.current.tabSections.map((t) => t.id)).toEqual(['t1']);
    expect(result.current.parsedLyrics).toHaveLength(1);
    expect(result.current.availableInstruments).toEqual([
      { id: 'i1', name: 'Guitarra' },
      { id: 'i2', name: 'General' },
      { id: '', name: 'General' },
    ]);
    // Trae los instrumentos que faltan (sin el vacío)
    expect(mockedInstrument.getAllById).toHaveBeenCalledWith(['i2']);
  });

  it('prefiere el instrumento favorito del usuario y la versión original si es avanzado', async () => {
    useAuthStore.setState({ currentUserId: 'u1' });
    useUserEntitiesStore.setState({
      users: { u1: { id: 'u1', favouriteInstrumentsIds: [99, 'i1'], difficultyLevel: 5 } as any },
    });

    const { result } = await renderHook(() => useSong('s1'));
    expect(result.current.selectedInstrumentId).toBe('i1');
    expect(result.current.selectedLesson?.id).toBe('gO');
  });

  it('un usuario avanzado sin versión original usa la primera lección', async () => {
    useAuthStore.setState({ currentUserId: 'u1' });
    useUserEntitiesStore.setState({
      users: { u1: { id: 'u1', favouriteInstrumentsIds: ['i2'], difficultyLevel: 5 } as any },
    });

    const { result } = await renderHook(() => useSong('s1'));
    expect(result.current.selectedInstrumentId).toBe('i2');
    expect(result.current.availableDifficulties).toEqual([DifficultyLevel.EASY, DifficultyLevel.MEDIUM]);
    expect(result.current.selectedLesson?.id).toBe('pM');
  });

  it('permite cambiar de instrumento y dificultad', async () => {
    const { result } = await renderHook(() => useSong('s1'));

    await act(async () => result.current.changeInstrument('i2'));
    expect(result.current.selectedInstrumentId).toBe('i2');
    expect(result.current.selectedLesson?.id).toBe('pM');

    await act(async () => result.current.changeDifficulty(DifficultyLevel.EASY));
    expect(result.current.selectedLesson?.id).toBe('pE');
    expect(result.current.chords).toEqual([]);
    expect(result.current.tabSections).toEqual([]);
    expect(result.current.parsedLyrics).toEqual([]);
  });

  it('ignora selecciones que no existen para la canción', async () => {
    const { result } = await renderHook(() => useSong('s1'));

    await act(async () => result.current.changeInstrument('no-existe'));
    expect(result.current.selectedInstrumentId).toBe('i1');

    await act(async () => result.current.changeDifficulty(DifficultyLevel.MEDIUM));
    expect(result.current.selectedLesson?.id).toBe('gE');
  });

  it('una canción sin lecciones no tiene lección seleccionada', async () => {
    const { result } = await renderHook(() => useSong('s2'));
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.selectedLesson).toBeUndefined();
    expect(result.current.availableInstruments).toEqual([]);
    expect(result.current.availableDifficulties).toEqual([]);
    expect(result.current.selectedInstrumentId).toBeUndefined();
  });

  it('reinicia la selección al cambiar de canción', async () => {
    const { result, rerender } = await renderHook(({ id }: { id: string }) => useSong(id), {
      initialProps: { id: 's1' },
    });
    await act(async () => result.current.changeInstrument('i2'));
    expect(useSongScreenStore.getState().selectedInstrumentId).toBe('i2');

    await rerender({ id: 's2' });
    expect(useSongScreenStore.getState().selectedInstrumentId).toBeNull();
  });

  it('si el repositorio falla deja de cargar y registra el error', async () => {
    mockedSong.getById.mockRejectedValue(new Error('red caída'));
    const { result } = await renderHook(() => useSong('s1'));
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(console.error).toHaveBeenCalledWith('[useSong] Error cargando la canción:', expect.any(Error));
  });

  it('no marca como accedido sin id', async () => {
    await renderHook(() => useSong(''));
    expect(mockedItem.markAsAccessed).not.toHaveBeenCalled();
  });
});
