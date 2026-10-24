import React from 'react';
import { Linking } from 'react-native';
import { render, screen, fireEvent } from '@testing-library/react-native';
import TunerScreen from '../../screens/TunerScreen';

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

const GUITAR = { id: '1', title: 'Estándar', instrumentId: '1', instrumentName: 'Guitarra', label: 'Guitarra (Estándar)', notes: ['E2', 'A2', 'D3', 'G3', 'B3', 'E4'] };
const DROP_D = { id: '2', title: 'Drop D', instrumentId: '1', instrumentName: 'Guitarra', label: 'Guitarra (Drop D)', notes: ['D2', 'A2', 'D3', 'G3', 'B3', 'E4'] };
const BASS = { id: '6', title: 'Estándar', instrumentId: '3', instrumentName: 'Bajo', label: 'Bajo (Estándar)', notes: ['E1', 'A1', 'D2', 'G2'] };

let mockTunes: any = { tunes: [BASS, GUITAR, DROP_D], defaultTuneId: '1', loading: false };
jest.mock('../../hooks/useTunes', () => ({ useTunes: () => mockTunes }));

let mockTuner: any;
const mockStart = jest.fn();
const mockStop = jest.fn();
const mockSelectString = jest.fn();
const mockUseTuner = jest.fn();
jest.mock('../../hooks/useTuner', () => ({
  useTuner: (notes: string[]) => {
    mockUseTuner(notes);
    return mockTuner;
  },
}));

jest.mock('../../components/ProfileHeader', () => {
  const { Text } = require('react-native');
  return ({ title }: any) => <Text>{title}</Text>;
});
jest.mock('../../components/tuner/TunerStringSelector', () => {
  const { Text, View } = require('react-native');
  return ({ notes, activeIndex, onSelect }: any) => (
    <View>
      {notes.map((note: string, i: number) => (
        <Text key={i} onPress={() => onSelect(i)}>{i === activeIndex ? `[${note}]` : note}</Text>
      ))}
    </View>
  );
});
jest.mock('../../components/tuner/TunerGauge', () => {
  const { Text } = require('react-native');
  return ({ cents, inTune, onPress, accessibilityLabel }: any) => (
    <Text onPress={onPress}>{`Medidor ${cents ?? 'sin lectura'} ${inTune ? 'afinado' : ''} ${accessibilityLabel}`}</Text>
  );
});
jest.mock('../../components/tuner/TuneWheel', () => {
  const { Text, View } = require('react-native');
  return ({ options, selectedIndex, onSelect }: any) => (
    <View>
      {options.map((o: any, i: number) => (
        <Text key={o.id} onPress={() => onSelect(i)}>{i === selectedIndex ? `> ${o.label}` : o.label}</Text>
      ))}
    </View>
  );
});

const tuner = (overrides: any = {}) => ({
  status: 'listening',
  isListening: true,
  simulated: false,
  reading: null,
  lockedString: null,
  start: mockStart,
  stop: mockStop,
  selectString: mockSelectString,
  ...overrides,
});

describe('TunerScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockFocusCleanup = undefined;
    mockTunes = { tunes: [BASS, GUITAR, DROP_D], defaultTuneId: '1', loading: false };
    mockTuner = tuner();
  });

  it('muestra la afinación por defecto con sus cuerdas y empieza a escuchar', async () => {
    await render(<TunerScreen />);
    expect(screen.getByText('Afinador')).toBeTruthy();
    expect(screen.getByText('> Guitarra (Estándar)')).toBeTruthy();
    expect(mockUseTuner).toHaveBeenLastCalledWith(GUITAR.notes);
    expect(screen.getByText('E4')).toBeTruthy();
    expect(mockStart).toHaveBeenCalled();
  });

  it('sin lectura muestra la pista del estado', async () => {
    await render(<TunerScreen />);
    expect(screen.getByText('--')).toBeTruthy();
    expect(screen.getByText('Toca una cuerda')).toBeTruthy();
    expect(screen.getByText('-- hz')).toBeTruthy();
  });

  it.each([
    ['idle', 'Toca el medidor para escuchar'],
    ['starting', 'Preparando el micrófono…'],
    ['error', 'No se pudo usar el micrófono'],
  ])('en estado %s indica "%s"', async (status, hint) => {
    mockTuner = tuner({ status, isListening: false });
    await render(<TunerScreen />);
    expect(screen.getByText(hint)).toBeTruthy();
  });

  it('con lectura dentro del margen muestra los cents, "Afinado", la frecuencia y resalta la cuerda', async () => {
    mockTuner = tuner({ reading: { frequency: 117.4, stringIndex: 1, cents: -3.4 } });
    await render(<TunerScreen />);
    expect(screen.getByText('-3')).toBeTruthy();
    expect(screen.getByText('Afinado')).toBeTruthy();
    expect(screen.getByText('117 hz')).toBeTruthy();
    expect(screen.getByText('[A2]')).toBeTruthy();
    expect(screen.getByText(/Medidor -3.4 afinado/)).toBeTruthy();
  });

  it('indica tensar o destensar fuera del margen, con signo', async () => {
    mockTuner = tuner({ reading: { frequency: 100, stringIndex: 1, cents: -18 } });
    const { rerender } = await render(<TunerScreen />);
    expect(screen.getByText('-18')).toBeTruthy();
    expect(screen.getByText('Tensar')).toBeTruthy();

    mockTuner = tuner({ reading: { frequency: 120, stringIndex: 1, cents: 22 } });
    await rerender(<TunerScreen />);
    expect(screen.getByText('+22')).toBeTruthy();
    expect(screen.getByText('Destensar')).toBeTruthy();
  });

  it('elegir otra afinación deja de escuchar y cambia las cuerdas', async () => {
    await render(<TunerScreen />);
    await fireEvent.press(screen.getByText('Guitarra (Drop D)'));
    expect(mockStop).toHaveBeenCalled();
    expect(screen.getByText('> Guitarra (Drop D)')).toBeTruthy();
    expect(mockUseTuner).toHaveBeenLastCalledWith(DROP_D.notes);
  });

  it('pulsar una cuerda la fija y, si no escuchaba, empieza a escuchar', async () => {
    mockTuner = tuner({ status: 'idle', isListening: false });
    await render(<TunerScreen />);
    mockStart.mockClear();
    await fireEvent.press(screen.getByText('D3'));
    expect(mockSelectString).toHaveBeenCalledWith(2);
    expect(mockStart).toHaveBeenCalled();
  });

  it('pulsar la cuerda fijada vuelve a la detección automática', async () => {
    mockTuner = tuner({ lockedString: 2 });
    await render(<TunerScreen />);
    mockStart.mockClear();
    await fireEvent.press(screen.getByText('[D3]'));
    expect(mockSelectString).toHaveBeenCalledWith(null);
    expect(mockStart).not.toHaveBeenCalled();
  });

  it('el medidor para si escucha y escucha si está parado', async () => {
    const { rerender } = await render(<TunerScreen />);
    await fireEvent.press(screen.getByText(/Medidor .* Dejar de escuchar/));
    expect(mockStop).toHaveBeenCalled();

    mockTuner = tuner({ status: 'idle', isListening: false });
    await rerender(<TunerScreen />);
    mockStart.mockClear();
    await fireEvent.press(screen.getByText(/Medidor .* Escuchar/));
    expect(mockStart).toHaveBeenCalled();
  });

  it('sin permiso de micrófono ofrece abrir los ajustes', async () => {
    const openSettings = jest.spyOn(Linking, 'openSettings').mockResolvedValue();
    mockTuner = tuner({ status: 'denied', isListening: false });
    await render(<TunerScreen />);
    expect(screen.getByText('Sin acceso al micrófono')).toBeTruthy();
    await fireEvent.press(screen.getByText('Abrir ajustes'));
    expect(openSettings).toHaveBeenCalled();
  });

  it('con el micrófono real no muestra el aviso de demostración', async () => {
    await render(<TunerScreen />);
    expect(screen.queryByText(/Modo demostración/)).toBeNull();
  });

  it('en Expo Go avisa de que el sonido es simulado', async () => {
    mockTuner = tuner({ simulated: true });
    await render(<TunerScreen />);
    expect(screen.getByText(/Modo demostración: en Expo Go no hay micrófono/)).toBeTruthy();
  });

  it('al salir de la pantalla deja de escuchar', async () => {
    await render(<TunerScreen />);
    expect(mockFocusCleanup).toBeDefined();
    mockFocusCleanup?.();
    expect(mockStop).toHaveBeenCalled();
  });

  it('sin afinación por defecto usa la primera', async () => {
    mockTunes = { tunes: [BASS, GUITAR], defaultTuneId: null, loading: false };
    await render(<TunerScreen />);
    expect(screen.getByText('> Bajo (Estándar)')).toBeTruthy();
  });

  it('mientras carga sin afinaciones no pinta el afinador', async () => {
    mockTunes = { tunes: [], defaultTuneId: null, loading: true };
    await render(<TunerScreen />);
    expect(screen.queryByText('-- hz')).toBeNull();
    expect(mockUseTuner).toHaveBeenLastCalledWith([]);
  });

  it('sin afinaciones avisa de que no hay ninguna', async () => {
    mockTunes = { tunes: [], defaultTuneId: null, loading: false };
    await render(<TunerScreen />);
    expect(screen.getByText('No hay afinaciones disponibles. Comprueba tu conexión.')).toBeTruthy();
  });
});
