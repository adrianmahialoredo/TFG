import { StyleSheet } from 'react-native';
import { render, screen, fireEvent } from '@testing-library/react-native';
import MetronomeActionButton from '@/components/metronome/MetronomeActionButton';
import { COLORS } from '@/constants/Theme';

jest.setTimeout(30000);

const textStyle = (label: string) => StyleSheet.flatten(screen.getByText(label).props.style);

describe('MetronomeActionButton', () => {
  it('muestra la etiqueta y llama a onPress', async () => {
    const onPress = jest.fn();
    await render(<MetronomeActionButton label="Hi-Hat" onPress={onPress} />);
    await fireEvent.press(screen.getByText('Hi-Hat'));
    expect(onPress).toHaveBeenCalledTimes(1);
  });

  it('sin seleccionar usa texto dorado de tamaño normal', async () => {
    await render(<MetronomeActionButton label="Hi-Hat" onPress={jest.fn()} />);
    expect(textStyle('Hi-Hat')).toEqual(expect.objectContaining({ color: COLORS.primary, fontSize: 18, fontWeight: '700' }));
  });

  it('seleccionado usa texto oscuro sobre fondo dorado', async () => {
    await render(<MetronomeActionButton label="Hi-Hat" onPress={jest.fn()} selected />);
    expect(textStyle('Hi-Hat').color).toBe(COLORS.background);
  });

  it('en grande usa texto de 30', async () => {
    await render(<MetronomeActionButton label="TAP TEMPO" onPress={jest.fn()} large />);
    expect(textStyle('TAP TEMPO')).toEqual(expect.objectContaining({ fontSize: 30, fontWeight: '400' }));
  });
});
