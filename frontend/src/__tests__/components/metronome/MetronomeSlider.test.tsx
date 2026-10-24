import { render, screen, fireEvent, act } from '@testing-library/react-native';
import MetronomeSlider, { clampTempo, MIN_TEMPO, MAX_TEMPO, DEFAULT_TEMPO } from '@/components/metronome/MetronomeSlider';

jest.setTimeout(30000);

// Sustituto sencillo del slider nativo: expone sus props en una View
jest.mock('@react-native-community/slider', () => {
  const { jsx: mockJsx } = require('react/jsx-runtime');
  const { View } = require('react-native');
  return { __esModule: true, default: (props: any) => mockJsx(View, { testID: 'slider', ...props }) };
});

const slider = () => screen.getByTestId('slider');

describe('clampTempo', () => {
  it('redondea y limita el tempo al rango permitido', () => {
    expect(clampTempo(100.4)).toBe(100);
    expect(clampTempo(100.6)).toBe(101);
    expect(clampTempo(5)).toBe(MIN_TEMPO);
    expect(clampTempo(999)).toBe(MAX_TEMPO);
  });

  it('las constantes tienen los valores esperados', () => {
    expect([MIN_TEMPO, MAX_TEMPO, DEFAULT_TEMPO]).toEqual([30, 300, 120]);
  });
});

describe('MetronomeSlider', () => {
  it('muestra el tempo actual, el rango y los botones de ajuste', async () => {
    await render(<MetronomeSlider tempo={120} onTempoChange={jest.fn()} />);
    expect(screen.getByText('120 BPM')).toBeTruthy();
    expect(screen.getByText('Tempo')).toBeTruthy();
    expect(screen.getByText('BPM 30-300')).toBeTruthy();
    ['-10', '-1', '+1', '+10'].forEach((t) => expect(screen.getByText(t)).toBeTruthy());
    expect(slider().props).toEqual(expect.objectContaining({ minimumValue: 30, maximumValue: 300, value: 120, step: 1 }));
  });

  it.each([
    ['-10', 110],
    ['-1', 119],
    ['+1', 121],
    ['+10', 130],
  ])('el botón %s cambia el tempo a %i', async (label, expected) => {
    const onTempoChange = jest.fn();
    await render(<MetronomeSlider tempo={120} onTempoChange={onTempoChange} />);
    await fireEvent.press(screen.getByText(label));
    expect(onTempoChange).toHaveBeenCalledWith(expected);
  });

  it('los botones no salen del rango permitido', async () => {
    const onTempoChange = jest.fn();
    const { rerender } = await render(<MetronomeSlider tempo={35} onTempoChange={onTempoChange} />);
    await fireEvent.press(screen.getByText('-10'));
    expect(onTempoChange).toHaveBeenLastCalledWith(MIN_TEMPO);
    await rerender(<MetronomeSlider tempo={295} onTempoChange={onTempoChange} />);
    await fireEvent.press(screen.getByText('+10'));
    expect(onTempoChange).toHaveBeenLastCalledWith(MAX_TEMPO);
  });

  it('mover el slider notifica el tempo redondeado', async () => {
    const onTempoChange = jest.fn();
    await render(<MetronomeSlider tempo={120} onTempoChange={onTempoChange} />);
    await act(async () => slider().props.onValueChange(150.7));
    expect(onTempoChange).toHaveBeenCalledWith(151);
  });

  it('el slider sigue al tempo cuando no se está arrastrando', async () => {
    const { rerender } = await render(<MetronomeSlider tempo={120} onTempoChange={jest.fn()} />);
    await rerender(<MetronomeSlider tempo={140} onTempoChange={jest.fn()} />);
    expect(slider().props.value).toBe(140);
  });

  it('mientras se arrastra no se le vuelve a pasar el valor y al soltar se fija', async () => {
    const onTempoChange = jest.fn();
    const { rerender } = await render(<MetronomeSlider tempo={120} onTempoChange={onTempoChange} />);
    await act(async () => slider().props.onSlidingStart());
    await rerender(<MetronomeSlider tempo={160} onTempoChange={onTempoChange} />);
    expect(slider().props.value).toBe(120);
    expect(screen.getByText('160 BPM')).toBeTruthy();

    await act(async () => slider().props.onSlidingComplete(170.2));
    expect(onTempoChange).toHaveBeenLastCalledWith(170);
    expect(slider().props.value).toBe(170);

    // Ya sin arrastrar, vuelve a seguir al tempo
    await rerender(<MetronomeSlider tempo={90} onTempoChange={onTempoChange} />);
    expect(slider().props.value).toBe(90);
  });
});
