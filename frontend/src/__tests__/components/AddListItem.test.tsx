import { render, screen, fireEvent } from '@testing-library/react-native';
import AddListItem from '@/components/AddListItem';
import { COLORS } from '@/constants/Theme';

jest.setTimeout(30000);

jest.mock('lucide-react-native', () => {
  const { jsx: mockJsx } = require('react/jsx-runtime');
  const { View } = require('react-native');
  const make = (name: string) => (props: any) => mockJsx(View, { testID: `icon-${name}`, ...props });
  return { Plus: make('Plus'), Check: make('Check') };
});

const item = { id: '1', title: 'Canción', description: 'desc', imageUrl: 'http://img/1.png', info: 'Canción\nArtista · Canción' };

describe('AddListItem', () => {
  it('muestra la información del item y su imagen', async () => {
    await render(<AddListItem item={item} onPress={jest.fn()} />);
    expect(screen.getByText('Canción\nArtista · Canción')).toBeTruthy();
    const image = screen.container.queryAll((n) => n.type === 'Image')[0];
    expect(image.props.source).toEqual({ uri: 'http://img/1.png' });
  });

  it('muestra el icono de añadir y llama a onPress al pulsarlo', async () => {
    const onPress = jest.fn();
    await render(<AddListItem item={item} onPress={onPress} />);
    expect(screen.getByTestId('icon-Plus')).toBeTruthy();
    expect(screen.queryByTestId('icon-Check')).toBeNull();
    await fireEvent.press(screen.getByTestId('icon-Plus'));
    expect(onPress).toHaveBeenCalledTimes(1);
  });

  it('con added muestra el tic y no permite pulsar', async () => {
    const onPress = jest.fn();
    await render(<AddListItem item={item} onPress={onPress} added />);
    expect(screen.getByTestId('icon-Check')).toBeTruthy();
    await fireEvent.press(screen.getByTestId('icon-Check'));
    expect(onPress).not.toHaveBeenCalled();
  });

  it('usa el color primario por defecto y el secundario si está invertido', async () => {
    const { rerender } = await render(<AddListItem item={item} onPress={jest.fn()} />);
    expect(screen.getByTestId('icon-Plus').props.color).toBe(COLORS.primary);
    await rerender(<AddListItem item={item} onPress={jest.fn()} inverted />);
    expect(screen.getByTestId('icon-Plus').props.color).toBe(COLORS.secondary);
  });
});
