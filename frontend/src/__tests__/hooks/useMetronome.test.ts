import { act, renderHook } from '@testing-library/react-native';
import { createAudioPlayer, setAudioModeAsync } from 'expo-audio';
import { BEATS_PER_BAR, useMetronome, useTapTempo } from '../../hooks/useMetronome';

jest.mock('expo-audio', () => ({
  createAudioPlayer: jest.fn(),
  setAudioModeAsync: jest.fn(),
}));

const mockedCreate = createAudioPlayer as jest.Mock;
const mockedSetMode = setAudioModeAsync as jest.Mock;

type FakePlayer = { file: any; volume: number; seekTo: jest.Mock; play: jest.Mock; remove: jest.Mock };
let players: FakePlayer[];

const makePlayer = (file: any): FakePlayer => {
  const player: FakePlayer = {
    file,
    volume: 0,
    seekTo: jest.fn().mockResolvedValue(undefined),
    play: jest.fn(),
    remove: jest.fn(),
  };
  players.push(player);
  return player;
};

describe('useMetronome', () => {
  beforeEach(() => {
    jest.useFakeTimers();
    jest.setSystemTime(new Date('2026-01-01T00:00:00Z'));
    jest.clearAllMocks();
    players = [];
    mockedCreate.mockImplementation(makePlayer);
    mockedSetMode.mockResolvedValue(undefined);
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('configura el modo de audio y carga los cuatro sonidos', async () => {
    const { result } = await renderHook(() => useMetronome(120, 'click'));

    expect(mockedSetMode).toHaveBeenCalledWith({ playsInSilentMode: true, interruptionMode: 'mixWithOthers' });
    expect(mockedCreate).toHaveBeenCalledTimes(4);
    // normal 0.8, acento 1 (por cada sonido)
    expect(players.map((p) => p.volume)).toEqual([0.8, 1, 0.8, 1]);
    expect(result.current.ready).toBe(true);
    expect(result.current.isPlaying).toBe(false);
  });

  it('avisa si no puede configurar el modo de audio', async () => {
    mockedSetMode.mockRejectedValue(new Error('modo'));
    await renderHook(() => useMetronome(120, 'click'));
    await act(async () => {});
    expect(console.warn).toHaveBeenCalledWith(
      '[useMetronome] No se pudo configurar el modo de audio:',
      expect.any(Error)
    );
  });

  it('si fallan los sonidos no queda listo pero el pulso visual funciona', async () => {
    mockedCreate.mockImplementation(() => {
      throw new Error('sin audio');
    });
    const onBeat = jest.fn();
    const { result } = await renderHook(() => useMetronome(60, 'click', onBeat));

    expect(result.current.ready).toBe(false);
    expect(console.error).toHaveBeenCalledWith('[useMetronome] Error cargando los sonidos:', expect.any(Error));

    await act(async () => result.current.start());
    expect(onBeat).toHaveBeenCalledWith(0);
  });

  it('start toca golpes al tempo indicado con acento en el primero del compás', async () => {
    const onBeat = jest.fn();
    const { result } = await renderHook(() => useMetronome(120, 'click', onBeat));
    const [clickNormal, clickAccent] = players;

    await act(async () => result.current.start());
    expect(result.current.isPlaying).toBe(true);
    expect(onBeat).toHaveBeenLastCalledWith(0);
    expect(clickAccent.seekTo).toHaveBeenCalledWith(0);
    await act(async () => {});
    expect(clickAccent.play).toHaveBeenCalledTimes(1);

    // 120 BPM = un golpe cada 500 ms
    await act(async () => {
      jest.advanceTimersByTime(500 * BEATS_PER_BAR);
    });
    expect(onBeat.mock.calls.map((c) => c[0])).toEqual([0, 1, 2, 3, 0]);
    expect(clickNormal.seekTo).toHaveBeenCalledTimes(3);
    expect(clickAccent.seekTo).toHaveBeenCalledTimes(2);
  });

  it('usa el sonido hihat cuando se elige', async () => {
    const { result } = await renderHook(() => useMetronome(120, 'hihat'));
    const hihatAccent = players[3];
    await act(async () => result.current.start());
    expect(hihatAccent.seekTo).toHaveBeenCalled();
    expect(players[1].seekTo).not.toHaveBeenCalled();
  });

  it('avisa si falla la reproducción de un golpe', async () => {
    const { result } = await renderHook(() => useMetronome(120, 'click'));
    players[1].seekTo.mockRejectedValue(new Error('play'));
    await act(async () => result.current.start());
    await act(async () => {});
    expect(console.warn).toHaveBeenCalledWith('[useMetronome] Error reproduciendo el golpe:', expect.any(Error));
  });

  it('start no reinicia si ya está sonando', async () => {
    const onBeat = jest.fn();
    const { result } = await renderHook(() => useMetronome(120, 'click', onBeat));
    await act(async () => result.current.start());
    await act(async () => result.current.start());
    expect(onBeat).toHaveBeenCalledTimes(1);
  });

  it('stop detiene los golpes', async () => {
    const onBeat = jest.fn();
    const { result } = await renderHook(() => useMetronome(120, 'click', onBeat));
    await act(async () => result.current.start());
    await act(async () => result.current.stop());
    expect(result.current.isPlaying).toBe(false);

    await act(async () => {
      jest.advanceTimersByTime(5000);
    });
    expect(onBeat).toHaveBeenCalledTimes(1);
  });

  it('stop sin estar sonando no falla', async () => {
    const { result } = await renderHook(() => useMetronome(120, 'click'));
    await act(async () => result.current.stop());
    expect(result.current.isPlaying).toBe(false);
  });

  it('toggle alterna entre sonar y parar', async () => {
    const { result } = await renderHook(() => useMetronome(120, 'click'));
    await act(async () => result.current.toggle());
    expect(result.current.isPlaying).toBe(true);
    await act(async () => result.current.toggle());
    expect(result.current.isPlaying).toBe(false);
  });

  it('un cambio de tempo en marcha se aplica desde el siguiente golpe', async () => {
    const onBeat = jest.fn();
    const { result, rerender } = await renderHook(
      ({ tempo }: { tempo: number }) => useMetronome(tempo, 'click', onBeat),
      { initialProps: { tempo: 60 } }
    );
    await act(async () => result.current.start());
    await rerender({ tempo: 120 });

    // El segundo golpe ya estaba programado a 1000 ms (60 BPM)
    await act(async () => {
      jest.advanceTimersByTime(1000);
    });
    expect(onBeat).toHaveBeenCalledTimes(2);
    // A partir de ahí, cada 500 ms
    await act(async () => {
      jest.advanceTimersByTime(500);
    });
    expect(onBeat).toHaveBeenCalledTimes(3);
  });

  it('al desmontar para el temporizador y libera los sonidos', async () => {
    const onBeat = jest.fn();
    const { result, unmount } = await renderHook(() => useMetronome(120, 'click', onBeat));
    await act(async () => result.current.start());
    await unmount();

    players.forEach((p) => expect(p.remove).toHaveBeenCalled());
    await act(async () => {
      jest.advanceTimersByTime(5000);
    });
    expect(onBeat).toHaveBeenCalledTimes(1);
  });
});

describe('useTapTempo', () => {
  beforeEach(() => {
    jest.useFakeTimers();
    jest.setSystemTime(new Date('2026-01-01T00:00:00Z'));
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('devuelve null con un solo toque', async () => {
    const { result } = await renderHook(() => useTapTempo());
    expect(result.current.tap()).toBeNull();
  });

  it('calcula el BPM con el promedio de los intervalos', async () => {
    const { result } = await renderHook(() => useTapTempo());
    result.current.tap();
    jest.advanceTimersByTime(500);
    expect(result.current.tap()).toBe(120);
    jest.advanceTimersByTime(1000);
    // (1500 ms / 2 intervalos) = 750 ms -> 80 BPM
    expect(result.current.tap()).toBe(80);
  });

  it('solo promedia los últimos intervalos', async () => {
    const { result } = await renderHook(() => useTapTempo());
    result.current.tap();
    jest.advanceTimersByTime(1500);
    result.current.tap();
    let bpm: number | null = null;
    for (let i = 0; i < 4; i++) {
      jest.advanceTimersByTime(500);
      bpm = result.current.tap();
    }
    // El intervalo de 1500 ms ya no cuenta
    expect(bpm).toBe(120);
  });

  it('empieza de nuevo si pasan más de 2 segundos entre toques', async () => {
    const { result } = await renderHook(() => useTapTempo());
    result.current.tap();
    jest.advanceTimersByTime(500);
    result.current.tap();
    jest.advanceTimersByTime(2500);
    expect(result.current.tap()).toBeNull();
  });
});
