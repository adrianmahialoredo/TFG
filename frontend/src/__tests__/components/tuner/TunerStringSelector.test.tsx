import { StyleSheet } from 'react-native';
import { render, screen, fireEvent } from '@testing-library/react-native';
import TunerStringSelector from '@/components/tuner/TunerStringSelector';
import { COLORS } from '@/constants/Theme';

jest.setTimeout(30000);

const STANDARD = ['E2', 'A2', 'D3', 'G3', 'B3', 'E4'];
const buttonStyle = (note: string) =>
  StyleSheet.flatten(screen.getByRole('button', { name: new RegExp(`: ${note}$`) }).props.style);

describe('TunerStringSelector', () => {
  it('muestra un botón por cuerda en orden', async () => {
    await render(<TunerStringSelector notes={STANDARD} selectedIndex={0} onSelect={jest.fn()} />);
    expect(screen.getAllByRole('button')).toHaveLength(6);
    expect(screen.getByRole('button', { name: 'Cuerda 1: E2' })).toBeTruthy();
    expect(screen.getByRole('button', { name: 'Cuerda 6: E4' })).toBeTruthy();
  });

  it('la cuerda seleccionada va rellena en dorado y el resto solo con borde', async () => {
    await render(<TunerStringSelector notes={STANDARD} selectedIndex={1} onSelect={jest.fn()} />);
    expect(buttonStyle('A2').backgroundColor).toBe(COLORS.primary);
    expect(buttonStyle('E2').backgroundColor).toBe('transparent');
    expect(StyleSheet.flatten(screen.getByText('A2').props.style).color).toBe(COLORS.secondary);
    expect(StyleSheet.flatten(screen.getByText('E2').props.style).color).toBe(COLORS.primary);
  });

  it('la cuerda seleccionada se marca como seleccionada y con borde más grueso', async () => {
    await render(<TunerStringSelector notes={STANDARD} selectedIndex={2} onSelect={jest.fn()} />);
    expect(screen.getByRole('button', { name: 'Cuerda 3: D3' }).props.accessibilityState.selected).toBe(true);
    expect(buttonStyle('D3').borderWidth).toBe(2);
    expect(buttonStyle('G3').borderWidth).toBe(1);
  });

  it('avisa del índice de la cuerda pulsada', async () => {
    const onSelect = jest.fn();
    await render(<TunerStringSelector notes={STANDARD} selectedIndex={0} onSelect={onSelect} />);
    await fireEvent.press(screen.getByText('G3'));
    expect(onSelect).toHaveBeenCalledWith(3);
  });

  it('admite notas repetidas sin claves duplicadas', async () => {
    await render(<TunerStringSelector notes={['E2', 'E2']} selectedIndex={0} onSelect={jest.fn()} />);
    expect(screen.getAllByText('E2')).toHaveLength(2);
  });
});
