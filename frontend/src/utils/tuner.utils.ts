/**
 * Notas, frecuencias y desviación (en cents) para el afinador.
 *
 * Las notas van en notación científica: nombre, sostenido opcional y octava ("E2", "F#3").
 * Afinación de referencia: La4 = 440 Hz, temperamento igual (12 semitonos por octava).
 */

const NOTE_NAMES = ['C', 'C#', 'D', 'D#', 'E', 'F', 'F#', 'G', 'G#', 'A', 'A#', 'B'];
const NOTE_PATTERN = /^([A-G]#?)([0-8])$/;

/** Frecuencia del La4 (nota MIDI 69). */
export const A4_FREQUENCY = 440;
const A4_MIDI = 69;

/** Margen (en cents) dentro del que la cuerda se considera afinada. Un oído entrenado distingue unos 5 cents. */
export const IN_TUNE_CENTS = 5;

/** Desviación (en cents) en el extremo del medidor. 50 cents = medio semitono: a partir de ahí, la nota más cercana es otra. */
export const MAX_GAUGE_CENTS = 50;

export type TuningAdvice = 'Afinado' | 'Tensar' | 'Destensar';

/** Número MIDI de la nota ("A4" -> 69), o null si no tiene el formato esperado. */
export const noteToMidi = (note: string): number | null => {
  const match = NOTE_PATTERN.exec(note);
  if (!match) return null;
  const [, name, octave] = match;
  return (Number(octave) + 1) * 12 + NOTE_NAMES.indexOf(name);
};

/** Frecuencia (Hz) de la nota ("A2" -> 110), o null si la nota no es válida. */
export const noteToFrequency = (note: string): number | null => {
  const midi = noteToMidi(note);
  return midi === null ? null : A4_FREQUENCY * Math.pow(2, (midi - A4_MIDI) / 12);
};

/**
 * Desviación en cents de `frequency` respecto a `target` (100 cents = 1 semitono).
 * Negativa: la nota está baja (hay que tensar). Positiva: está alta (hay que destensar).
 */
export const centsBetween = (frequency: number, target: number): number => 1200 * Math.log2(frequency / target);

/**
 * Índice de la cuerda cuya nota está más cerca de `frequency` (comparando en cents, no en Hz:
 * a 40 Hz un hercio es mucho más que a 400). -1 si no hay cuerdas.
 */
export const closestStringIndex = (frequency: number, targets: number[]): number => {
  let best = -1;
  let bestDistance = Infinity;
  targets.forEach((target, index) => {
    const distance = Math.abs(centsBetween(frequency, target));
    if (distance < bestDistance) {
      best = index;
      bestDistance = distance;
    }
  });
  return best;
};

/** Qué tiene que hacer el músico con la clavija. */
export const tuningAdvice = (cents: number): TuningAdvice => {
  if (Math.abs(cents) <= IN_TUNE_CENTS) return 'Afinado';
  return cents < 0 ? 'Tensar' : 'Destensar';
};

/** Mediana de una lista de números (los valores raros de un bloque no mueven la aguja). */
export const median = (values: number[]): number => {
  if (values.length === 0) return NaN;
  const sorted = [...values].sort((a, b) => a - b);
  const middle = Math.floor(sorted.length / 2);
  return sorted.length % 2 === 0 ? (sorted[middle - 1] + sorted[middle]) / 2 : sorted[middle];
};
