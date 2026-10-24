import { createRef } from 'react';
import { render, screen, fireEvent, act } from '@testing-library/react-native';
import MetronomePulse, { MetronomePulseHandle } from '@/components/metronome/MetronomePulse';

jest.setTimeout(30000);

// El mock global de reanimated 4 necesita también el de worklets (si no, carga el módulo nativo)
jest.mock('react-native-worklets', () => require('react-native-worklets/src/mock'));

jest.mock('lucide-react-native', () => {
  const { jsx: mockJsx } = require('react/jsx-runtime');
  const { View } = require('react-native');
  const make = (name: string) => (props: any) => mockJsx(View, { testID: `icon-${name}`, ...props });
  return { Play: make('Play'), Square: make('Square') };
});

describe('MetronomePulse', () => {
  it('parado muestra el botón de iniciar con el icono de play', async () => {
    await render(<MetronomePulse tempo={120} isPlaying={false} onToggle={jest.fn()} />);
    expect(screen.getByRole('button', { name: 'Iniciar metrónomo' })).toBeTruthy();
    expect(screen.getByTestId('icon-Play')).toBeTruthy();
    expect(screen.queryByTestId('icon-Square')).toBeNull();
  });

  it('sonando muestra el botón de parar con el icono de stop', async () => {
    await render(<MetronomePulse tempo={120} isPlaying onToggle={jest.fn()} />);
    expect(screen.getByRole('button', { name: 'Parar metrónomo' })).toBeTruthy();
    expect(screen.getByTestId('icon-Square')).toBeTruthy();
  });

  it('llama a onToggle al pulsar el círculo', async () => {
    const onToggle = jest.fn();
    await render(<MetronomePulse tempo={120} isPlaying={false} onToggle={onToggle} />);
    await fireEvent.press(screen.getByRole('button'));
    expect(onToggle).toHaveBeenCalledTimes(1);
  });

  it('expone pulse por ref y puede lanzar varias ondas seguidas sin errores', async () => {
    const ref = createRef<MetronomePulseHandle>();
    await render(<MetronomePulse ref={ref} tempo={60} isPlaying onToggle={jest.fn()} />);
    expect(typeof ref.current!.pulse).toBe('function');
    await act(async () => {
      for (let beat = 0; beat < 5; beat++) ref.current!.pulse(beat % 4);
    });
    expect(screen.getByRole('button', { name: 'Parar metrónomo' })).toBeTruthy();
  });
});
