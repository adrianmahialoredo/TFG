import { act, renderHook } from '@testing-library/react-native';
import { useGenreInstruments } from '../../hooks/useGenreInstruments';
import { useInstrumentGenreEntitiesStore } from '../../stores/entities';

describe('useGenreInstruments', () => {
  beforeEach(() => {
    useInstrumentGenreEntitiesStore.setState({
      instrumentGenres: {
        a: { id: 'a', genreId: 'g1', title: 'Guitarra' } as any,
        b: { id: 'b', genreId: 'g1', title: 'Bajo' } as any,
        c: { id: 'c', genreId: 'g2', title: 'Piano' } as any,
        d: { id: 'd', genreId: 'g1' } as any,
      },
    });
  });

  it('filtra por género y ordena por título', async () => {
    const { result } = await renderHook(() => useGenreInstruments('g1'));
    expect(result.current.instrumentGenres.map((ig) => ig.id)).toEqual(['d', 'b', 'a']);
    expect(result.current.selectedInstrumentGenre).toBeNull();
  });

  it('selecciona y deselecciona un instrumento', async () => {
    const { result } = await renderHook(() => useGenreInstruments('g1'));

    await act(async () => result.current.selectInstrument('a'));
    expect(result.current.selectedInstrumentGenre).toEqual(expect.objectContaining({ id: 'a' }));

    await act(async () => result.current.unselectInstrument());
    expect(result.current.selectedInstrumentGenre).toBeNull();
  });
});
