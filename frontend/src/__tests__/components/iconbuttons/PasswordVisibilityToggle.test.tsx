import { render, screen, fireEvent } from '@testing-library/react-native';
import PasswordVisibilityToggle from '@/components/iconbuttons/PasswordVisibilityToggle';
import { COLORS } from '@/constants/Theme';

jest.setTimeout(30000);

jest.mock('lucide-react-native', () => {
  const { jsx: mockJsx } = require('react/jsx-runtime');
  const { View } = require('react-native');
  const make = (name: string) => (props: any) => mockJsx(View, { testID: `icon-${name}`, ...props });
  return { Eye: make('Eye'), EyeOff: make('EyeOff') };
});

describe('PasswordVisibilityToggle', () => {
  it('con la contraseña oculta muestra el ojo y la etiqueta "Mostrar contraseña"', async () => {
    await render(<PasswordVisibilityToggle visible={false} onToggle={jest.fn()} />);
    expect(screen.getByRole('button', { name: 'Mostrar contraseña' })).toBeTruthy();
    expect(screen.getByTestId('icon-Eye').props).toEqual(expect.objectContaining({ size: 20, color: COLORS.secondary }));
  });

  it('con la contraseña visible muestra el ojo tachado y "Ocultar contraseña"', async () => {
    await render(<PasswordVisibilityToggle visible onToggle={jest.fn()} />);
    expect(screen.getByRole('button', { name: 'Ocultar contraseña' })).toBeTruthy();
    expect(screen.getByTestId('icon-EyeOff')).toBeTruthy();
  });

  it('llama a onToggle al pulsar', async () => {
    const onToggle = jest.fn();
    await render(<PasswordVisibilityToggle visible={false} onToggle={onToggle} />);
    await fireEvent.press(screen.getByRole('button'));
    expect(onToggle).toHaveBeenCalledTimes(1);
  });

  it('desactivado no llama a onToggle', async () => {
    const onToggle = jest.fn();
    await render(<PasswordVisibilityToggle visible={false} onToggle={onToggle} disabled />);
    await fireEvent.press(screen.getByRole('button'));
    expect(onToggle).not.toHaveBeenCalled();
    expect(screen.getByRole('button')).toBeDisabled();
  });
});
