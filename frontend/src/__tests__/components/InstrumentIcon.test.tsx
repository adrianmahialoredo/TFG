import { render, screen } from '@testing-library/react-native';
import InstrumentIcon from '@/components/InstrumentIcon';

jest.setTimeout(30000);

jest.mock('lucide-react-native', () => {
  const { jsx: mockJsx } = require('react/jsx-runtime');
  const { View } = require('react-native');
  const make = (name: string) => (props: any) => mockJsx(View, { testID: `icon-${name}`, ...props });
  return { Guitar: make('Guitar'), Keyboard: make('Keyboard'), Drum: make('Drum'), Music: make('Music') };
});

describe('InstrumentIcon', () => {
  it.each([
    ['Guitarra', 'Guitar'],
    ['Bajo eléctrico', 'Guitar'],
    ['UKELELE', 'Guitar'],
    ['Piano', 'Keyboard'],
    ['Batería Rock', 'Drum'],
    ['bateria', 'Drum'],
    ['Violín', 'Music'],
    [undefined, 'Music'],
  ])('para "%s" muestra el icono %s', async (name, icon) => {
    await render(<InstrumentIcon name={name} size={24} color="red" />);
    expect(screen.getByTestId(`icon-${icon}`)).toBeTruthy();
  });

  it('pasa tamaño, color y grosor al icono (grosor 1 por defecto)', async () => {
    await render(<InstrumentIcon name="Piano" size={30} color="#fff" />);
    const icon = screen.getByTestId('icon-Keyboard');
    expect(icon.props).toEqual(expect.objectContaining({ size: 30, color: '#fff', strokeWidth: 1 }));
  });
});
