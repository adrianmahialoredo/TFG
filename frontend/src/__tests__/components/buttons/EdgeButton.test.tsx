import { StyleSheet } from 'react-native';
import { render, screen, fireEvent } from '@testing-library/react-native';
import EdgeButton, { EDGE_RADIUS } from '@/components/buttons/EdgeButton';

jest.setTimeout(30000);

const props = { name: 'Lección 1', isTopEdge: true, isBottomEdge: false };

/** Estilo del botón: la vista pulsable (la que recibe los toques) que contiene el texto. */
const buttonStyle = () => {
  const button = screen.container.queryAll((n) => n.type === 'View' && !!n.props.onResponderRelease)[0];
  return StyleSheet.flatten(button.props.style);
};

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

  it('sin seleccionar tiene fondo negro y borde dorado', async () => {
    await render(<EdgeButton {...props} isSelected={false} onPress={jest.fn()} />);
    expect(buttonStyle()).toEqual(expect.objectContaining({ backgroundColor: '#000000', borderColor: '#C1853C', borderWidth: 1 }));
  });

  it('seleccionado usa fondo dorado y texto blanco', async () => {
    await render(<EdgeButton {...props} isSelected onPress={jest.fn()} />);
    const style = StyleSheet.flatten(screen.getByText('Lección 1').props.style);
    expect(style).toEqual(expect.objectContaining({ backgroundColor: '#C1853C', color: '#FFFFFF' }));
  });

  it('el primero solo redondea la esquina superior derecha', async () => {
    await render(<EdgeButton {...props} isSelected={false} onPress={jest.fn()} />);
    const style = buttonStyle();
    expect(style.borderTopRightRadius).toBe(EDGE_RADIUS);
    expect(style.borderBottomRightRadius).toBeUndefined();
    expect(style.borderTopLeftRadius).toBeUndefined();
    expect(style.borderBottomLeftRadius).toBeUndefined();
  });

  it('el último solo redondea la esquina inferior derecha y no repite el borde superior', async () => {
    await render(<EdgeButton {...props} isTopEdge={false} isBottomEdge isSelected={false} onPress={jest.fn()} />);
    const style = buttonStyle();
    expect(style.borderBottomRightRadius).toBe(EDGE_RADIUS);
    expect(style.borderTopRightRadius).toBeUndefined();
    expect(style.borderTopWidth).toBe(0);
  });
});
