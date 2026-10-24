import { createRevalidator } from '../../utils/revalidate.utils';

type Entity = { id: string; v?: number };

const flush = () => new Promise((resolve) => setTimeout(resolve, 0));

// El estado de frescura es global al módulo: cada test usa un nombre de entidad distinto
let counter = 0;
const uniqueName = () => `entidad-${++counter}`;

const setup = (fetchImpl?: (ids: string[]) => Promise<Entity[]>) => {
  const fetchByIds = jest.fn(fetchImpl ?? (async (ids: string[]) => ids.map((id) => ({ id }))));
  const save = jest.fn();
  const revalidator = createRevalidator<Entity>(uniqueName(), fetchByIds, save, (e) => e.id);
  return { fetchByIds, save, revalidator };
};

describe('createRevalidator', () => {
  it('pide en segundo plano las entidades no frescas y guarda la respuesta', async () => {
    const { fetchByIds, save, revalidator } = setup();

    revalidator.revalidate(['1', '2']);
    expect(fetchByIds).toHaveBeenCalledWith(['1', '2']);

    await flush();
    expect(save).toHaveBeenCalledWith([{ id: '1' }, { id: '2' }]);
  });

  it('revalida cada entidad como mucho una vez por sesión', async () => {
    const { fetchByIds, revalidator } = setup();

    revalidator.revalidate(['1']);
    revalidator.revalidate(['1']);
    await flush();
    revalidator.revalidate(['1']);

    expect(fetchByIds).toHaveBeenCalledTimes(1);
  });

  it('solo pide las entidades que aún no son frescas', () => {
    const { fetchByIds, revalidator } = setup();

    revalidator.revalidate(['1']);
    revalidator.revalidate(['1', '2']);

    expect(fetchByIds).toHaveBeenNthCalledWith(2, ['2']);
  });

  it('no pide nada si la lista está vacía', () => {
    const { fetchByIds, revalidator } = setup();
    revalidator.revalidate([]);
    expect(fetchByIds).not.toHaveBeenCalled();
  });

  it('no revalida entidades marcadas como frescas', () => {
    const { fetchByIds, revalidator } = setup();

    revalidator.markFresh(['1', '2']);
    revalidator.revalidate(['1', '2']);

    expect(fetchByIds).not.toHaveBeenCalled();
  });

  it('descarta la respuesta de una entidad modificada mientras la petición estaba en vuelo', async () => {
    let resolve!: (value: Entity[]) => void;
    const { save, revalidator } = setup(() => new Promise((r) => (resolve = r)));

    revalidator.revalidate(['1', '2']);
    revalidator.markFresh(['1']); // Escritura posterior: la respuesta del 1 ya es vieja
    resolve([{ id: '1', v: 0 }, { id: '2', v: 0 }]);
    await flush();

    expect(save).toHaveBeenCalledWith([{ id: '2', v: 0 }]);
  });

  it('no guarda nada si todas las entidades se modificaron durante la petición', async () => {
    let resolve!: (value: Entity[]) => void;
    const { save, revalidator } = setup(() => new Promise((r) => (resolve = r)));

    revalidator.revalidate(['1']);
    revalidator.markFresh(['1']);
    resolve([{ id: '1' }]);
    await flush();

    expect(save).not.toHaveBeenCalled();
  });

  it('no guarda nada si la API devuelve una lista vacía', async () => {
    const { save, revalidator } = setup(async () => []);
    revalidator.revalidate(['1']);
    await flush();
    expect(save).not.toHaveBeenCalled();
  });

  it('si la petición falla, olvida las entidades para reintentarlo en la siguiente lectura', async () => {
    const { fetchByIds, revalidator } = setup(async () => {
      throw new Error('offline');
    });

    revalidator.revalidate(['1']);
    await flush();
    revalidator.revalidate(['1']);

    expect(fetchByIds).toHaveBeenCalledTimes(2);
  });

  it('si la petición falla, no olvida las entidades que se escribieron mientras tanto', async () => {
    let reject!: (error: Error) => void;
    const fetchByIds = jest.fn()
      .mockImplementationOnce(() => new Promise((_, r) => (reject = r)))
      .mockResolvedValue([]);
    const revalidator = createRevalidator<Entity>(uniqueName(), fetchByIds, jest.fn(), (e) => e.id);

    revalidator.revalidate(['1', '2']);
    revalidator.markFresh(['1']);
    reject(new Error('offline'));
    await flush();
    revalidator.revalidate(['1', '2']);

    expect(fetchByIds).toHaveBeenLastCalledWith(['2']);
  });

  it('separa la frescura por nombre de entidad', () => {
    const a = setup();
    const b = setup();

    a.revalidator.markFresh(['1']);
    b.revalidator.revalidate(['1']);

    expect(b.fetchByIds).toHaveBeenCalledWith(['1']);
  });
});
