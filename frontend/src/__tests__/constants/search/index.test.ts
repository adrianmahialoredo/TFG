import { getActiveSearchVocabularies } from '../../../constants/search';
import { es } from '../../../constants/search/es';
import { normalizeSearchText } from '../../../utils/search.utils';

const mockLocale = (locale: string) =>
  jest.spyOn(Intl, 'DateTimeFormat').mockImplementation(
    () => ({ resolvedOptions: () => ({ locale }) }) as unknown as Intl.DateTimeFormat
  );

describe('getActiveSearchVocabularies', () => {
  it('incluye siempre el vocabulario del catálogo (español)', () => {
    mockLocale('en-US');
    expect(getActiveSearchVocabularies()).toEqual([es]);
  });

  it('no duplica el vocabulario si el dispositivo está en español', () => {
    mockLocale('es-ES');
    expect(getActiveSearchVocabularies()).toEqual([es]);
  });

  it('normaliza el idioma del dispositivo a minúsculas', () => {
    mockLocale('ES');
    expect(getActiveSearchVocabularies()).toEqual([es]);
  });

  it('si no se puede averiguar el idioma usa solo el del catálogo', () => {
    jest.spyOn(Intl, 'DateTimeFormat').mockImplementation(() => {
      throw new Error('sin Intl');
    });
    expect(getActiveSearchVocabularies()).toEqual([es]);
  });
});

describe('vocabulario es', () => {
  it('tiene claves y valores ya normalizados', () => {
    const entries = [...Object.entries(es.terms), ...Object.entries(es.decades)];
    entries.forEach(([key, value]) => {
      expect(normalizeSearchText(key)).toBe(key);
      expect(normalizeSearchText(value)).toBe(value);
    });
  });

  it('las décadas tienen el formato canónico "1980s"', () => {
    Object.values(es.decades).forEach((decade) => expect(decade).toMatch(/^\d{3}0s$/));
  });
});
