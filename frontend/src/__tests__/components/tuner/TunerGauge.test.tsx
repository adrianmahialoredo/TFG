import { processColor, StyleSheet } from 'react-native';
import { render, screen, fireEvent } from '@testing-library/react-native';
import TunerGauge, { centsToAngle, IN_TUNE_COLOR } from '@/components/tuner/TunerGauge';
import { COLORS } from '@/constants/Theme';

jest.setTimeout(30000);

// El mock global de reanimated 4 necesita también el de worklets (si no, carga el módulo nativo)
jest.mock('react-native-worklets', () => require('react-native-worklets/src/mock'));

/** react-native-svg pasa el color al nativo ya procesado: { type: 0, payload: <color numérico> }. */
const needleFill = () => screen.getByTestId('tuner-needle-shape').props.fill.payload;
const native = (color: string) => processColor(color);

describe('centsToAngle', () => {
  it('centrada sin lectura o afinada', () => {
    expect(centsToAngle(null)).toBe(0);
    expect(centsToAngle(NaN)).toBe(0);
    expect(centsToAngle(0)).toBe(0);
  });

  it('a la izquierda si está baja y a la derecha si está alta, proporcional a los cents', () => {
    expect(centsToAngle(-25)).toBe(-45);
    expect(centsToAngle(10)).toBe(18);
  });

  it('se queda en el tope a partir de ±50 cents', () => {
    expect(centsToAngle(-80)).toBe(-90);
    expect(centsToAngle(300)).toBe(90);
  });
});

describe('TunerGauge', () => {
  it('es un botón con su etiqueta y avisa al pulsarlo', async () => {
    const onPress = jest.fn();
    await render(<TunerGauge cents={null} inTune={false} onPress={onPress} accessibilityLabel="Escuchar" />);
    await fireEvent.press(screen.getByRole('button', { name: 'Escuchar' }));
    expect(onPress).toHaveBeenCalledTimes(1);
  });

  it('sin lectura la aguja está atenuada y dorada', async () => {
    await render(<TunerGauge cents={null} inTune={false} onPress={jest.fn()} accessibilityLabel="Escuchar" />);
    expect(StyleSheet.flatten(screen.getByTestId('tuner-needle').props.style).opacity).toBe(0.5);
    expect(needleFill()).toBe(native(COLORS.primary));
  });

  it('desafinada la aguja es dorada y opaca', async () => {
    await render(<TunerGauge cents={-20} inTune={false} onPress={jest.fn()} accessibilityLabel="Escuchar" />);
    expect(StyleSheet.flatten(screen.getByTestId('tuner-needle').props.style).opacity).toBe(1);
    expect(needleFill()).toBe(native(COLORS.primary));
  });

  it('afinada la aguja se pone verde', async () => {
    await render(<TunerGauge cents={2} inTune onPress={jest.fn()} accessibilityLabel="Escuchar" />);
    expect(needleFill()).toBe(native(IN_TUNE_COLOR));
  });

  it('se re-renderiza con otra desviación sin errores', async () => {
    const { rerender } = await render(
      <TunerGauge cents={-40} inTune={false} onPress={jest.fn()} accessibilityLabel="Escuchar" />
    );
    await rerender(<TunerGauge cents={40} inTune={false} onPress={jest.fn()} accessibilityLabel="Escuchar" />);
    expect(screen.getByTestId('tuner-needle')).toBeTruthy();
  });
});
