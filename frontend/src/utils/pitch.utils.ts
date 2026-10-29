/**
 * Detección de la frecuencia fundamental de un bloque de audio con el algoritmo YIN
 * (de Cheveigné y Kawahara, 2002). Es JavaScript puro: se prueba en Jest con señales sintéticas.
 *
 * Por qué YIN y no el pico de una FFT: en una cuerda grave el armónico 2 o 3 suele tener más
 * energía que la fundamental, y el pico de la FFT daría una octava (o una quinta) de más.
 * YIN busca el periodo de la señal y no depende de qué armónico suena más fuerte.
 */

/** Rango de frecuencias que se buscan: desde el Si0 del bajo de 5 cuerdas (30.9 Hz) hasta el Mi6. */
export const MIN_FREQUENCY = 27;
export const MAX_FREQUENCY = 1400;

/**
 * Umbral de la diferencia normalizada de YIN: cuanto más bajo, más periódica tiene que ser la
 * señal para aceptarla. 0.15 es el valor del artículo original.
 */
const YIN_THRESHOLD = 0.15;

/** Volumen mínimo (RMS, con muestras de -1 a 1) para analizar el bloque: por debajo es silencio o ruido de fondo. */
export const MIN_RMS = 0.01;

/** Raíz de la media de los cuadrados: el "volumen" del bloque. */
export const rms = (samples: Float32Array): number => {
  let sum = 0;
  for (let i = 0; i < samples.length; i++) sum += samples[i] * samples[i];
  return samples.length === 0 ? 0 : Math.sqrt(sum / samples.length);
};

/**
 * Devuelve la frecuencia fundamental (Hz) del bloque, o null si es silencio o no es una nota
 * (ruido, varias notas a la vez...).
 *
 * El bloque tiene que contener al menos dos periodos de la nota más grave buscada: con 4096
 * muestras a 44.1 kHz llega hasta unos 27 Hz.
 */
export const detectPitch = (samples: Float32Array, sampleRate: number): number | null => {
  if (rms(samples) < MIN_RMS) return null;

  const minLag = Math.max(2, Math.floor(sampleRate / MAX_FREQUENCY));
  const maxLag = Math.min(Math.ceil(sampleRate / MIN_FREQUENCY), Math.floor(samples.length / 2));
  if (maxLag <= minLag) return null;
  const window = samples.length - maxLag;

  // 1-3. Para cada periodo candidato tau:
  //  - Función diferencia: d(tau) = suma de (x[j] - x[j + tau])^2. Es mínima cuando tau es el periodo.
  //  - Diferencia normalizada acumulada: evita que tau = 0 (o muy pequeño) gane siempre.
  //  - Se busca el primer mínimo por debajo del umbral (el primero es el periodo; los siguientes, sus múltiplos).
  // Se calcula tau a tau y se para en cuanto se encuentra el mínimo (más un vecino para la interpolación):
  // la función diferencia es lo caro (lags x ventana) y con esto, en una cuerda de guitarra, se calcula
  // una fracción de los lags. Importa porque corre en el hilo de JS (Hermes, sin JIT).
  const normalized = new Float32Array(maxLag + 2);
  normalized[0] = 1;
  let runningSum = 0;
  let found = -1;
  let computed = 0;
  for (let tau = 1; tau <= maxLag; tau++) {
    let sum = 0;
    for (let j = 0; j < window; j++) {
      const delta = samples[j] - samples[j + tau];
      sum += delta * delta;
    }
    runningSum += sum;
    normalized[tau] = runningSum === 0 ? 1 : (sum * tau) / runningSum;
    computed = tau;

    if (tau <= minLag) continue;
    if (found === -1) {
      if (normalized[tau - 1] < YIN_THRESHOLD) found = tau - 1;
      else continue;
    }
    // Se baja mientras siga bajando; el primer valor que sube cierra el mínimo
    if (normalized[tau] < normalized[found]) found = tau;
    else break;
  }
  if (found === -1) {
    if (computed >= minLag && normalized[computed] < YIN_THRESHOLD) found = computed;
    else return null;
  }
  const tau = found;

  // 4. Interpolación parabólica entre los vecinos: precisión por debajo de una muestra
  // (sin ella, a 110 Hz el error llega a ~0.1 Hz, unos 2 cents)
  let period = tau;
  if (tau > 1 && tau < computed) {
    const previous = normalized[tau - 1];
    const current = normalized[tau];
    const next = normalized[tau + 1];
    const denominator = previous - 2 * current + next;
    if (denominator !== 0) period = tau + (previous - next) / (2 * denominator);
  }

  return sampleRate / period;
};
