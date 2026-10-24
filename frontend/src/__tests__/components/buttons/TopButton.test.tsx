import { StyleSheet } from 'react-native';
import { render, screen, fireEvent } from '@testing-library/react-native';
import TopButton from '@/components/buttons/TopButton';

jest.setTimeout(30000);

describe('TopButton', () => {
  it('muestra el nombre y llama a onPress al pulsarlo', async () => {
    const onPress = jest.fn();
    await render(<TopButton name="Canciones" isSelected={false} onPress={onPress} />);
    await fireEvent.press(screen.getByText('Canciones'));
    expect(onPress).toHaveBeenCalledTimes(1);
  });

  it('sin seleccionar el texto es dorado', async () => {
    await render(<TopButton name="Canciones" isSelected={false} onPress={jest.fn()} />);
    expect(StyleSheet.flatten(screen.getByText('Canciones').props.style).color).toBe('#C1853C');
  });

  it('seleccionado el texto es blanco', async () => {
    await render(<TopButton name="Canciones" isSelected onPress={jest.fn()} />);
    expect(StyleSheet.flatten(screen.getByText('Canciones').props.style).color).toBe('#FFFFFF');
  });
});
