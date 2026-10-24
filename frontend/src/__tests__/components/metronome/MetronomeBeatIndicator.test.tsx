import { createRef } from 'react';
import { StyleSheet } from 'react-native';
import { render, screen, act } from '@testing-library/react-native';
import MetronomeBeatIndicator, { MetronomeBeatIndicatorHandle } from '@/components/metronome/MetronomeBeatIndicator';
import { COLORS } from '@/constants/Theme';

jest.setTimeout(30000);

// useMetronome importa expo-audio (nativo); aquí solo hace falta la constante
jest.mock('@/hooks/useMetronome', () => ({ BEATS_PER_BAR: 4 }));

/** Puntos del compás: las Views con borderRadius (el contenedor no lo tiene). */
const dots = () =>
  screen.container
    .queryAll((n) => n.type === 'View' && StyleSheet.flatten(n.props.style)?.borderRadius !== undefined)
    .map((n) => StyleSheet.flatten(n.props.style));

describe('MetronomeBeatIndicator', () => {
  it('muestra 4 puntos apagados al principio', async () => {
    await render(<MetronomeBeatIndicator />);
    const all = dots();
    expect(all).toHaveLength(4);
    all.forEach((d) => expect(d).toEqual(expect.objectContaining({ width: 10, opacity: 0.25, backgroundColor: COLORS.secondary })));
  });

  it('el acento (golpe 0) crece más que el resto', async () => {
    const ref = createRef<MetronomeBeatIndicatorHandle>();
    await render(<MetronomeBeatIndicator ref={ref} />);
    await act(async () => ref.current!.setBeat(0));
    const all = dots();
    expect(all[0]).toEqual(expect.objectContaining({ width: 18, opacity: 1, backgroundColor: COLORS.primary }));
    expect(all[1].width).toBe(10);
  });

  it('un golpe normal se ilumina con tamaño intermedio', async () => {
    const ref = createRef<MetronomeBeatIndicatorHandle>();
    await render(<MetronomeBeatIndicator ref={ref} />);
    await act(async () => ref.current!.setBeat(2));
    const all = dots();
    expect(all[2]).toEqual(expect.objectContaining({ width: 14, opacity: 1 }));
    expect(all[0].width).toBe(10);
  });

  it('con null se apagan todos', async () => {
    const ref = createRef<MetronomeBeatIndicatorHandle>();
    await render(<MetronomeBeatIndicator ref={ref} />);
    await act(async () => ref.current!.setBeat(1));
    await act(async () => ref.current!.setBeat(null));
    dots().forEach((d) => expect(d.opacity).toBe(0.25));
  });
});
