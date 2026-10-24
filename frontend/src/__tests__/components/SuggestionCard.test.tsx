import { render, screen, fireEvent } from '@testing-library/react-native';
import SuggestionCard from '@/components/SuggestionCard';

jest.setTimeout(30000);

jest.mock('lucide-react-native', () => {
  const { jsx: mockJsx } = require('react/jsx-runtime');
  const { View } = require('react-native');
  const make = (name: string) => (props: any) => mockJsx(View, { testID: `icon-${name}`, ...props });
  return {
    ChevronRight: make('ChevronRight'),
    Guitar: make('Guitar'),
    Keyboard: make('Keyboard'),
    Drum: make('Drum'),
    Music: make('Music'),
  };
});

const images = () => screen.container.queryAll((n) => n.type === 'Image');

describe('SuggestionCard', () => {
  it('muestra la cabecera, el título y la imagen', async () => {
    await render(
      <SuggestionCard label="Prueba esta combinación" title="Guitarra Rock" imageUrl="http://img/x.png" onPress={jest.fn()} />
    );
    expect(screen.getByText('Prueba esta combinación')).toBeTruthy();
    expect(screen.getByText('Guitarra Rock')).toBeTruthy();
    expect(images()[0].props.source).toEqual({ uri: 'http://img/x.png' });
    expect(screen.queryByTestId('icon-Guitar')).toBeNull();
  });

  it('sin imagen muestra el icono del instrumento del título', async () => {
    await render(<SuggestionCard label="Explora" title="Piano Jazz" onPress={jest.fn()} />);
    expect(images()).toHaveLength(0);
    expect(screen.getByTestId('icon-Keyboard')).toBeTruthy();
  });

  it('si la imagen falla al cargar cambia al icono', async () => {
    await render(<SuggestionCard label="Explora" title="Batería Rock" imageUrl="http://img/rota.png" onPress={jest.fn()} />);
    await fireEvent(images()[0], 'error');
    expect(images()).toHaveLength(0);
    expect(screen.getByTestId('icon-Drum')).toBeTruthy();
  });

  it('llama a onPress al pulsar la card', async () => {
    const onPress = jest.fn();
    await render(<SuggestionCard label="Explora" title="Rock" onPress={onPress} />);
    await fireEvent.press(screen.getByRole('button'));
    expect(onPress).toHaveBeenCalledTimes(1);
  });
});
