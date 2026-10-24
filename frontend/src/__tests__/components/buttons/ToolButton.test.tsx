import { StyleSheet, Text } from 'react-native';
import { render, screen, fireEvent } from '@testing-library/react-native';
import { ToolButton } from '@/components/buttons/ToolButton';

jest.setTimeout(30000);

describe('ToolButton', () => {
  it('muestra el icono y la etiqueta', async () => {
    await render(<ToolButton icon={<Text>ICONO</Text>} label="Metrónomo" />);
    expect(screen.getByText('ICONO')).toBeTruthy();
    expect(screen.getByText('Metrónomo')).toBeTruthy();
  });

  it('llama a onPress al pulsar el botón', async () => {
    const onPress = jest.fn();
    await render(<ToolButton icon={<Text>ICONO</Text>} label="Afinador" onPress={onPress} />);
    await fireEvent.press(screen.getByText('ICONO'));
    expect(onPress).toHaveBeenCalledTimes(1);
  });

  it('aplica los márgenes por defecto y los personalizados', async () => {
    const { rerender } = await render(<ToolButton icon={<Text>I</Text>} label="L" />);
    expect(StyleSheet.flatten(screen.getByText('L').props.style).marginRight).toBe(10);
    await rerender(<ToolButton icon={<Text>I</Text>} label="L" labelMarginRight={3} buttonMarginRight={0} />);
    expect(StyleSheet.flatten(screen.getByText('L').props.style).marginRight).toBe(3);
  });
});
