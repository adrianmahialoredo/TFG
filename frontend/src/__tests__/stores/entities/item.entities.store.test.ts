import { useItemEntitiesStore } from '../../../stores/entities/item.entities.store';
import { ItemType } from '../../../types';

const item = (overrides: Record<string, unknown> = {}) =>
  ({ id: '1', title: 'T', imageUrl: '', type: ItemType.SONG, ...overrides }) as any;

describe('item.entities.store', () => {
  beforeEach(() => {
    useItemEntitiesStore.getState().clearItems();
  });

  it('guarda los resúmenes por id como string', () => {
    useItemEntitiesStore.getState().setItems([item({ id: 5 }), item({ id: '6' })]);
    expect(Object.keys(useItemEntitiesStore.getState().items)).toEqual(['5', '6']);
  });

  it('fusiona con el resumen previo sin perder metadatos de búsqueda', () => {
    const { setItems } = useItemEntitiesStore.getState();
    setItems([item({ genreIds: ['g1'], decade: '1980s', title: 'Viejo' })]);
    setItems([item({ title: 'Nuevo' })]);

    expect(useItemEntitiesStore.getState().items['1']).toEqual(
      item({ genreIds: ['g1'], decade: '1980s', title: 'Nuevo' })
    );
  });

  it('los campos nuevos sobrescriben a los previos', () => {
    const { setItems } = useItemEntitiesStore.getState();
    setItems([item({ genreIds: ['g1'] })]);
    setItems([item({ genreIds: ['g2'] })]);
    expect(useItemEntitiesStore.getState().items['1'].genreIds).toEqual(['g2']);
  });

  it('conserva los demás items', () => {
    const { setItems } = useItemEntitiesStore.getState();
    setItems([item({ id: '1' })]);
    setItems([item({ id: '2' })]);
    expect(Object.keys(useItemEntitiesStore.getState().items).sort()).toEqual(['1', '2']);
  });

  it('clearItems vacía la store', () => {
    useItemEntitiesStore.getState().setItems([item()]);
    useItemEntitiesStore.getState().clearItems();
    expect(useItemEntitiesStore.getState().items).toEqual({});
  });
});
