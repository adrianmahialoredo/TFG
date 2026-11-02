import { StyleSheet } from 'react-native';
import { render, screen, fireEvent } from '@testing-library/react-native';
import HorizontalItemsCarousel from '@/components/carousels/HorizontalItemsCarousel';
import { CarouselItemDTO } from '@/types';

jest.setTimeout(30000);

const mockNavigate = jest.fn();
jest.mock('@react-navigation/native', () => ({
  ...jest.requireActual('@react-navigation/native'),
  useNavigation: () => ({ navigate: mockNavigate }),
}));

const data: CarouselItemDTO[] = [
  { id: 'a', title: 'Item A', imgUrl: 'http://img/a.png', artist: 'Artista A' },
  { id: 'b', title: 'Item B', imgUrl: 'http://img/b.png' },
];

const images = () => screen.container.queryAll((n) => n.type === 'Image');

describe('HorizontalItemsCarousel', () => {
  it('usa el tamaño grande, bordes redondeados y sin artista', async () => {
    await render(<HorizontalItemsCarousel title="Para ti" data={data} />);
    expect(screen.getByText('Para ti')).toBeTruthy();
    expect(screen.queryByText('Artista A')).toBeNull();
    expect(StyleSheet.flatten(images()[0].props.style)).toEqual(
      expect.objectContaining({ width: 100, height: 100, borderRadius: 6 })
    );
  });

  it('llama a onItemPress con el id del item', async () => {
    const onItemPress = jest.fn();
    await render(<HorizontalItemsCarousel title="Para ti" data={data} onItemPress={onItemPress} />);
    await fireEvent.press(images()[0]);
    expect(onItemPress).toHaveBeenCalledWith('a');
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('muestra el mensaje de vacío si no hay datos', async () => {
    await render(<HorizontalItemsCarousel title="Para ti" data={[]} emptyMessage="Sin recomendaciones" />);
    expect(screen.getByText('Sin recomendaciones')).toBeTruthy();
  });
});
