import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { detectPitch } from '../utils/pitch.utils';
import { centsBetween, closestStringIndex, median, noteToFrequency } from '../utils/tuner.utils';
import {
  AudioBlock,
  createNativeMicrophone,
  createSimulatedMicrophone,
  isMicrophoneSimulated,
  Microphone,
} from '../utils/microphone.utils';

/** Bloques con nota que se combinan (mediana) para que la aguja no tiemble. */
const SMOOTHING_FRAMES = 3;

/**
 * - idle: sin escuchar.
 * - starting: pidiendo permiso / arrancando el micrófono.
 * - listening: escuchando.
 * - denied: el usuario no ha dado permiso de micrófono.
 * - error: el micrófono no ha arrancado.
 */
export type TunerStatus = 'idle' | 'starting' | 'listening' | 'denied' | 'error';

export type TunerReading = {
  /** Frecuencia detectada (Hz). */
  frequency: number;
  /** Cuerda con la que se compara (la elegida a mano o la más cercana). */
  stringIndex: number;
  /** Desviación respecto a la nota de esa cuerda (negativa: hay que tensar). */
  cents: number;
};

/**
 * Afinador: escucha el micrófono, detecta la nota de cada bloque (YIN, en `utils/pitch.utils`)
 * y la compara con las cuerdas de la afinación (`notes`).
 *
 * - Sin cuerda fijada se compara con la cuerda más cercana; `selectString(i)` fija una.
 * - El permiso de micrófono se pide al empezar (`start`); si se deniega, `status` es 'denied'.
 * - El micrófono real (`react-native-audio-api`) necesita una development build. En Expo Go se
 *   usa un micrófono simulado (`simulated`: true) que pasa por el mismo análisis.
 */
export const useTuner = (notes: string[]) => {
  const [status, setStatus] = useState<TunerStatus>('idle');
  const [reading, setReading] = useState<TunerReading | null>(null);
  const [lockedString, setLockedString] = useState<number | null>(null);

  const notesKey = notes.join(',');
  const frequencies = useMemo(
    () => notes.map((note) => noteToFrequency(note) ?? NaN),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [notesKey]
  );

  // Refs que lee el callback del micrófono (va fuera del ciclo de render)
  const frequenciesRef = useRef(frequencies);
  const lockedStringRef = useRef(lockedString);
  frequenciesRef.current = frequencies;
  lockedStringRef.current = lockedString;

  const simulated = useMemo(() => isMicrophoneSimulated(), []);
  const microphoneRef = useRef<Microphone | null>(null);
  const getMicrophone = useCallback(() => {
    microphoneRef.current ??= simulated
      ? createSimulatedMicrophone(() => frequenciesRef.current, () => lockedStringRef.current)
      : createNativeMicrophone();
    return microphoneRef.current;
  }, [simulated]);

  const recentPitchesRef = useRef<number[]>([]);
  // Cada start/stop incrementa la sesión: un start que aún espera al permiso sabe si lo han cancelado
  const sessionRef = useRef(0);

  // Otra afinación: las lecturas y la cuerda fijada eran de las cuerdas anteriores
  useEffect(() => {
    recentPitchesRef.current = [];
    setReading(null);
    setLockedString(null);
  }, [notesKey]);

  // Hasta cuándo se descartan bloques (ver handleBlock)
  const skipUntilRef = useRef(0);

  const handleBlock = useCallback(({ samples, sampleRate }: AudioBlock) => {
    // El análisis corre en el hilo de JS, el mismo que atiende los toques y el scroll. Si un
    // bloque tarda X ms en analizarse, se descartan los que lleguen en los X ms siguientes: el
    // análisis nunca ocupa más de la mitad del hilo y, en un móvil lento, la app sigue respondiendo
    // (la aguja se actualiza menos veces por segundo en vez de bloquearlo todo).
    const startedAt = Date.now();
    if (startedAt < skipUntilRef.current) return;
    const pitch = detectPitch(samples, sampleRate);
    const finishedAt = Date.now();
    skipUntilRef.current = finishedAt + (finishedAt - startedAt);
    if (pitch === null) {
      // Silencio: se mantiene la última lectura, pero la siguiente nota empieza de cero
      recentPitchesRef.current = [];
      return;
    }

    const recent = recentPitchesRef.current;
    recent.push(pitch);
    if (recent.length > SMOOTHING_FRAMES) recent.shift();
    const frequency = median(recent);

    const targets = frequenciesRef.current;
    const stringIndex = lockedStringRef.current ?? closestStringIndex(frequency, targets);
    const target = targets[stringIndex];
    if (target === undefined || Number.isNaN(target)) return;

    setReading({ frequency, stringIndex, cents: centsBetween(frequency, target) });
  }, []);

  const releaseMicrophone = useCallback(async () => {
    await microphoneRef.current?.stop();
  }, []);

  const stop = useCallback(async () => {
    sessionRef.current += 1;
    recentPitchesRef.current = [];
    setReading(null);
    setStatus((current) => (current === 'denied' ? current : 'idle'));
    try {
      await releaseMicrophone();
    } catch (error) {
      console.warn('[useTuner] Error parando el micrófono:', error);
    }
  }, [releaseMicrophone]);

  const start = useCallback(async () => {
    if (microphoneRef.current?.isRecording()) return;
    const session = ++sessionRef.current;
    setStatus('starting');

    try {
      const microphone = getMicrophone();
      const permission = await microphone.requestPermission();
      if (session !== sessionRef.current) return;
      if (permission !== 'Granted') {
        setStatus('denied');
        return;
      }

      await microphone.start(handleBlock, (message) => {
        console.error('[useTuner] Error del micrófono:', message);
        setStatus('error');
      });
      if (session !== sessionRef.current) {
        // Se paró mientras arrancaba
        await releaseMicrophone();
        return;
      }
      setStatus('listening');
    } catch (error) {
      console.error('[useTuner] No se pudo iniciar el micrófono:', error);
      if (session === sessionRef.current) setStatus('error');
      await releaseMicrophone().catch(() => {});
    }
  }, [getMicrophone, handleBlock, releaseMicrophone]);

  // Al desmontar se libera el micrófono
  useEffect(() => () => {
    sessionRef.current += 1;
    releaseMicrophone().catch(() => {});
  }, [releaseMicrophone]);

  /** Fija la cuerda con la que se compara (null: la más cercana a lo que suena). */
  const selectString = useCallback((index: number | null) => {
    recentPitchesRef.current = [];
    setReading(null);
    setLockedString(index);
  }, []);

  return {
    status,
    isListening: status === 'listening',
    simulated,
    reading,
    lockedString,
    start,
    stop,
    selectString,
  } as const;
};
