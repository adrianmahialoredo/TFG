import { act, renderHook, waitFor } from '@testing-library/react-native';
import { useInstruments, useInstrumentsByIds } from '../../hooks/useInstruments';
import { useInstrumentEntitiesStore } from '../../stores/entities';
import * as instrumentRepository from '../../repositories/instrument.repository';

jest.mock('../../repositories/instrument.repository', () => ({ getAll: jest.fn(), getAllById: jest.fn() }));
const mocked = instrumentRepository as jest.Mocked<typeof instrumentRepository>;

describe('useInstruments', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    useInstrumentEntitiesStore.setState({
      instruments: {
        '1': { id: '1', name: 'Guitarra' } as any,
        '2': { id: '2', name: 'Guitarra eléctrica' } as any,
        '3': { id: '3', name: 'Piano' } as any,
      },
    });
    mocked.getAll.mockResolvedValue([] as any);
    mocked.getAllById.mockResolvedValue([] as any);
  });

  it('carga los instrumentos con estado de carga', async () => {
    let resolve: (v: any) => void = () => {};
    mocked.getAll.mockReturnValue(new Promise((r) => { resolve = r; }));
    const { result } = await renderHook(() => useInstruments());
    expect(result.current.loading).toBe(true);

    await act(async () => resolve([]));
    expect(result.current.loading).toBe(false);
    expect(result.current.instruments).toHaveLength(3);
  });

  it('si el repositorio falla deja de cargar y registra el error', async () => {
    mocked.getAll.mockRejectedValue(new Error('red caída'));
    const { result } = await renderHook(() => useInstruments());
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(console.error).toHaveBeenCalledWith('[useInstruments] Error cargando instrumentos:', expect.any(Error));
  });

  it('filtra por nombre sin distinguir mayúsculas', async () => {
    const { result } = await renderHook(() => useInstruments('GUITARRA'));
    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.instruments.map((i) => i.id)).toEqual(['1', '2']);
  });

  it('una búsqueda de espacios devuelve todos', async () => {
    const { result } = await renderHook(() => useInstruments('  '));
    expect(result.current.instruments).toHaveLength(3);
  });
});

describe('useInstrumentsByIds', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    useInstrumentEntitiesStore.setState({ instruments: { '1': { id: '1', name: 'Guitarra' } as any } });
    mocked.getAllById.mockResolvedValue([] as any);
  });

  it('resuelve los presentes y pide solo los que faltan', async () => {
    const { result } = await renderHook(() => useInstrumentsByIds([1, 2]));
    expect(result.current.map((i) => i.id)).toEqual(['1']);
    expect(mocked.getAllById).toHaveBeenCalledWith(['2']);
  });

  it('no pide nada si todos están', async () => {
    await renderHook(() => useInstrumentsByIds(['1']));
    expect(mocked.getAllById).not.toHaveBeenCalled();
  });

  it('registra el error si falla', async () => {
    mocked.getAllById.mockRejectedValue(new Error('red'));
    await renderHook(() => useInstrumentsByIds(['5']));
    await waitFor(() => expect(console.error).toHaveBeenCalledWith(expect.any(Error)));
  });
});
