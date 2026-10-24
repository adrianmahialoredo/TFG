import { act, renderHook, waitFor } from '@testing-library/react-native';
import { useTunes } from '../../hooks/useTunes';
import { useInstrumentEntitiesStore, useTuneEntitiesStore } from '../../stores/entities';
import * as tuneRepository from '../../repositories/tune.repository';
import * as instrumentRepository from '../../repositories/instrument.repository';

jest.mock('../../repositories/tune.repository', () => ({ getAll: jest.fn() }));
jest.mock('../../repositories/instrument.repository', () => ({ getAll: jest.fn(), getAllById: jest.fn() }));

let mockUser: any = null;
jest.mock('../../context/AuthContext', () => ({ useAuth: () => ({ user: mockUser }) }));

const tunes = jest.mocked(tuneRepository);
const instruments = jest.mocked(instrumentRepository);

const tune = (id: string, title: string, instrumentId: string) => ({ id, title, instrumentId, notes: ['E2'] });

describe('useTunes', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockUser = null;
    useTuneEntitiesStore.setState({
      tunes: {
        '1': tune('1', 'Estándar', '1'),
        '2': tune('2', 'Drop D', '1'),
        '6': tune('6', 'Estándar', '3'),
        '9': tune('9', 'Estándar', '2'),
      },
    });
    useInstrumentEntitiesStore.setState({
      instruments: {
        '1': { id: '1', name: 'Guitarra' } as any,
        '2': { id: '2', name: 'Ukelele' } as any,
        '3': { id: '3', name: 'Bajo' } as any,
      },
    });
    tunes.getAll.mockResolvedValue(['2', '1', '9', '6']);
    instruments.getAllById.mockResolvedValue([]);
  });

  it('ordena por instrumento y, dentro de cada uno, por id, con la etiqueta "Instrumento (Afinación)"', async () => {
    const { result } = await renderHook(() => useTunes());
    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(result.current.tunes.map((t) => t.label)).toEqual([
      'Bajo (Estándar)',
      'Guitarra (Estándar)',
      'Guitarra (Drop D)',
      'Ukelele (Estándar)',
    ]);
  });

  it('sin favoritos, la afinación por defecto es la primera', async () => {
    const { result } = await renderHook(() => useTunes());
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.defaultTuneId).toBe('6');
  });

  it('por defecto elige la primera afinación de un instrumento favorito', async () => {
    mockUser = { id: '7', favouriteInstrumentsIds: [5, 1] };
    const { result } = await renderHook(() => useTunes());
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.defaultTuneId).toBe('1');
  });

  it('sin afinaciones no hay ninguna por defecto', async () => {
    tunes.getAll.mockResolvedValue([]);
    const { result } = await renderHook(() => useTunes());
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.tunes).toEqual([]);
    expect(result.current.defaultTuneId).toBeNull();
  });

  it('pide los instrumentos que falten y no muestra sus afinaciones hasta tenerlos', async () => {
    useInstrumentEntitiesStore.setState({ instruments: { '1': { id: '1', name: 'Guitarra' } as any } });
    const { result } = await renderHook(() => useTunes());
    await waitFor(() => expect(result.current.loading).toBe(false));

    expect(instruments.getAllById).toHaveBeenCalledWith(['2', '3']);
    expect(result.current.tunes.map((t) => t.id)).toEqual(['1', '2']);
  });

  it('aplica los IDs frescos cuando llega la red', async () => {
    let onFresh!: (ids: string[]) => void;
    tunes.getAll.mockImplementation(async (callback?: (ids: string[]) => void) => {
      onFresh = callback!;
      return ['1'];
    });
    const { result } = await renderHook(() => useTunes());
    await waitFor(() => expect(result.current.tunes).toHaveLength(1));

    await act(async () => onFresh(['1', '2']));
    expect(result.current.tunes.map((t) => t.id)).toEqual(['1', '2']);
  });

  it('si el repositorio falla deja de cargar y registra el error', async () => {
    tunes.getAll.mockRejectedValue(new Error('red caída'));
    const { result } = await renderHook(() => useTunes());
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(console.error).toHaveBeenCalledWith('[useTunes] Error cargando afinaciones:', expect.any(Error));
  });
});
