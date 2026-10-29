import { createEntityCache } from '../../utils/entityCache.utils';

type Entity = { id: string | number; name?: string };

const flush = () => new Promise((resolve) => setTimeout(resolve, 0));

let counter = 0;

/** Crea una caché con RAM en memoria y Disco/API mockeados. */
const setup = (options: { disk?: Record<string, Entity>; index?: Record<string, string[]>; withIndex?: boolean } = {}) => {
  const ram: Record<string, Entity> = {};
  const disk: Record<string, Entity> = { ...(options.disk ?? {}) };
  const indexes: Record<string, string[]> = { ...(options.index ?? {}) };

  const local = {
    getAllById: jest.fn(async (ids: string[]) => ({
      cached: ids.filter((id) => disk[id]).map((id) => disk[id]),
      missing: ids.filter((id) => !disk[id]),
    })),
    saveBatch: jest.fn(async (entities: Entity[]) => {
      entities.forEach((e) => (disk[e.id.toString()] = e));
    }),
    ...(options.withIndex !== false && {
      saveQueryIndex: jest.fn(async (key: string, ids: string[]) => {
        indexes[key] = ids;
      }),
      getQueryIndex: jest.fn(async (key: string) => indexes[key] ?? null),
    }),
  };

  const setRam = jest.fn((entities: Entity[]) => entities.forEach((e) => (ram[e.id.toString()] = e)));
  const fetchByIds = jest.fn(async (ids: string[]) => ids.map((id) => ({ id: Number(id), name: `api-${id}` })));

  const cache = createEntityCache<Entity>({
    name: `test-${++counter}`,
    getRam: () => ram,
    setRam,
    local,
    fetchByIds,
  });

  return { cache, ram, disk, indexes, local, setRam, fetchByIds };
};

const unauthorized = (withToken: boolean) => ({
  isAxiosError: true,
  response: { status: 401 },
  config: { headers: withToken ? { Authorization: 'Bearer x' } : {} },
});

describe('createEntityCache', () => {
  describe('getAllById', () => {
    it('devuelve [] sin consultar nada si no hay IDs', async () => {
      const { cache, local, fetchByIds } = setup();
      expect(await cache.getAllById([])).toEqual([]);
      expect(await cache.getAllById(undefined as unknown as string[])).toEqual([]);
      expect(local.getAllById).not.toHaveBeenCalled();
      expect(fetchByIds).not.toHaveBeenCalled();
    });

    it('sirve desde RAM sin ir a Disco y revalida en segundo plano', async () => {
      const { cache, ram, local, fetchByIds } = setup();
      ram['1'] = { id: '1', name: 'ram' };

      const result = await cache.getAllById(['1']);

      expect(result).toEqual([{ id: '1', name: 'ram' }]);
      expect(local.getAllById).not.toHaveBeenCalled();
      // Revalidación Stale-While-Revalidate
      expect(fetchByIds).toHaveBeenCalledWith(['1']);
    });

    it('sube a RAM lo leído de Disco', async () => {
      const { cache, setRam } = setup({ disk: { '2': { id: '2', name: 'disco' } } });

      const result = await cache.getAllById(['2']);

      expect(result).toEqual([{ id: '2', name: 'disco' }]);
      expect(setRam).toHaveBeenCalledWith([{ id: '2', name: 'disco' }]);
    });

    it('pide a la API solo lo que falta, lo hidrata y respeta el orden pedido', async () => {
      const { cache, ram, disk, fetchByIds, local } = setup({ disk: { '2': { id: '2' } } });
      ram['1'] = { id: '1' };

      const result = await cache.getAllById(['3', '1', '2']);

      expect(fetchByIds).toHaveBeenNthCalledWith(1, ['3']);
      expect(result.map((e) => e.id.toString())).toEqual(['3', '1', '2']);
      expect(ram['3']).toEqual({ id: 3, name: 'api-3' });
      expect(local.saveBatch).toHaveBeenCalledWith([{ id: 3, name: 'api-3' }]);
      expect(disk['3']).toBeDefined();
    });

    it('acepta IDs numéricos (como llegan del backend) y devuelve las entidades', async () => {
      const { cache, ram } = setup({ disk: { '2': { id: 2 } } });
      ram['1'] = { id: 1 };

      const result = await cache.getAllById([1, 2, 3] as unknown as string[]);

      expect(result.map((e) => e.id.toString())).toEqual(['1', '2', '3']);
    });

    it('no revalida lo que acaba de llegar de la API', async () => {
      const { cache, fetchByIds } = setup();

      await cache.getAllById(['5']);
      await flush();
      await cache.getAllById(['5']); // Ya está en RAM y fresco

      expect(fetchByIds).toHaveBeenCalledTimes(1);
    });

    it('si la API falla devuelve lo que tenía en caché', async () => {
      const { cache, ram, fetchByIds } = setup();
      ram['1'] = { id: '1' };
      fetchByIds.mockRejectedValueOnce(new Error('offline'));

      const result = await cache.getAllById(['1', '9']);

      expect(result).toEqual([{ id: '1' }]);
      expect(console.error).toHaveBeenCalled();
    });

    it('la revalidación guarda en RAM y Disco la respuesta fresca', async () => {
      const { cache, ram, local } = setup();
      ram['1'] = { id: '1', name: 'viejo' };

      await cache.getAllById(['1']);
      await flush();

      expect(ram['1']).toEqual({ id: 1, name: 'api-1' });
      expect(local.saveBatch).toHaveBeenCalledWith([{ id: 1, name: 'api-1' }]);
    });

    it('registra el error si falla el guardado en Disco de la revalidación', async () => {
      const { cache, ram, local } = setup();
      ram['1'] = { id: '1' };
      local.saveBatch.mockRejectedValueOnce(new Error('disco lleno'));

      await cache.getAllById(['1']);
      await flush();

      expect(console.error).toHaveBeenCalledWith(expect.any(Error));
    });
  });

  describe('hydrate', () => {
    it('guarda en RAM, Disco e índice y devuelve los IDs como string', () => {
      const { cache, setRam, local } = setup();
      const ids = cache.hydrate([{ id: 1 }, { id: 2 }], 'clave');

      expect(ids).toEqual(['1', '2']);
      expect(setRam).toHaveBeenCalledWith([{ id: 1 }, { id: 2 }]);
      expect(local.saveBatch).toHaveBeenCalled();
      expect(local.saveQueryIndex).toHaveBeenCalledWith('clave', ['1', '2']);
    });

    it('con lista vacía no toca RAM ni Disco pero sí guarda el índice vacío', () => {
      const { cache, setRam, local } = setup();
      expect(cache.hydrate([], 'clave')).toEqual([]);
      expect(setRam).not.toHaveBeenCalled();
      expect(local.saveBatch).not.toHaveBeenCalled();
      expect(local.saveQueryIndex).toHaveBeenCalledWith('clave', []);
    });

    it('no guarda índice sin clave', () => {
      const { cache, local } = setup();
      cache.hydrate([{ id: 1 }]);
      expect(local.saveQueryIndex).not.toHaveBeenCalled();
    });

    it('no falla si el local no soporta índices', () => {
      const { cache } = setup({ withIndex: false });
      expect(cache.hydrate([{ id: 1 }], 'clave')).toEqual(['1']);
    });

    it('marca como frescas las entidades hidratadas', () => {
      const { cache, fetchByIds } = setup();
      cache.hydrate([{ id: 1 }]);
      cache.revalidate(['1']);
      expect(fetchByIds).not.toHaveBeenCalled();
    });

    it('registra los errores de guardado en Disco e índice', async () => {
      const { cache, local } = setup();
      local.saveBatch.mockRejectedValueOnce(new Error('a'));
      local.saveQueryIndex!.mockRejectedValueOnce(new Error('b'));
      cache.hydrate([{ id: 1 }], 'clave');
      await flush();
      expect(console.error).toHaveBeenCalledTimes(2);
    });
  });

  describe('readIndex', () => {
    it('devuelve [] si el local no soporta índices', async () => {
      const { cache } = setup({ withIndex: false });
      expect(await cache.readIndex('clave')).toEqual([]);
    });

    it('devuelve [] si no hay índice guardado', async () => {
      const { cache } = setup();
      expect(await cache.readIndex('clave')).toEqual([]);
    });

    it('devuelve [] si el índice está vacío', async () => {
      const { cache, local } = setup({ index: { clave: [] } });
      expect(await cache.readIndex('clave')).toEqual([]);
      expect(local.getAllById).not.toHaveBeenCalled();
    });

    it('devuelve solo los IDs con entidad disponible, en orden', async () => {
      const { cache, ram } = setup({ index: { clave: ['3', '1', '2'] }, disk: { '2': { id: '2' } } });
      ram['1'] = { id: '1' };
      expect(await cache.readIndex('clave')).toEqual(['1', '2']);
      expect(ram['2']).toEqual({ id: '2' }); // Subido a RAM
    });
  });

  describe('list', () => {
    it('sin índice espera a la red, hidrata y guarda el índice', async () => {
      const { cache, indexes, ram } = setup();
      const fetchList = jest.fn(async () => [{ id: 7 }, { id: 8 }]);

      const ids = await cache.list('lista', fetchList);

      expect(ids).toEqual(['7', '8']);
      expect(indexes.lista).toEqual(['7', '8']);
      expect(ram['7']).toEqual({ id: 7 });
    });

    it('con índice devuelve la caché al instante y avisa con los IDs frescos', async () => {
      const { cache, ram } = setup({ index: { lista: ['1'] } });
      ram['1'] = { id: '1' };
      const onFresh = jest.fn();
      const fetchList = jest.fn(async () => [{ id: 2 }]);

      const ids = await cache.list('lista', fetchList, onFresh);
      expect(ids).toEqual(['1']);

      await flush();
      expect(onFresh).toHaveBeenCalledWith(['2']);
    });

    it('con índice no falla si no se pasa onFresh', async () => {
      const { cache, ram } = setup({ index: { lista: ['1'] } });
      ram['1'] = { id: '1' };
      await expect(cache.list('lista', async () => [{ id: 2 }])).resolves.toEqual(['1']);
      await flush();
    });

    it('con índice, si el refresco falla mantiene la caché y avisa', async () => {
      const { cache, ram } = setup({ index: { lista: ['1'] } });
      ram['1'] = { id: '1' };
      const onFresh = jest.fn();

      const ids = await cache.list('lista', async () => { throw new Error('offline'); }, onFresh);
      await flush();

      expect(ids).toEqual(['1']);
      expect(onFresh).not.toHaveBeenCalled();
      expect(console.warn).toHaveBeenCalled();
    });

    it('con índice, un 401 con token en el refresco no se avisa', async () => {
      const { cache, ram } = setup({ index: { lista: ['1'] } });
      ram['1'] = { id: '1' };

      await cache.list('lista', async () => { throw unauthorized(true); });
      await flush();

      expect(console.warn).not.toHaveBeenCalled();
    });

    it('sin índice y sin red devuelve [] y registra el error', async () => {
      const { cache } = setup();
      const ids = await cache.list('lista', async () => { throw new Error('offline'); });
      expect(ids).toEqual([]);
      expect(console.error).toHaveBeenCalled();
    });

    it('sin índice, un 401 con token no se registra como error', async () => {
      const { cache } = setup();
      const ids = await cache.list('lista', async () => { throw unauthorized(true); });
      expect(ids).toEqual([]);
      expect(console.error).not.toHaveBeenCalled();
    });

    it('sin índice, un 401 sin token sí se registra como error', async () => {
      const { cache } = setup();
      await cache.list('lista', async () => { throw unauthorized(false); });
      expect(console.error).toHaveBeenCalled();
    });

    it('si leer el índice falla, se trata como si no hubiera caché', async () => {
      const { cache, local } = setup();
      local.getQueryIndex!.mockRejectedValueOnce(new Error('disco'));
      expect(await cache.list('lista', async () => [{ id: 4 }])).toEqual(['4']);
    });
  });

  describe('feed', () => {
    it('guarda el índice con los IDs que devuelve la carga', async () => {
      const { cache, indexes } = setup();
      const ids = await cache.feed('feed', async () => ['a', 'b']);
      expect(ids).toEqual(['a', 'b']);
      expect(indexes.feed).toEqual(['a', 'b']);
    });

    it('funciona aunque el local no soporte índices', async () => {
      const { cache } = setup({ withIndex: false });
      expect(await cache.feed('feed', async () => ['a'])).toEqual(['a']);
    });

    it('registra el error si falla el guardado del índice', async () => {
      const { cache, local } = setup();
      local.saveQueryIndex!.mockRejectedValueOnce(new Error('disco'));
      await cache.feed('feed', async () => ['a']);
      await flush();
      expect(console.error).toHaveBeenCalled();
    });
  });

  describe('idList', () => {
    it('convierte los IDs a string y solo pide a la API los que faltan en caché', async () => {
      const { cache, ram, fetchByIds, indexes } = setup({ disk: { '2': { id: '2' } } });
      ram['1'] = { id: '1' };

      const ids = await cache.idList('ids', async () => [1, '2', 3]);

      expect(ids).toEqual(['1', '2', '3']);
      expect(fetchByIds).toHaveBeenCalledTimes(1);
      expect(fetchByIds).toHaveBeenCalledWith(['3']);
      expect(ram['3']).toEqual({ id: 3, name: 'api-3' });
      expect(indexes.ids).toEqual(['1', '2', '3']);
    });

    it('no llama a la API si todo está en caché', async () => {
      const { cache, ram, fetchByIds } = setup();
      ram['1'] = { id: '1' };
      await cache.idList('ids', async () => [1]);
      expect(fetchByIds).not.toHaveBeenCalled();
    });

    it('si la API falla al resolver, devuelve igualmente los IDs', async () => {
      const { cache, fetchByIds } = setup();
      fetchByIds.mockRejectedValueOnce(new Error('offline'));
      expect(await cache.idList('ids', async () => [9])).toEqual(['9']);
      expect(console.error).toHaveBeenCalled();
    });
  });

  it('expone markFresh y revalidate del revalidador', () => {
    const { cache, fetchByIds } = setup();
    cache.markFresh(['1']);
    cache.revalidate(['1', '2']);
    expect(fetchByIds).toHaveBeenCalledWith(['2']);
  });
});
