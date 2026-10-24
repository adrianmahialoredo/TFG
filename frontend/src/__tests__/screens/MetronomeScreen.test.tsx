import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react-native';
import MetronomeScreen from '../../screens/MetronomeScreen';

// El primer render de cada suite carga muchos módulos y puede superar los 5 s por defecto
jest.setTimeout(30000);

let mockFocusCleanup: (() => void) | undefined;
jest.mock('@react-navigation/native', () => {
  const ReactActual = jest.requireActual('react');
  return {
    ...jest.requireActual('@react-navigation/native'),
    useFocusEffect: (cb: () => (() => void) | void) => {
      ReactActual.useEffect(() => {
        const cleanup = cb();
        mockFocusCleanup = cleanup || undefined;
        return cleanup;
      }, [cb]);
    },
  };
});

let mockIsPlaying = false;
const mockToggle = jest.fn();
const mockStop = jest.fn();
const mockTap = jest.fn();
const mockUseMetronome = jest.fn();
let mockOnBeat: ((beat: number) => void) | undefined;
jest.mock('../../hooks/useMetronome', () => ({
  BEATS_PER_BAR: 4,
  useMetronome: (tempo: number, sound: string, onBeat: (beat: number) => void) => {
    mockUseMetronome(tempo, sound);
    mockOnBeat = onBeat;
    return { isPlaying: mockIsPlaying, toggle: mockToggle, stop: mockStop };
  },
  useTapTempo: () => ({ tap: mockTap }),
}));

jest.mock('../../components/ProfileHeader', () => {
  const { Text } = require('react-native');
  return ({ title }: any) => <Text>{title}</Text>;
});

const mockPulse = jest.fn();
const mockSetBeat = jest.fn();
jest.mock('../../components/metronome/MetronomeSlider', () => {
  const { Text } = require('react-native');
  return {
    __esModule: true,
    DEFAULT_TEMPO: 120,
    clampTempo: (v: number) => Math.min(300, Math.max(30, Math.round(v))),
    default: ({ tempo, onTempoChange }: any) => (
      <Text onPress={() => onTempoChange(tempo + 1)}>{`Tempo ${tempo}`}</Text>
    ),
  };
});
jest.mock('../../components/metronome/MetronomePulse', () => {
  const ReactActual = require('react');
  const { Text } = require('react-native');
  return {
    __esModule: true,
    default: ReactActual.forwardRef(({ onToggle }: any, ref: any) => {
      ReactActual.useImperativeHandle(ref, () => ({ pulse: mockPulse }));
      return <Text onPress={onToggle}>Pulso</Text>;
    }),
  };
});
jest.mock('../../components/metronome/MetronomeBeatIndicator', () => {
  const ReactActual = require('react');
  return {
    __esModule: true,
    default: ReactActual.forwardRef((_: any, ref: any) => {
      ReactActual.useImperativeHandle(ref, () => ({ setBeat: mockSetBeat }));
      return null;
    }),
  };
});

describe('MetronomeScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockIsPlaying = false;
    mockFocusCleanup = undefined;
  });

  it('arranca con el tempo por defecto y el sonido click', async () => {
    await render(<MetronomeScreen />);
    expect(screen.getByText('Metrónomo')).toBeTruthy();
    expect(screen.getByText('Tempo 120')).toBeTruthy();
    expect(screen.getByText('Toca para empezar')).toBeTruthy();
    expect(mockUseMetronome).toHaveBeenLastCalledWith(120, 'click');
  });

  it('cambia el tempo desde el slider', async () => {
    await render(<MetronomeScreen />);
    await fireEvent.press(screen.getByText('Tempo 120'));
    expect(screen.getByText('Tempo 121')).toBeTruthy();
    expect(mockUseMetronome).toHaveBeenLastCalledWith(121, 'click');
  });

  it('pulsar el círculo arranca el metrónomo', async () => {
    await render(<MetronomeScreen />);
    await fireEvent.press(screen.getByText('Pulso'));
    expect(mockToggle).toHaveBeenCalled();
    expect(mockSetBeat).not.toHaveBeenCalled();
  });

  it('si está sonando, al parar reinicia el indicador del compás', async () => {
    mockIsPlaying = true;
    await render(<MetronomeScreen />);
    expect(screen.getByText('Toca para parar')).toBeTruthy();
    await fireEvent.press(screen.getByText('Pulso'));
    expect(mockSetBeat).toHaveBeenCalledWith(null);
    expect(mockToggle).toHaveBeenCalled();
  });

  it('cada golpe actualiza la onda y el indicador', async () => {
    await render(<MetronomeScreen />);
    mockOnBeat?.(2);
    expect(mockPulse).toHaveBeenCalledWith(2);
    expect(mockSetBeat).toHaveBeenCalledWith(2);
  });

  it('el tap tempo fija el tempo limitado al rango válido', async () => {
    mockTap.mockReturnValue(500);
    await render(<MetronomeScreen />);
    await fireEvent.press(screen.getByText('TAP TEMPO'));
    expect(screen.getByText('Tempo 300')).toBeTruthy();
  });

  it('el tap tempo no cambia nada si aún no hay bpm', async () => {
    mockTap.mockReturnValue(null);
    await render(<MetronomeScreen />);
    await fireEvent.press(screen.getByText('TAP TEMPO'));
    expect(screen.getByText('Tempo 120')).toBeTruthy();
  });

  it('alterna el sonido entre click y hi-hat', async () => {
    await render(<MetronomeScreen />);
    await fireEvent.press(screen.getByText('Hi-Hat'));
    expect(screen.getByText('Hi-Hat ✓')).toBeTruthy();
    expect(mockUseMetronome).toHaveBeenLastCalledWith(120, 'hihat');
    await fireEvent.press(screen.getByText('Hi-Hat ✓'));
    expect(mockUseMetronome).toHaveBeenLastCalledWith(120, 'click');
  });

  it('al salir de la pantalla para el metrónomo', async () => {
    await render(<MetronomeScreen />);
    expect(mockFocusCleanup).toBeDefined();
    mockFocusCleanup?.();
    expect(mockStop).toHaveBeenCalled();
    expect(mockSetBeat).toHaveBeenCalledWith(null);
  });
});
