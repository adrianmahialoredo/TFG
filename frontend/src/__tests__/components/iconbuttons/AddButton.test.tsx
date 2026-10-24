import { StyleSheet } from 'react-native';
import { render, screen } from '@testing-library/react-native';
import AddButton from '@/components/iconbuttons/AddButton';

jest.setTimeout(30000);

jest.mock('lucide-react-native', () => {
  const { jsx: mockJsx } = require('react/jsx-runtime');
  const { View } = require('react-native');
  const make = (name: string) => (props: any) => mockJsx(View, { testID: `icon-${name}`, ...props });
  return { Plus: make('Plus'), Check: make('Check') };
});

const box = () =>
  screen.container.queryAll((n) => n.type === 'View' && StyleSheet.flatten(n.props.style)?.borderWidth !== undefined)[0];

describe('AddButton', () => {
  it('muestra un más por defecto con el tamaño y grosor por defecto', async () => {
    await render(<AddButton color="red" />);
    const icon = screen.getByTestId('icon-Plus');
    expect(icon.props).toEqual(expect.objectContaining({ size: 14, color: 'red', strokeWidth: 1.8 }));
    expect(StyleSheet.flatten(box().props.style)).toEqual(
      expect.objectContaining({ width: 24, height: 24, borderWidth: 1.8, borderColor: 'red', borderRadius: 6 })
    );
  });

  it('muestra un tic cuando está marcado', async () => {
    await render(<AddButton color="blue" checked />);
    expect(screen.getByTestId('icon-Check')).toBeTruthy();
    expect(screen.queryByTestId('icon-Plus')).toBeNull();
  });

  it('escala el icono y el borde con el tamaño', async () => {
    await render(<AddButton color="blue" size={50} strokeWidth={2} />);
    expect(screen.getByTestId('icon-Plus').props.size).toBe(29);
    expect(StyleSheet.flatten(box().props.style)).toEqual(
      expect.objectContaining({ width: 50, height: 50, borderWidth: 2, borderRadius: 13 })
    );
  });
});
