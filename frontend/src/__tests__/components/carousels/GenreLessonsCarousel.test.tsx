import { render, screen, fireEvent } from '@testing-library/react-native';
import GenreLessonsCarousel from '@/components/carousels/GenreLessonsCarousel';
import { useRecommendedLessonsByGenre } from '@/hooks/useRecommendedLessonsByGenre';

jest.setTimeout(30000);

const mockNavigate = jest.fn();
jest.mock('@react-navigation/native', () => ({
  ...jest.requireActual('@react-navigation/native'),
  useNavigation: () => ({ navigate: mockNavigate }),
}));
jest.mock('@/hooks/useRecommendedLessonsByGenre', () => ({ useRecommendedLessonsByGenre: jest.fn() }));

const mockedHook = useRecommendedLessonsByGenre as jest.Mock;

const lessons = [
  { id: 'l1', title: 'Power chords', imageUrl: 'http://img/l1.png' },
  { id: 'l2', title: 'Palm mute', imageUrl: 'http://img/l2.png' },
];

describe('GenreLessonsCarousel', () => {
  beforeEach(() => mockNavigate.mockClear());

  it('pide las lecciones del género indicado y las muestra', async () => {
    mockedHook.mockReturnValue({ lessons });
    await render(<GenreLessonsCarousel title="Lecciones de Rock" genreId="g1" />);
    expect(mockedHook).toHaveBeenCalledWith('g1');
    expect(screen.getByText('Lecciones de Rock')).toBeTruthy();
    expect(screen.getByText('Power chords')).toBeTruthy();
    expect(screen.getByText('Palm mute')).toBeTruthy();
  });

  it('navega a la lección pulsada', async () => {
    mockedHook.mockReturnValue({ lessons });
    await render(<GenreLessonsCarousel title="Lecciones" genreId="g1" />);
    const images = screen.container.queryAll((n) => n.type === 'Image');
    await fireEvent.press(images[1]);
    expect(mockNavigate).toHaveBeenCalledWith('LessonScreen', { lessonId: 'l2' });
  });

  it('no renderiza nada si no hay lecciones', async () => {
    mockedHook.mockReturnValue({ lessons: [] });
    await render(<GenreLessonsCarousel title="Lecciones" genreId="g1" />);
    expect(screen.queryByText('Lecciones')).toBeNull();
  });
});
