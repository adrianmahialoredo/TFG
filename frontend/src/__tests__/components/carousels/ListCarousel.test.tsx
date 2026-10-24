import { render, screen, fireEvent } from '@testing-library/react-native';
import ListCarousel from '@/components/carousels/ListCarousel';
import { CarouselItemDTO } from '@/types';

jest.setTimeout(30000);

jest.mock('lucide-react-native', () => {
  const { jsx: mockJsx } = require('react/jsx-runtime');
  const { View } = require('react-native');
  const make = (name: string) => (props: any) => mockJsx(View, { testID: `icon-${name}`, ...props });
  return { Plus: make('Plus'), Check: make('Check') };
});

const data: CarouselItemDTO[] = [
  { id: 'l1', title: 'Favoritas', imgUrl: 'http://img/fav.png' },
  { id: 'l2', title: 'practicar', imgUrl: '' },
];

const images = () => screen.container.queryAll((n) => n.type === 'Image');

describe('ListCarousel', () => {
  it('muestra cada lista y al final el botón de añadir', async () => {
    await render(<ListCarousel data={data} />);
    expect(screen.getByText('Favoritas')).toBeTruthy();
    expect(screen.getByText('practicar')).toBeTruthy();
    expect(screen.getByTestId('icon-Plus')).toBeTruthy();
  });

  it('usa la portada si la hay y la inicial en mayúscula si no', async () => {
    await render(<ListCarousel data={data} />);
    expect(images()).toHaveLength(1);
    expect(images()[0].props.source).toEqual({ uri: 'http://img/fav.png' });
    expect(screen.getByText('P')).toBeTruthy();
  });

  it('muestra "?" si la lista no tiene título ni portada', async () => {
    await render(<ListCarousel data={[{ id: 'x', title: '', imgUrl: '' }]} />);
    expect(screen.getByText('?')).toBeTruthy();
  });

  it('llama a onItemPress con el id de la lista pulsada', async () => {
    const onItemPress = jest.fn();
    await render(<ListCarousel data={data} onItemPress={onItemPress} />);
    await fireEvent.press(screen.getByText('P'));
    expect(onItemPress).toHaveBeenCalledWith('l2');
    await fireEvent.press(images()[0]);
    expect(onItemPress).toHaveBeenCalledWith('l1');
  });

  it('llama a onEmptyPress al pulsar el botón de añadir', async () => {
    const onEmptyPress = jest.fn();
    await render(<ListCarousel data={[]} onEmptyPress={onEmptyPress} />);
    await fireEvent.press(screen.getByTestId('icon-Plus'));
    expect(onEmptyPress).toHaveBeenCalledTimes(1);
  });

  it('sin datos (undefined) muestra solo el botón de añadir', async () => {
    await render(<ListCarousel data={undefined as unknown as CarouselItemDTO[]} />);
    expect(screen.getByTestId('icon-Plus')).toBeTruthy();
    expect(images()).toHaveLength(0);
  });

  it('pulsar una lista sin onItemPress no rompe', async () => {
    await render(<ListCarousel data={data} />);
    await fireEvent.press(screen.getByText('P'));
    expect(screen.getByText('practicar')).toBeTruthy();
  });
});
