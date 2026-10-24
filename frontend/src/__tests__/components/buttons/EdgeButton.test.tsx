import { StyleSheet } from 'react-native';
import { render, screen, fireEvent } from '@testing-library/react-native';
import EdgeButton from '@/components/buttons/EdgeButton';

jest.setTimeout(30000);

const props = { name: 'Lección 1', isTopEdge: true, isBottomEdge: false };

describe('EdgeButton', () => {
  it('muestra el nombre y llama a onPress al pulsarlo', async () => {
    const onPress = jest.fn();
    await render(<EdgeButton {...props} isSelected={false} onPress={onPress} />);
    await fireEvent.press(screen.getByText('Lección 1'));
    expect(onPress).toHaveBeenCalledTimes(1);
  });

  it('sin seleccionar usa fondo negro y texto dorado', async () => {
    await render(<EdgeButton {...props} isSelected={false} onPress={jest.fn()} />);
    const style = StyleSheet.flatten(screen.getByText('Lección 1').props.style);
    expect(style).toEqual(expect.objectContaining({ backgroundColor: '#000000', color: '#C1853C' }));
  });

  it('seleccionado usa fondo dorado y texto blanco', async () => {
    await render(<EdgeButton {...props} isSelected onPress={jest.fn()} />);
    const style = StyleSheet.flatten(screen.getByText('Lección 1').props.style);
    expect(style).toEqual(expect.objectContaining({ backgroundColor: '#C1853C', color: '#FFFFFF' }));
  });
});
