import { render, screen, fireEvent, waitFor } from '@testing-library/react-native';
import SuggestionCard from '@/components/SuggestionCard';
import * as imageColorRepository from '@/repositories/imageColor.repository';

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

jest.mock('@/repositories/imageColor.repository', () => ({ getDominantColor: jest.fn() }));
const mockedColor = imageColorRepository.getDominantColor as jest.Mock;

const images = () => screen.container.queryAll((n) => n.type === 'Image');
/** Estilo de la View de la card (la que lleva el borde). */
const cardStyle = () =>
  screen.container.queryAll((n) => n.type === 'View' && [n.props.style].flat().some((s: any) => s?.borderWidth === 1))[0]
    .props.style;

beforeEach(() => {
  mockedColor.mockReset();
  mockedColor.mockResolvedValue(null);
});

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

  it('toma el color principal de la imagen, con la opacidad del fondo también en el borde', async () => {
    mockedColor.mockResolvedValue('#336699');
    await render(<SuggestionCard label="Explora este género" title="Jazz" imageUrl="http://img/jazz.png" onPress={jest.fn()} />);
    await waitFor(() => expect(cardStyle()).toEqual(expect.objectContaining({
      backgroundColor: 'rgba(51, 102, 153, 0.4)',
      borderColor: 'rgba(51, 102, 153, 0.4)',
    })));
    expect(mockedColor).toHaveBeenCalledWith('http://img/jazz.png');
  });

  it('sin imagen (género-instrumento) es dorada', async () => {
    await render(<SuggestionCard label="Prueba esta combinación" title="Guitarra Rock" onPress={jest.fn()} />);
    expect(cardStyle()).toEqual(expect.objectContaining({
      backgroundColor: 'rgba(193, 133, 60, 0.4)',
      borderColor: 'rgba(193, 133, 60, 0.4)',
    }));
    expect(mockedColor).not.toHaveBeenCalled();
  });

  it('si no se puede calcular el color de la imagen es dorada', async () => {
    await render(<SuggestionCard label="Explora" title="Rock" imageUrl="http://img/rock.png" onPress={jest.fn()} />);
    await waitFor(() => expect(cardStyle().backgroundColor).toBe('rgba(193, 133, 60, 0.4)'));
  });
});
