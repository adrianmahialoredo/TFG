import { act, renderHook, waitFor } from '@testing-library/react-native';
import { useRecommendedLessonsByGenre } from '../../hooks/useRecommendedLessonsByGenre';
import { useLessonEntitiesStore } from '../../stores/entities';
import * as lessonRepository from '../../repositories/lesson.repository';

jest.mock('../../repositories/lesson.repository', () => ({ getRecommendedByGenreId: jest.fn() }));
const mocked = lessonRepository as jest.Mocked<typeof lessonRepository>;

describe('useRecommendedLessonsByGenre', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    useLessonEntitiesStore.setState({
      lessons: { l1: { id: 'l1' } as any, l2: { id: 'l2' } as any, l3: { id: 'l3' } as any },
    });
    mocked.getRecommendedByGenreId.mockResolvedValue(['l2', 'falta', 'l1']);
  });

  it('devuelve las lecciones recomendadas en el orden del backend', async () => {
    const { result } = await renderHook(() => useRecommendedLessonsByGenre('g1'));
    await waitFor(() => expect(result.current.lessons.map((l) => l.id)).toEqual(['l2', 'l1']));
    expect(mocked.getRecommendedByGenreId).toHaveBeenCalledWith('g1', expect.any(Function));
  });

  it('aplica los IDs frescos de onFresh', async () => {
    mocked.getRecommendedByGenreId.mockImplementation(async (_g: string, onFresh: any) => {
      setTimeout(() => onFresh(['l3']), 0);
      return ['l1'];
    });
    const { result } = await renderHook(() => useRecommendedLessonsByGenre('g1'));
    await waitFor(() => expect(result.current.lessons.map((l) => l.id)).toEqual(['l3']));
  });

  it('sin género no pide nada', async () => {
    const { result } = await renderHook(() => useRecommendedLessonsByGenre(''));
    expect(result.current.lessons).toEqual([]);
    expect(mocked.getRecommendedByGenreId).not.toHaveBeenCalled();
  });

  it('registra el error si falla', async () => {
    mocked.getRecommendedByGenreId.mockRejectedValue(new Error('red'));
    const { result } = await renderHook(() => useRecommendedLessonsByGenre('g1'));
    await waitFor(() =>
      expect(console.error).toHaveBeenCalledWith('[useRecommendedLessonsByGenre] Error:', expect.any(Error))
    );
    expect(result.current.lessons).toEqual([]);
  });

  it('ignora la respuesta del género anterior al cambiar', async () => {
    let resolveFirst: (v: string[]) => void = () => {};
    mocked.getRecommendedByGenreId
      .mockReturnValueOnce(new Promise((r) => { resolveFirst = r; }))
      .mockResolvedValueOnce(['l3']);

    const { result, rerender } = await renderHook(({ id }: { id: string }) => useRecommendedLessonsByGenre(id), {
      initialProps: { id: 'g1' },
    });
    await rerender({ id: 'g2' });
    await waitFor(() => expect(result.current.lessons.map((l) => l.id)).toEqual(['l3']));

    await act(async () => resolveFirst(['l1']));
    expect(result.current.lessons.map((l) => l.id)).toEqual(['l3']);
  });
});
