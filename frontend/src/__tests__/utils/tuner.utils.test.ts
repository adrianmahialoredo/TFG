import {
  A4_FREQUENCY,
  centsBetween,
  closestStringIndex,
  IN_TUNE_CENTS,
  median,
  noteToFrequency,
  noteToMidi,
  tuningAdvice,
} from '../../utils/tuner.utils';

describe('tuner.utils', () => {
  describe('noteToMidi', () => {
    it.each([
      ['A4', 69],
      ['C4', 60],
      ['E2', 40],
      ['F#3', 54],
      ['B0', 23],
      ['C0', 12],
    ])('%s es la nota MIDI %i', (note, midi) => {
      expect(noteToMidi(note)).toBe(midi);
    });

    it.each(['', 'E', 'e2', 'H2', 'Bb2', 'E9', 'A#10'])('"%s" no es una nota válida', (note) => {
      expect(noteToMidi(note)).toBeNull();
    });
  });

  describe('noteToFrequency', () => {
    it.each([
      ['A4', A4_FREQUENCY],
      ['A2', 110],
      ['E2', 82.41],
      ['E4', 329.63],
      ['E1', 41.2],
      ['G4', 392],
      ['D#2', 77.78],
    ])('%s suena a %f Hz', (note, frequency) => {
      expect(noteToFrequency(note)).toBeCloseTo(frequency, 1);
    });

    it('devuelve null para una nota inválida', () => {
      expect(noteToFrequency('X1')).toBeNull();
    });
  });

  describe('centsBetween', () => {
    it('es 0 en la misma frecuencia, ±1200 a una octava y 100 a un semitono', () => {
      expect(centsBetween(110, 110)).toBe(0);
      expect(centsBetween(220, 110)).toBeCloseTo(1200);
      expect(centsBetween(55, 110)).toBeCloseTo(-1200);
      expect(centsBetween(noteToFrequency('A#2')!, 110)).toBeCloseTo(100);
    });

    it('es negativo si la nota está baja', () => {
      expect(centsBetween(108, 110)).toBeLessThan(0);
    });
  });

  describe('closestStringIndex', () => {
    const standard = ['E2', 'A2', 'D3', 'G3', 'B3', 'E4'].map((n) => noteToFrequency(n)!);

    it('elige la cuerda más cercana', () => {
      expect(closestStringIndex(108, standard)).toBe(1); // La2 bajo
      expect(closestStringIndex(330, standard)).toBe(5); // Mi4
      expect(closestStringIndex(70, standard)).toBe(0); // muy por debajo del Mi2
    });

    it('compara en cents, no en Hz', () => {
      // 96 Hz: en Hz ganaría el Mi2 (a 13.6 Hz, frente a 14 Hz del La2),
      // pero en cents está más cerca del La2 (-236) que del Mi2 (+264)
      expect(closestStringIndex(96, standard)).toBe(1);
    });

    it('devuelve -1 sin cuerdas', () => {
      expect(closestStringIndex(110, [])).toBe(-1);
    });
  });

  describe('tuningAdvice', () => {
    it('dentro del margen está afinado', () => {
      expect(tuningAdvice(0)).toBe('Afinado');
      expect(tuningAdvice(IN_TUNE_CENTS)).toBe('Afinado');
      expect(tuningAdvice(-IN_TUNE_CENTS)).toBe('Afinado');
    });

    it('baja hay que tensar y alta destensar', () => {
      expect(tuningAdvice(-12)).toBe('Tensar');
      expect(tuningAdvice(30)).toBe('Destensar');
    });
  });

  describe('median', () => {
    it('ignora un valor raro', () => {
      expect(median([110, 220, 111])).toBe(111);
    });

    it('con un número par promedia los dos centrales', () => {
      expect(median([1, 4, 2, 3])).toBe(2.5);
    });

    it('sin valores es NaN y no modifica la lista', () => {
      expect(median([])).toBeNaN();
      const values = [3, 1, 2];
      median(values);
      expect(values).toEqual([3, 1, 2]);
    });
  });
});
