import API_ENDPOINTS, { API_ENDPOINTS as namedEndpoints } from '../../config/apiConfig';

/** Recorre el objeto de endpoints y devuelve [ruta, valor] de cada hoja. */
const leaves = (obj: Record<string, unknown>, prefix = ''): [string, unknown][] =>
  Object.entries(obj).flatMap(([key, value]) =>
    value && typeof value === 'object'
      ? leaves(value as Record<string, unknown>, `${prefix}${key}.`)
      : [[`${prefix}${key}`, value] as [string, unknown]]
  );

describe('API_ENDPOINTS', () => {
  it('el export por defecto y el nombrado son el mismo objeto', () => {
    expect(API_ENDPOINTS).toBe(namedEndpoints);
  });

  it('todas las rutas cuelgan de /api y no tienen dobles barras', () => {
    leaves(API_ENDPOINTS).forEach(([path, value]) => {
      const url = typeof value === 'function' ? (value as (...a: string[]) => string)('1', '2') : value;
      expect(typeof url).toBe('string');
      expect(url as string).toMatch(/^https?:\/\/[^/]+\/api\//);
      expect((url as string).replace(/^https?:\/\//, '')).not.toContain('//');
      // Sin "undefined" por parámetros mal interpolados
      expect(url as string).not.toContain('undefined');
      expect(path).toBeTruthy();
    });
  });

  it('las funciones interpolan sus parámetros', () => {
    expect(API_ENDPOINTS.USERS.BY_ID(5)).toMatch(/\/api\/users\/5$/);
    expect(API_ENDPOINTS.USERS.FAVORITE_GENRE('u', 'g')).toMatch(/\/api\/users\/u\/favorite-genres\/g$/);
    expect(API_ENDPOINTS.USERS.FAVORITE_INSTRUMENT('u', 'i')).toMatch(/\/api\/users\/u\/favorite-instruments\/i$/);
    expect(API_ENDPOINTS.SONGS.BY_DECADE('1980s')).toMatch(/\/api\/songs\/by-decade\/1980s$/);
    expect(API_ENDPOINTS.ITEM_LISTS.ITEM('l', 'it')).toMatch(/\/api\/item-lists\/l\/items\/it$/);
    expect(API_ENDPOINTS.TUNES.BY_INSTRUMENT('1')).toMatch(/\/api\/tunes\/by-instrument\/1$/);
    expect(API_ENDPOINTS.GENRES.MOST_USED_IN_ITEM_LISTS_IDS_BY_USER('u')).toMatch(
      /\/api\/recommendations\/genres\/most-used-in-item-lists\/by-user\/u\/ids$/
    );
  });

  it('los BY_IDS de cada recurso terminan en /by-ids', () => {
    leaves(API_ENDPOINTS)
      .filter(([path]) => path.endsWith('.BY_IDS'))
      .forEach(([, url]) => expect(url).toMatch(/\/by-ids$/));
  });
});
