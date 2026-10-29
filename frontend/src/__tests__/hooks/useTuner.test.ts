import { act, renderHook } from '@testing-library/react-native';
import { Platform } from 'react-native';
import { useTuner } from '../../hooks/useTuner';

let mockRecording = false;
const mockRecorder = {
  onAudioReady: jest.fn((_options: any, _callback: any) => ({ status: 'success' })),
  clearOnAudioReady: jest.fn(),
  onError: jest.fn(),
  clearOnError: jest.fn(),
  start: jest.fn(async () => { mockRecording = true; return { status: 'success' }; }),
  stop: jest.fn(async () => { mockRecording = false; return { status: 'success' }; }),
  isRecording: jest.fn(() => mockRecording),
};
const mockAudioManager = {
  requestRecordingPermissions: jest.fn(),
  setAudioSessionOptions: jest.fn(),
  setAudioSessionActivity: jest.fn(async (_enabled: boolean) => {}),
};
// La factoría se evalúa al importar el hook, antes de que existan los mocks: se delega en ellos al llamar
jest.mock('react-native-audio-api', () => ({
  AudioRecorder: jest.fn(() => mockRecorder),
  AudioManager: {
    requestRecordingPermissions: () => mockAudioManager.requestRecordingPermissions(),
    setAudioSessionOptions: (options: any) => mockAudioManager.setAudioSessionOptions(options),
    setAudioSessionActivity: (enabled: boolean) => mockAudioManager.setAudioSessionActivity(enabled),
  },
}));

// jest-expo simula Expo Go (isRunningInExpoGo() es true): cada test elige la fuente de audio
let mockSimulated = false;
jest.mock('../../utils/microphone.utils', () => ({
  ...jest.requireActual('../../utils/microphone.utils'),
  isMicrophoneSimulated: () => mockSimulated,
}));

const STANDARD = ['E2', 'A2', 'D3', 'G3', 'B3', 'E4'];
const SAMPLE_RATE = 44100;

/** Bloque de audio como el que entrega el micrófono: una senoidal de `frequency` Hz (0 = silencio). */
const block = (frequency: number) => {
  const samples = new Float32Array(4096);
  if (frequency > 0) {
    for (let i = 0; i < samples.length; i++) samples[i] = 0.5 * Math.sin((2 * Math.PI * frequency * i) / SAMPLE_RATE);
  }
  return { buffer: { sampleRate: SAMPLE_RATE, getChannelData: () => samples }, numFrames: samples.length, when: 0 };
};

/** Envía un bloque al callback que registró el hook. */
const emit = async (frequency: number) => {
  const callback = mockRecorder.onAudioReady.mock.calls.at(-1)![1];
  await act(async () => callback(block(frequency)));
};

const flat = (frequency: number, cents: number) => frequency * Math.pow(2, cents / 1200);

describe('useTuner', () => {
  // Reloj parado: el análisis "no tarda nada" y no se descarta ningún bloque por ir lento
  // (salvo en el test del descarte, que mueve el reloj a mano)
  let nowSpy: jest.SpyInstance<number, []>;

  beforeEach(() => {
    jest.clearAllMocks();
    mockRecording = false;
    mockSimulated = false;
    mockAudioManager.requestRecordingPermissions.mockResolvedValue('Granted');
    nowSpy = jest.spyOn(Date, 'now').mockReturnValue(1000);
  });

  afterEach(() => nowSpy.mockRestore());

  it('si el análisis va lento descarta los bloques que llegan mientras tanto', async () => {
    const { result } = await renderHook(() => useTuner(STANDARD));
    await act(() => result.current.start());

    // 1.er bloque: empieza en 1000 y tarda 50 ms -> se descarta hasta 1100
    nowSpy.mockReturnValueOnce(1000).mockReturnValueOnce(1050);
    await emit(110);
    const first = result.current.reading!.frequency;
    expect(first).toBeCloseTo(110, 0);

    // Llega en 1080: se descarta sin analizarlo (la lectura no cambia)
    nowSpy.mockReturnValueOnce(1080);
    await emit(82.41);
    expect(result.current.reading!.frequency).toBe(first);

    // En 1100 ya se analiza (la lectura se mueve hacia el Mi, suavizada con la anterior)
    nowSpy.mockReturnValueOnce(1100).mockReturnValueOnce(1110);
    await emit(82.41);
    expect(result.current.reading!.frequency).toBeLessThan(first);
  });

  it('empieza parado', async () => {
    const { result } = await renderHook(() => useTuner(STANDARD));
    expect(result.current.status).toBe('idle');
    expect(result.current.reading).toBeNull();
    expect(result.current.isListening).toBe(false);
  });

  it('con permiso escucha el micrófono con la configuración del afinador', async () => {
    const { result } = await renderHook(() => useTuner(STANDARD));
    await act(() => result.current.start());

    expect(mockAudioManager.requestRecordingPermissions).toHaveBeenCalled();
    expect(mockRecorder.onAudioReady).toHaveBeenCalledWith(
      { sampleRate: 44100, bufferLength: 4096, channelCount: 1 },
      expect.any(Function)
    );
    expect(mockRecorder.start).toHaveBeenCalled();
    expect(result.current.status).toBe('listening');
    expect(result.current.isListening).toBe(true);
  });

  it('si ya está escuchando, start no vuelve a arrancar', async () => {
    const { result } = await renderHook(() => useTuner(STANDARD));
    await act(() => result.current.start());
    await act(() => result.current.start());
    expect(mockRecorder.start).toHaveBeenCalledTimes(1);
  });

  it('sin permiso no graba y queda en "denied"', async () => {
    mockAudioManager.requestRecordingPermissions.mockResolvedValue('Denied');
    const { result } = await renderHook(() => useTuner(STANDARD));
    await act(() => result.current.start());

    expect(result.current.status).toBe('denied');
    expect(mockRecorder.start).not.toHaveBeenCalled();
  });

  it('parar no oculta el aviso de permiso denegado', async () => {
    mockAudioManager.requestRecordingPermissions.mockResolvedValue('Denied');
    const { result } = await renderHook(() => useTuner(STANDARD));
    await act(() => result.current.start());
    await act(() => result.current.stop());
    expect(result.current.status).toBe('denied');
  });

  it('si el micrófono no arranca queda en "error" y libera el grabador', async () => {
    mockRecorder.start.mockResolvedValueOnce({ status: 'error', message: 'busy' } as any);
    const { result } = await renderHook(() => useTuner(STANDARD));
    await act(() => result.current.start());

    expect(result.current.status).toBe('error');
    expect(mockRecorder.clearOnAudioReady).toHaveBeenCalled();
    expect(console.error).toHaveBeenCalledWith('[useTuner] No se pudo iniciar el micrófono:', expect.any(Error));
  });

  it('un error del micrófono durante la escucha pasa a "error"', async () => {
    const { result } = await renderHook(() => useTuner(STANDARD));
    await act(() => result.current.start());
    const onError = mockRecorder.onError.mock.calls[0][0];
    await act(async () => onError({ message: 'interrumpido' }));
    expect(result.current.status).toBe('error');
  });

  it('compara la nota con la cuerda más cercana', async () => {
    const { result } = await renderHook(() => useTuner(STANDARD));
    await act(() => result.current.start());
    await emit(flat(110, -8)); // La2, 8 cents bajo

    expect(result.current.reading?.stringIndex).toBe(1);
    expect(result.current.reading?.frequency).toBeCloseTo(flat(110, -8), 1);
    expect(result.current.reading?.cents).toBeCloseTo(-8, 0);
  });

  it('suaviza con la mediana: un bloque raro no mueve la lectura', async () => {
    const { result } = await renderHook(() => useTuner(STANDARD));
    await act(() => result.current.start());
    await emit(110);
    await emit(110);
    await emit(150); // golpe o armónico suelto
    expect(result.current.reading?.frequency).toBeCloseTo(110, 0);
  });

  it('en silencio mantiene la última lectura', async () => {
    const { result } = await renderHook(() => useTuner(STANDARD));
    await act(() => result.current.start());
    await emit(82.41);
    await emit(0);
    expect(result.current.reading?.stringIndex).toBe(0);
  });

  it('una cuerda fijada se usa aunque suene otra', async () => {
    const { result } = await renderHook(() => useTuner(STANDARD));
    await act(() => result.current.start());
    await act(async () => result.current.selectString(0));
    await emit(110); // La2 contra el Mi2 fijado: 5 semitonos por encima

    expect(result.current.lockedString).toBe(0);
    expect(result.current.reading?.stringIndex).toBe(0);
    expect(result.current.reading?.cents).toBeCloseTo(500, 0);

    await act(async () => result.current.selectString(null));
    await emit(110);
    expect(result.current.reading?.stringIndex).toBe(1);
  });

  it('cambiar de afinación borra la lectura y la cuerda fijada', async () => {
    const { result, rerender } = await renderHook(({ notes }: { notes: string[] }) => useTuner(notes), {
      initialProps: { notes: STANDARD },
    });
    await act(() => result.current.start());
    await act(async () => result.current.selectString(2));
    await emit(146.83);
    expect(result.current.reading).not.toBeNull();

    await rerender({ notes: ['D2', 'A2', 'D3', 'G3', 'B3', 'E4'] });
    expect(result.current.reading).toBeNull();
    expect(result.current.lockedString).toBeNull();
  });

  it('ignora el audio si la afinación no tiene cuerdas (aún cargando)', async () => {
    const { result } = await renderHook(() => useTuner([]));
    await act(() => result.current.start());
    await emit(110);
    expect(result.current.reading).toBeNull();
  });

  it('stop deja de escuchar, para el grabador y borra la lectura', async () => {
    const { result } = await renderHook(() => useTuner(STANDARD));
    await act(() => result.current.start());
    await emit(110);
    await act(() => result.current.stop());

    expect(mockRecorder.clearOnAudioReady).toHaveBeenCalled();
    expect(mockRecorder.stop).toHaveBeenCalled();
    expect(result.current.status).toBe('idle');
    expect(result.current.reading).toBeNull();
  });

  it('un stop mientras se pide el permiso cancela el arranque', async () => {
    let grant!: (status: string) => void;
    mockAudioManager.requestRecordingPermissions.mockReturnValue(new Promise((r) => (grant = r)));
    const { result } = await renderHook(() => useTuner(STANDARD));

    let starting!: Promise<void>;
    await act(async () => { starting = result.current.start(); });
    expect(result.current.status).toBe('starting');
    await act(() => result.current.stop());
    await act(async () => { grant('Granted'); await starting; });

    expect(mockRecorder.start).not.toHaveBeenCalled();
    expect(result.current.status).toBe('idle');
  });

  it('al desmontar libera el micrófono', async () => {
    const { result, unmount } = await renderHook(() => useTuner(STANDARD));
    await act(() => result.current.start());
    await act(async () => unmount());
    expect(mockRecorder.stop).toHaveBeenCalled();
  });

  describe('en iOS', () => {
    const originalOS = Platform.OS;
    beforeEach(() => { (Platform as any).OS = 'ios'; });
    afterEach(() => { (Platform as any).OS = originalOS; });

    it('activa una sesión de grabación en modo medida y la desactiva al parar', async () => {
      const { result } = await renderHook(() => useTuner(STANDARD));
      await act(() => result.current.start());
      expect(mockAudioManager.setAudioSessionOptions).toHaveBeenCalledWith(
        { iosCategory: 'record', iosMode: 'measurement', iosOptions: [] }
      );
      expect(mockAudioManager.setAudioSessionActivity).toHaveBeenCalledWith(true);

      await act(() => result.current.stop());
      expect(mockAudioManager.setAudioSessionActivity).toHaveBeenLastCalledWith(false);
    });
  });

  describe('en Expo Go (micrófono simulado)', () => {
    beforeEach(() => {
      mockSimulated = true;
      jest.useFakeTimers();
    });
    afterEach(() => jest.useRealTimers());

    const advance = async (ms: number) => {
      await act(async () => { jest.advanceTimersByTime(ms); });
    };

    it('no usa el micrófono real ni pide permiso', async () => {
      const { result } = await renderHook(() => useTuner(STANDARD));
      await act(() => result.current.start());

      expect(result.current.simulated).toBe(true);
      expect(result.current.status).toBe('listening');
      expect(mockAudioManager.requestRecordingPermissions).not.toHaveBeenCalled();
      expect(mockRecorder.start).not.toHaveBeenCalled();
    });

    it('la cuerda simulada pasa por el análisis y se va afinando', async () => {
      const { result } = await renderHook(() => useTuner(STANDARD));
      await act(() => result.current.start());

      await advance(300);
      expect(result.current.reading?.stringIndex).toBe(0);
      const early = Math.abs(result.current.reading!.cents);
      expect(early).toBeGreaterThan(10);

      await advance(3000);
      expect(result.current.reading?.stringIndex).toBe(0);
      expect(Math.abs(result.current.reading!.cents)).toBeLessThan(early);
    });

    it('con una cuerda fijada simula esa cuerda', async () => {
      const { result } = await renderHook(() => useTuner(STANDARD));
      await act(() => result.current.start());
      await act(async () => result.current.selectString(4));
      await advance(500);
      expect(result.current.reading?.stringIndex).toBe(4);
      expect(result.current.reading?.frequency).toBeGreaterThan(220);
      expect(result.current.reading?.frequency).toBeLessThan(270);
    });

    it('stop deja de generar audio', async () => {
      const { result } = await renderHook(() => useTuner(STANDARD));
      await act(() => result.current.start());
      await advance(300);
      await act(() => result.current.stop());
      await advance(1000);
      expect(result.current.status).toBe('idle');
      expect(result.current.reading).toBeNull();
    });
  });
});
