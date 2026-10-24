import { render, screen, fireEvent } from '@testing-library/react-native';
import BackButton from '@/components/iconbuttons/BackButton';

jest.setTimeout(30000);

jest.mock('lucide-react-native', () => {
  const { jsx: mockJsx } = require('react/jsx-runtime');
  const { View } = require('react-native');
  return { ArrowLeft: (props: any) => mockJsx(View, { testID: 'icon-ArrowLeft', ...props }) };
});

describe('BackButton', () => {
  it('es un botón accesible "Volver" que llama a onPress', async () => {
    const onPress = jest.fn();
    await render(<BackButton onPress={onPress} />);
    await fireEvent.press(screen.getByRole('button', { name: 'Volver' }));
    expect(onPress).toHaveBeenCalledTimes(1);
  });

  it('usa tamaño 24 y color blanco por defecto', async () => {
    await render(<BackButton onPress={jest.fn()} />);
    expect(screen.getByTestId('icon-ArrowLeft').props).toEqual(expect.objectContaining({ size: 24, color: 'white' }));
  });

  it('acepta tamaño y color personalizados', async () => {
    await render(<BackButton onPress={jest.fn()} size={32} color="gold" />);
    expect(screen.getByTestId('icon-ArrowLeft').props).toEqual(expect.objectContaining({ size: 32, color: 'gold' }));
  });
});
