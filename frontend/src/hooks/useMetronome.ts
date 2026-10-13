import { useCallback, useEffect, useRef, useState } from 'react';
import { AudioPlayer, createAudioPlayer, setAudioModeAsync } from 'expo-audio';

export type MetronomeSound = 'click' | 'hihat';

/** Tiempos por compás: el primero suena acentuado. */
export const BEATS_PER_BAR = 4;

/**
 * Sonidos del metrónomo: archivo y volumen (0 a 1) de cada golpe.
 * - `normal`: golpes 2, 3 y 4 del compás. `accent`: primer golpe.
 * - Los .wav están en `assets/sounds` y se generan con `scripts/generate-metronome-sounds.py`
 *   (o se sustituyen por muestras propias con el mismo nombre).
 */
const SOURCES = {
  click: {
    normal: { file: require('../../assets/sounds/click.wav'), volume: 0.8 },
    accent: { file: require('../../assets/sounds/click_accent.wav'), volume: 1 },
  },
  hihat: {
    normal: { file: require('../../assets/sounds/hihat.wav'), volume: 0.8 },
    accent: { file: require('../../assets/sounds/hihat_accent.wav'), volume: 1 },
  },
} as const;

/** Volumen general del metrónomo (0 a 1): multiplica el de cada sonido. */
const MASTER_VOLUME = 1;

const createPlayer = ({ file, volume }: { file: number; volume: number }) => {
  const player = createAudioPlayer(file);
  player.volume = volume * MASTER_VOLUME;
  return player;
};

type LoadedSounds = Record<MetronomeSound, { normal: AudioPlayer; accent: AudioPlayer }>;

const releaseAll = (sounds: LoadedSounds) =>
  Object.values(sounds).forEach(({ normal, accent }) => { normal.remove(); accent.remove(); });

/**
 * Metrónomo: carga los sonidos, programa los golpes y avisa de cada uno (`onBeat`) para la animación.
 *
 * Programación: cada golpe calcula cuándo toca el siguiente a partir del instante teórico del
 * anterior (no de "ahora"), así el retraso de los timers de JS no se acumula. Un cambio de tempo
 * o de sonido con el metrónomo en marcha se aplica desde el siguiente golpe, sin reiniciar.
 */
export const useMetronome = (tempo: number, sound: MetronomeSound, onBeat?: (beatInBar: number) => void) => {
  const [isPlaying, setIsPlaying] = useState(false);
  const [ready, setReady] = useState(false);

  const soundsRef = useRef<LoadedSounds | null>(null);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const nextBeatAtRef = useRef(0);
  const beatRef = useRef(0);

  // Refs para que el bucle, ya en marcha, lea siempre los valores actuales
  const tempoRef = useRef(tempo);
  const soundRef = useRef(sound);
  const onBeatRef = useRef(onBeat);
  tempoRef.current = tempo;
  soundRef.current = sound;
  onBeatRef.current = onBeat;

  // Carga de sonidos (una vez) con expo-audio (expo-av se eliminó en el SDK 55).
  // Modo de audio: que suene aunque el iPhone esté en silencio y que se mezcle con otra música.
  useEffect(() => {
    setAudioModeAsync({ playsInSilentMode: true, interruptionMode: 'mixWithOthers' })
      .catch((error) => console.warn('[useMetronome] No se pudo configurar el modo de audio:', error));

    let loaded: LoadedSounds | null = null;
    try {
      loaded = Object.fromEntries(
        (Object.keys(SOURCES) as MetronomeSound[]).map((key) => [
          key,
          { normal: createPlayer(SOURCES[key].normal), accent: createPlayer(SOURCES[key].accent) },
        ])
      ) as LoadedSounds;
      soundsRef.current = loaded;
      setReady(true);
    } catch (error) {
      console.error('[useMetronome] Error cargando los sonidos:', error);
    }

    return () => {
      if (timerRef.current) clearTimeout(timerRef.current);
      timerRef.current = null;
      soundsRef.current = null;
      if (loaded) releaseAll(loaded);
    };
  }, []);

  const tick = useCallback(() => {
    const beatInBar = beatRef.current % BEATS_PER_BAR;
    const sounds = soundsRef.current?.[soundRef.current];
    // Rebobina y reproduce (el sonido de antes ya ha terminado: dura menos que el golpe más corto).
    // No se espera a nada para no retrasar el siguiente golpe.
    const player = sounds?.[beatInBar === 0 ? 'accent' : 'normal'];
    if (player) {
      player.seekTo(0)
        .then(() => player.play())
        .catch((error) => console.warn('[useMetronome] Error reproduciendo el golpe:', error));
    }
    onBeatRef.current?.(beatInBar);

    beatRef.current += 1;
    nextBeatAtRef.current += 60000 / tempoRef.current;
    timerRef.current = setTimeout(tick, Math.max(0, nextBeatAtRef.current - Date.now()));
  }, []);

  const start = useCallback(() => {
    // No espera a los sonidos: si aún no han cargado (o fallaron) el pulso visual funciona igual
    if (timerRef.current) return;
    beatRef.current = 0;
    nextBeatAtRef.current = Date.now();
    setIsPlaying(true);
    tick();
  }, [tick]);

  const stop = useCallback(() => {
    if (timerRef.current) clearTimeout(timerRef.current);
    timerRef.current = null;
    setIsPlaying(false);
  }, []);

  const toggle = useCallback(() => (timerRef.current ? stop() : start()), [start, stop]);

  return { isPlaying, ready, start, stop, toggle };
};

/** Tap tempo: si pasa más de este tiempo entre dos toques se empieza a medir de nuevo. */
const TAP_RESET_MS = 2000;
/** Intervalos que se promedian (los últimos). */
const TAP_SAMPLES = 4;

/**
 * Calcula el tempo a partir de los toques del usuario: promedio de los últimos intervalos.
 * Devuelve `tap`, que retorna el BPM calculado (o null si aún no hay suficientes toques).
 */
export const useTapTempo = () => {
  const tapsRef = useRef<number[]>([]);

  const tap = useCallback((): number | null => {
    const now = Date.now();
    const taps = tapsRef.current;
    if (taps.length > 0 && now - taps[taps.length - 1] > TAP_RESET_MS) taps.length = 0;
    taps.push(now);
    if (taps.length > TAP_SAMPLES + 1) taps.shift();
    if (taps.length < 2) return null;

    const averageInterval = (taps[taps.length - 1] - taps[0]) / (taps.length - 1);
    return Math.round(60000 / averageInterval);
  }, []);

  return { tap };
};
