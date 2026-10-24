import AsyncStorage from '@react-native-async-storage/async-storage';
import { MAX_RECENT_ITEMS, useRecentItemsStore } from '../../../stores/entities/recentItems.entities.store';

const flush = () => new Promise((resolve) => setTimeout(resolve, 0));
const byUser = () => useRecentItemsStore.getState().byUser;

describe('recentItems.entities.store', () => {
  beforeEach(async () => {
    useRecentItemsStore.setState({ byUser: {} });
    await AsyncStorage.clear();
  });

  it('pone el item abierto el primero', () => {
    const { markAsAccessed } = useRecentItemsStore.getState();
    markAsAccessed('u1', 'a');
    markAsAccessed('u1', 'b');
    expect(byUser().u1).toEqual(['b', 'a']);
  });

  it('no repite items: reabrir uno lo mueve al principio', () => {
    const { markAsAccessed } = useRecentItemsStore.getState();
    ['a', 'b', 'c', 'a'].forEach((id) => markAsAccessed('u1', id));
    expect(byUser().u1).toEqual(['a', 'c', 'b']);
  });

  it('si ya era el último abierto no cambia el estado', () => {
    const { markAsAccessed } = useRecentItemsStore.getState();
    markAsAccessed('u1', 'a');
    const before = useRecentItemsStore.getState();
    markAsAccessed('u1', 'a');
    expect(useRecentItemsStore.getState()).toBe(before);
  });

  it(`guarda como mucho ${MAX_RECENT_ITEMS} items`, () => {
    const { markAsAccessed } = useRecentItemsStore.getState();
    for (let i = 0; i < MAX_RECENT_ITEMS + 3; i++) markAsAccessed('u1', `i${i}`);
    expect(byUser().u1).toHaveLength(MAX_RECENT_ITEMS);
    expect(byUser().u1[0]).toBe(`i${MAX_RECENT_ITEMS + 2}`);
    expect(byUser().u1).not.toContain('i0');
  });

  it('separa los recientes por usuario', () => {
    const { markAsAccessed } = useRecentItemsStore.getState();
    markAsAccessed('u1', 'a');
    markAsAccessed('u2', 'b');
    expect(byUser()).toEqual({ u1: ['a'], u2: ['b'] });
  });

  it('removeItems quita los items de todos los usuarios', () => {
    useRecentItemsStore.setState({ byUser: { u1: ['a', 'b'], u2: ['b', 'c'] } });
    useRecentItemsStore.getState().removeItems(['b', 'zz']);
    expect(byUser()).toEqual({ u1: ['a'], u2: ['c'] });
  });

  it('persiste en AsyncStorage bajo recent-items-store', async () => {
    useRecentItemsStore.getState().markAsAccessed('u1', 'a');
    await flush();
    const stored = JSON.parse((await AsyncStorage.getItem('recent-items-store'))!);
    expect(stored.state).toEqual({ byUser: { u1: ['a'] } });
  });

  it('rehidrata desde AsyncStorage', async () => {
    await AsyncStorage.setItem('recent-items-store', JSON.stringify({ state: { byUser: { u9: ['x'] } }, version: 0 }));
    await useRecentItemsStore.persist.rehydrate();
    expect(byUser()).toEqual({ u9: ['x'] });
  });
});
