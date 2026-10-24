import { StyleSheet } from 'react-native';
import { render, screen, fireEvent } from '@testing-library/react-native';
import GeneralLessonsCarousel from '@/components/carousels/GeneralLessonsCarousel';
import { CarouselItemDTO } from '@/types';

jest.setTimeout(30000);

const mockNavigate = jest.fn();
jest.mock('@react-navigation/native', () => ({
  ...jest.requireActual('@react-navigation/native'),
  useNavigation: () => ({ navigate: mockNavigate }),
}));

const data: CarouselItemDTO[] = [
  { id: 'a', title: 'Lección A', imgUrl: 'http://img/a.png', artist: 'Artista A' },
  { id: 'b', title: 'Lección B', imgUrl: 'http://img/b.png', artist: 'Artista B' },
];

const images = () => screen.container.queryAll((n) => n.type === 'Image');

describe('GeneralLessonsCarousel', () => {
  beforeEach(() => mockNavigate.mockClear());

  it('usa el tamaño pequeño y no muestra artista', async () => {
    await render(<GeneralLessonsCarousel title="Lecciones" data={data} />);
    expect(screen.getByText('Lecciones')).toBeTruthy();
    expect(screen.getByText('Lección A')).toBeTruthy();
    expect(screen.queryByText('Artista A')).toBeNull();
    expect(StyleSheet.flatten(images()[0].props.style)).toEqual(expect.objectContaining({ width: 51, height: 93 }));
  });

  it('navega a LessonScreen con el id pulsado', async () => {
    await render(<GeneralLessonsCarousel title="Lecciones" data={data} />);
    await fireEvent.press(images()[1]);
    expect(mockNavigate).toHaveBeenCalledWith('LessonScreen', { lessonId: 'b' });
  });
});
