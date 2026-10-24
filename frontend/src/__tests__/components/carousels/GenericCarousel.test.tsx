import { StyleSheet } from 'react-native';
import { render, screen, fireEvent } from '@testing-library/react-native';
import GenericCarousel from '@/components/carousels/GenericCarousel';
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
  { id: 's3', title: 'Sin artista', imgUrl: 'http://img/3.png' },
];

const images = () => screen.container.queryAll((n) => n.type === 'Image');

describe('GenericCarousel', () => {
  beforeEach(() => mockNavigate.mockClear());

  it('muestra el título, los items y sus artistas', async () => {
    await render(<GenericCarousel title="Recientes" data={data} />);
    expect(screen.getByText('Recientes')).toBeTruthy();
    expect(screen.getByText('Wonderwall')).toBeTruthy();
    expect(screen.getByText('Oasis')).toBeTruthy();
    expect(screen.getByText('Radiohead')).toBeTruthy();
    expect(images()).toHaveLength(3);
    expect(images()[0].props.source).toEqual({ uri: 'http://img/1.png' });
  });

  it('oculta los artistas con showArtist=false', async () => {
    await render(<GenericCarousel title="T" data={data} showArtist={false} />);
    expect(screen.getByText('Wonderwall')).toBeTruthy();
    expect(screen.queryByText('Oasis')).toBeNull();
  });

  it('por defecto navega a SongScreen al pulsar un item', async () => {
    await render(<GenericCarousel title="T" data={data} />);
    await fireEvent.press(images()[1]);
    expect(mockNavigate).toHaveBeenCalledWith('SongScreen', { songId: 's2' });
  });

  it('usa el callback personalizado si se pasa', async () => {
    const onItemPress = jest.fn();
    await render(<GenericCarousel title="T" data={data} onItemPress={onItemPress} />);
    await fireEvent.press(images()[0]);
    expect(onItemPress).toHaveBeenCalledWith('s1');
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('no rompe si la navegación falla', async () => {
    mockNavigate.mockImplementationOnce(() => {
      throw new Error('sin navegador');
    });
    await render(<GenericCarousel title="T" data={data} />);
    await fireEvent.press(images()[0]);
    expect(console.warn).toHaveBeenCalledWith('[GenericCarousel] Navigation not available');
  });

  it.each([
    ['small', 51, 93, 6],
    ['medium', 72, 72, 6],
    ['large', 100, 100, 6],
  ] as const)('con tamaño %s las imágenes miden %ix%i', async (size, w, h, radius) => {
    await render(<GenericCarousel title="T" data={data} size={size} />);
    expect(StyleSheet.flatten(images()[0].props.style)).toEqual(
      expect.objectContaining({ width: w, height: h, borderRadius: radius })
    );
  });

  it('con borderSquare las imágenes no tienen bordes redondeados', async () => {
    await render(<GenericCarousel title="T" data={data} borderSquare />);
    expect(StyleSheet.flatten(images()[0].props.style).borderRadius).toBe(0);
  });

  it('sin datos ni mensaje no renderiza nada', async () => {
    await render(<GenericCarousel title="Vacío" data={[]} />);
    expect(screen.queryByText('Vacío')).toBeNull();
  });

  it('sin datos y con mensaje muestra la card de vacío', async () => {
    await render(<GenericCarousel title="Recientes" data={[]} emptyMessage="Aún no has visto nada" />);
    expect(screen.getByText('Recientes')).toBeTruthy();
    expect(screen.getByText('Aún no has visto nada')).toBeTruthy();
    expect(images()).toHaveLength(0);
  });
});
