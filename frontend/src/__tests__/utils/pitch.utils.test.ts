import { detectPitch, MIN_RMS, rms } from '../../utils/pitch.utils';
import { centsBetween } from '../../utils/tuner.utils';

const SAMPLE_RATE = 44100;
const LENGTH = 4096;

/** Señal con la fundamental y armónicos: `harmonics[k]` es la amplitud del armónico k + 1. */
const tone = (frequency: number, harmonics: number[] = [1], amplitude = 0.5, sampleRate = SAMPLE_RATE) => {
  const samples = new Float32Array(LENGTH);
  for (let i = 0; i < LENGTH; i++) {
    let value = 0;
    harmonics.forEach((weight, k) => {
      value += weight * Math.sin((2 * Math.PI * frequency * (k + 1) * i) / sampleRate);
    });
    samples[i] = amplitude * value;
  }
  return samples;
};

/** Ruido blanco determinista (generador congruencial), para que el test no dependa del azar. */
const noise = (amplitude: number) => {
  let seed = 12345;
  const samples = new Float32Array(LENGTH);
  for (let i = 0; i < LENGTH; i++) {
    seed = (seed * 1103515245 + 12345) % 2147483648;
    samples[i] = amplitude * ((seed / 2147483648) * 2 - 1);
  }
  return samples;
};

const expectCloseInCents = (detected: number | null, expected: number, maxCents = 1) => {
  expect(detected).not.toBeNull();
  expect(Math.abs(centsBetween(detected!, expected))).toBeLessThan(maxCents);
};

describe('pitch.utils', () => {
  describe('rms', () => {
    it('es 0 en silencio y en un bloque vacío', () => {
      expect(rms(new Float32Array(LENGTH))).toBe(0);
      expect(rms(new Float32Array(0))).toBe(0);
    });

    it('de una senoidal es su amplitud / √2', () => {
      expect(rms(tone(440, [1], 0.5))).toBeCloseTo(0.5 / Math.SQRT2, 2);
    });
  });

  describe('detectPitch', () => {
    it.each([
      ['Si0 del bajo de 5 cuerdas', 30.87],
      ['Mi1 del bajo', 41.2],
      ['Mi2 de la guitarra', 82.41],
      ['La2', 110],
      ['Mi4', 329.63],
      ['La4 de referencia', 440],
      ['La5', 880],
    ])('detecta el %s (%f Hz) con menos de 1 cent de error', (_name, frequency) => {
      expectCloseInCents(detectPitch(tone(frequency), SAMPLE_RATE), frequency);
    });

    it('usa la frecuencia de muestreo del bloque (48 kHz)', () => {
      expectCloseInCents(detectPitch(tone(196, [1], 0.5, 48000), 48000), 196);
    });

    it('detecta la fundamental aunque un armónico suene más fuerte (cuerda grave real)', () => {
      // Mi2 con el armónico 2 al doble de volumen que la fundamental: una FFT daría Mi3
      expectCloseInCents(detectPitch(tone(82.41, [0.5, 1, 0.6, 0.3]), SAMPLE_RATE), 82.41);
    });

    it('distingue una cuerda desafinada unos pocos cents', () => {
      const slightlyFlat = 110 * Math.pow(2, -8 / 1200); // La2 8 cents bajo
      const detected = detectPitch(tone(slightlyFlat), SAMPLE_RATE)!;
      expect(centsBetween(detected, 110)).toBeCloseTo(-8, 0);
    });

    it('devuelve null en silencio y por debajo del volumen mínimo', () => {
      expect(detectPitch(new Float32Array(LENGTH), SAMPLE_RATE)).toBeNull();
      expect(detectPitch(tone(110, [1], MIN_RMS), SAMPLE_RATE)).toBeNull();
    });

    it('devuelve null con ruido (no hay una nota)', () => {
      expect(detectPitch(noise(0.5), SAMPLE_RATE)).toBeNull();
    });

    it('devuelve null si el bloque es demasiado corto para analizarlo', () => {
      expect(detectPitch(tone(440).slice(0, 32), SAMPLE_RATE)).toBeNull();
    });
  });
});
