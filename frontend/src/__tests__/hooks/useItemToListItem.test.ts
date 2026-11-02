import { act, renderHook } from '@testing-library/react-native';
import { useItemToListItem } from '../../hooks/useItemToListItem';
import { useGenreEntitiesStore, useInstrumentEntitiesStore } from '../../stores/entities';
import { ItemType } from '../../types';

const lesson = { id: 'l1', title: 'Acordes', imageUrl: 'l.png', type: ItemType.LESSON, genreIds: ['g1'], instrumentIds: ['i1'] } as any;

describe('useItemToListItem', () => {
  beforeEach(() => {
    useGenreEntitiesStore.setState({ genres: { g1: { id: 'g1', title: 'Rock' } as any } });
    useInstrumentEntitiesStore.setState({ instruments: {} });
  });

  it('nombra el género o instrumento de la lección con los de las Stores', async () => {
    const { result } = await renderHook(() => useItemToListItem());

    expect(result.current(lesson).info).toBe('Acordes\nRock · Lección');

    // Al llegar los instrumentos, el instrumento pasa por delante del género
    await act(async () => {
      useInstrumentEntitiesStore.setState({ instruments: { i1: { id: 'i1', name: 'Guitarra' } as any } });
    });
    expect(result.current(lesson).info).toBe('Acordes\nGuitarra · Lección');
  });
});
