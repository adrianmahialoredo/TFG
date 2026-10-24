import { act, renderHook, waitFor } from '@testing-library/react-native';
import { useHomeLessons } from '../../hooks/useHomeLessons';
import {
  useAuthStore,
  useInstrumentEntitiesStore,
  useItemEntitiesStore,
  useUserEntitiesStore,
} from '../../stores/entities';
import * as lessonRepository from '../../repositories/lesson.repository';
import * as instrumentRepository from '../../repositories/instrument.repository';
import { ItemType } from '../../types';

jest.mock('../../repositories/lesson.repository', () => ({
  getPopular: jest.fn(),
  getNew: jest.fn(),
  getRecommended: jest.fn(),
  getPopularByInstrumentId: jest.fn(),
}));
jest.mock('../../repositories/instrument.repository', () => ({ getAll: jest.fn(), getAllById: jest.fn() }));

const mocked = lessonRepository as jest.Mocked<typeof lessonRepository>;
const mockedInstrument = instrumentRepository as jest.Mocked<typeof instrumentRepository>;

describe('useHomeLessons', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    useItemEntitiesStore.setState({
      items: Object.fromEntries(
        ['1', '2', '3', '4', '5'].map((id) => [id, { id, title: `L${id}`, type: ItemType.LESSON, imageUrl: '' } as any])
      ),
    });
    useAuthStore.setState({ token: null, currentUserId: null, isAuthenticated: false });
    useUserEntitiesStore.setState({ users: {} });
    useInstrumentEntitiesStore.setState({ instruments: { i1: { id: 'i1', name: 'Guitarra' } as any } });
    mocked.getPopular.mockResolvedValue(['1']);
    mocked.getNew.mockResolvedValue(['2', 'falta']);
    mocked.getRecommended.mockResolvedValue(['3']);
    mocked.getPopularByInstrumentId.mockResolvedValue(['4']);
    mockedInstrument.getAllById.mockResolvedValue([] as any);
  });

  it('sin sesión carga populares y novedades', async () => {
    const { result } = await renderHook(() => useHomeLessons());

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.populars).toEqual([{ id: '1', title: 'L1', imgUrl: '', description: 'L1' }]);
    expect(result.current.news.map((l) => l.id)).toEqual(['2']);
    expect(result.current.recommendations).toEqual([]);
    expect(result.current.favouriteInstrument).toBeNull();
    expect(mocked.getRecommended).not.toHaveBeenCalled();
    expect(result.current.error).toBeNull();
  });

  it('con sesión pide recomendaciones y el carrusel del instrumento favorito', async () => {
    useAuthStore.setState({ currentUserId: 'u1' });
    useUserEntitiesStore.setState({ users: { u1: { id: 'u1', favouriteInstrumentsIds: ['i1'] } as any } });

    const { result } = await renderHook(() => useHomeLessons());

    await waitFor(() => expect(result.current.recommendations.map((l) => l.id)).toEqual(['3']));
    await waitFor(() => expect(result.current.favouriteInstrument?.data.map((l) => l.id)).toEqual(['4']));
    expect(result.current.favouriteInstrument?.title).toBe('Guitarra');
    expect(mocked.getPopularByInstrumentId).toHaveBeenCalledWith('i1', expect.any(Function));
    expect(mockedInstrument.getAllById).not.toHaveBeenCalled();
  });

  it('trae el instrumento favorito si no está en la Store', async () => {
    useAuthStore.setState({ currentUserId: 'u1' });
    useUserEntitiesStore.setState({ users: { u1: { id: 'u1', favouriteInstrumentsIds: [9] } as any } });

    const { result } = await renderHook(() => useHomeLessons());
    await waitFor(() => expect(result.current.favouriteInstrument?.data.length).toBe(1));
    expect(mockedInstrument.getAllById).toHaveBeenCalledWith(['9']);
    expect(result.current.favouriteInstrument?.title).toBe('');
  });

  it('muestra un error si falla una sección', async () => {
    mocked.getPopular.mockRejectedValue(new Error('red'));
    const { result } = await renderHook(() => useHomeLessons());
    await waitFor(() => expect(result.current.error).toBe('Error al sincronizar lecciones'));
    expect(result.current.loading).toBe(false);
  });

  it('registra el error del instrumento favorito', async () => {
    useAuthStore.setState({ currentUserId: 'u1' });
    useUserEntitiesStore.setState({ users: { u1: { id: 'u1', favouriteInstrumentsIds: ['i1'] } as any } });
    mocked.getPopularByInstrumentId.mockRejectedValue(new Error('x'));

    const { result } = await renderHook(() => useHomeLessons());
    await waitFor(() =>
      expect(console.error).toHaveBeenCalledWith('[useHomeLessons] Error en el instrumento favorito:', expect.any(Error))
    );
    expect(result.current.favouriteInstrument?.data).toEqual([]);
  });

  it('aplica los IDs frescos de onFresh', async () => {
    mocked.getPopular.mockImplementation(async (onFresh: any) => {
      setTimeout(() => onFresh(['5']), 0);
      return ['1'];
    });
    const { result } = await renderHook(() => useHomeLessons());
    await waitFor(() => expect(result.current.populars.map((l) => l.id)).toEqual(['5']));
  });

  it('ignora respuestas tras desmontar', async () => {
    useAuthStore.setState({ currentUserId: 'u1' });
    useUserEntitiesStore.setState({ users: { u1: { id: 'u1', favouriteInstrumentsIds: ['i1'] } as any } });
    const resolvers: ((v: string[]) => void)[] = [];
    const pending = () => new Promise<string[]>((r) => resolvers.push(r));
    mocked.getPopular.mockImplementation(pending as any);
    mocked.getNew.mockImplementation(pending as any);
    mocked.getRecommended.mockImplementation(pending as any);
    mocked.getPopularByInstrumentId.mockImplementation(pending as any);

    const { unmount } = await renderHook(() => useHomeLessons());
    await unmount();
    await act(async () => resolvers.forEach((r) => r(['1'])));
    expect(console.error).not.toHaveBeenCalled();
  });
});
