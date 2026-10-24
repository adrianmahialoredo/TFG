import { MIN_FAVOURITE_GENRES, MIN_FAVOURITE_INSTRUMENTS, ORIGINAL_LESSON_MIN_LEVEL } from '../../constants/Preferences';
import { COLORS } from '../../constants/Theme';

describe('Preferences', () => {
  it('los mínimos de favoritos son enteros positivos (iguales que en el backend)', () => {
    expect(MIN_FAVOURITE_GENRES).toBe(2);
    expect(MIN_FAVOURITE_INSTRUMENTS).toBe(2);
  });

  it('el nivel para abrir la versión original está en la escala de estrellas (0 a 5)', () => {
    expect(ORIGINAL_LESSON_MIN_LEVEL).toBeGreaterThanOrEqual(0);
    expect(ORIGINAL_LESSON_MIN_LEVEL).toBeLessThanOrEqual(5);
  });
});

describe('Theme', () => {
  it('todos los colores son hexadecimales válidos', () => {
    Object.values(COLORS).forEach((color) => expect(color).toMatch(/^#[0-9A-Fa-f]{6}$/));
  });
});
