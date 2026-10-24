import { render, screen, fireEvent } from '@testing-library/react-native';
import EditButton from '@/components/iconbuttons/EditButton';
import { COLORS } from '@/constants/Theme';

jest.setTimeout(30000);

jest.mock('lucide-react-native', () => {
  const { jsx: mockJsx } = require('react/jsx-runtime');
  const { View } = require('react-native');
  return { Pencil: (props: any) => mockJsx(View, { testID: 'icon-Pencil', ...props }) };
});

describe('EditButton', () => {
  it('es un botón "Editar" por defecto que llama a onPress', async () => {
    const onPress = jest.fn();
    await render(<EditButton onPress={onPress} />);
    await fireEvent.press(screen.getByRole('button', { name: 'Editar' }));
    expect(onPress).toHaveBeenCalledTimes(1);
  });

  it('usa los valores por defecto del icono', async () => {
    await render(<EditButton onPress={jest.fn()} />);
    expect(screen.getByTestId('icon-Pencil').props).toEqual(
      expect.objectContaining({ size: 20, color: COLORS.primary, strokeWidth: 1 })
    );
  });

  it('acepta etiqueta de accesibilidad y estilo personalizados', async () => {
    await render(<EditButton onPress={jest.fn()} accessibilityLabel="Renombrar lista" size={16} color="#fff" strokeWidth={2} />);
    expect(screen.getByRole('button', { name: 'Renombrar lista' })).toBeTruthy();
    expect(screen.getByTestId('icon-Pencil').props).toEqual(expect.objectContaining({ size: 16, color: '#fff', strokeWidth: 2 }));
  });
});
