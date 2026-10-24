import { StyleSheet } from 'react-native';
import { render, screen, fireEvent } from '@testing-library/react-native';
import SongsCarousel from '@/components/carousels/SongsCarousel';
import { CarouselItemDTO } from '@/types';

jest.setTimeout(30000);

const mockNavigate = jest.fn();
jest.mock('@react-navigation/native', () => ({
  ...jest.requireActual('@react-navigation/native'),
  useNavigation: () => ({ navigate: mockNavigate }),
}));

const data: CarouselItemDTO[] = [
  { id: 's1', title: 'Wonderwall', imgUrl: 'http://img/1.png', artist: 'Oasis' },
  { id: 's2', title: 'Creep', imgUrl: 'http://img/2.png', artist: 'Radiohead' },
];

const images = () => screen.container.queryAll((n) => n.type === 'Image');

describe('SongsCarousel', () => {
  beforeEach(() => mockNavigate.mockClear());

  it('usa el tamaño medio y muestra el artista', async () => {
    await render(<SongsCarousel title="Canciones" data={data} />);
    expect(screen.getByText('Canciones')).toBeTruthy();
    expect(screen.getByText('Radiohead')).toBeTruthy();
    expect(StyleSheet.flatten(images()[0].props.style)).toEqual(expect.objectContaining({ width: 72, height: 72 }));
  });

  it('navega a SongScreen al pulsar una canción', async () => {
    await render(<SongsCarousel title="Canciones" data={data} />);
    await fireEvent.press(images()[0]);
    expect(mockNavigate).toHaveBeenCalledWith('SongScreen', { songId: 's1' });
  });

  it('no renderiza nada sin canciones', async () => {
    await render(<SongsCarousel title="Canciones" data={[]} />);
    expect(screen.queryByText('Canciones')).toBeNull();
  });
});
