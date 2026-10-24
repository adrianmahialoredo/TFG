import { Platform } from 'react-native';
import { isRunningInExpoGo } from 'expo';

/**
 * Fuente de audio del afinador. Hay dos implementaciones con la misma interfaz:
 *  - Nativa (`react-native-audio-api`): el micrófono real. Necesita una development build.
 *  - Simulada: genera el sonido de una cuerda que se va afinando. Se usa en Expo Go, que no
 *    incluye el módulo nativo, para poder probar la pantalla, la aguja y el algoritmo.
 *
 * El módulo nativo se carga con `require` al crear el micrófono nativo, nunca al importar este
 * fichero: si se importara arriba, en Expo Go fallaría la app entera al arrancar (la pantalla del
 * afinador se importa desde la navegación).
 */

export type PermissionStatus = 'Granted' | 'Denied' | 'Undetermined';

/** Bloque de audio mono con muestras de -1 a 1. */
export type AudioBlock = { samples: Float32Array; sampleRate: number };

export interface Microphone {
  /** true si no es el micrófono real (Expo Go). */
  readonly simulated: boolean;
  requestPermission: () => Promise<PermissionStatus>;
  /** Empieza a entregar bloques a `onBlock`. Lanza un error si no puede arrancar. */
  start: (onBlock: (block: AudioBlock) => void, onError: (message: string) => void) => Promise<void>;
  stop: () => Promise<void>;
  isRecording: () => boolean;
}

/**
 * Configuración del audio que se pide al micrófono (el dispositivo puede dar otra frecuencia
 * de muestreo: se usa la que traiga cada bloque).
 * 4096 muestras a 44.1 kHz son ~93 ms: caben dos periodos del Si0 del bajo de 5 cuerdas
 * y la aguja se actualiza unas 10 veces por segundo.
 */
export const SAMPLE_RATE = 44100;
export const BUFFER_LENGTH = 4096;

// ---------------------------------------------------------------------------
// Micrófono real
// ---------------------------------------------------------------------------

type AudioApi = typeof import('react-native-audio-api');

export const createNativeMicrophone = (): Microphone => {
  // eslint-disable-next-line @typescript-eslint/no-require-imports
  const { AudioManager, AudioRecorder }: AudioApi = require('react-native-audio-api');
  // Una sola instancia del grabador, como recomienda la librería
  let recorder: InstanceType<AudioApi['AudioRecorder']> | null = null;

  const release = async () => {
    if (!recorder) return;
    recorder.clearOnAudioReady();
    recorder.clearOnError();
    if (recorder.isRecording()) await recorder.stop();
    if (Platform.OS === 'ios') await AudioManager.setAudioSessionActivity(false);
  };

  return {
    simulated: false,
    requestPermission: () => AudioManager.requestRecordingPermissions(),
    start: async (onBlock, onError) => {
      try {
        // iOS: sesión solo de grabación y en modo "measurement" (sin el procesado de voz del sistema,
        // que filtra y comprime la señal y estropea la detección de la nota)
        if (Platform.OS === 'ios') {
          AudioManager.setAudioSessionOptions({ iosCategory: 'record', iosMode: 'measurement', iosOptions: [] });
          await AudioManager.setAudioSessionActivity(true);
        }

        recorder = recorder ?? new AudioRecorder();
        recorder.onError((error) => onError(error.message));
        const subscribed = recorder.onAudioReady(
          { sampleRate: SAMPLE_RATE, bufferLength: BUFFER_LENGTH, channelCount: 1 },
          ({ buffer }) => onBlock({ samples: buffer.getChannelData(0), sampleRate: buffer.sampleRate })
        );
        if (subscribed.status === 'error') throw new Error(subscribed.message);

        const result = await recorder.start();
        if (result.status === 'error') throw new Error(result.message);
      } catch (error) {
        await release().catch(() => {});
        throw error;
      }
    },
    stop: release,
    isRecording: () => recorder?.isRecording() ?? false,
  };
};

// ---------------------------------------------------------------------------
// Micrófono simulado (Expo Go)
// ---------------------------------------------------------------------------

/** Cada cuánto llega un bloque simulado: lo que dura un bloque real. */
const SIMULATED_BLOCK_MS = Math.round((BUFFER_LENGTH / SAMPLE_RATE) * 1000);
/** Duración de cada "cuerda" simulada: empieza desafinada y se va afinando. */
export const SIMULATED_NOTE_MS = 4000;
/** Desviación inicial de cada cuerda simulada (cents). */
const SIMULATED_START_CENTS = [-35, 28, -18, 40, -45, 12];
/** Ritmo al que la cuerda simulada se acerca a la nota (fracción del error que se corrige por segundo). */
const SIMULATED_TUNING_RATE = 0.9;
/** Armónicos de la cuerda simulada: la fundamental es más débil que el 2.º, como en una cuerda grave real. */
const SIMULATED_HARMONICS = [0.6, 1, 0.5, 0.25];

/**
 * Desviación (cents) de la cuerda simulada número `note` a los `elapsedMs` de empezar a sonar:
 * parte de una desafinación y se acerca a la nota, como si el músico girase la clavija.
 */
export const simulatedCents = (note: number, elapsedMs: number): number => {
  const start = SIMULATED_START_CENTS[note % SIMULATED_START_CENTS.length];
  return start * Math.exp((-SIMULATED_TUNING_RATE * elapsedMs) / 1000);
};

/** Bloque con el sonido de una cuerda de `frequency` Hz. `phase` continúa la onda del bloque anterior. */
export const simulatedBlock = (frequency: number, phase: number): { block: AudioBlock; phase: number } => {
  const samples = new Float32Array(BUFFER_LENGTH);
  const step = (2 * Math.PI * frequency) / SAMPLE_RATE;
  for (let i = 0; i < BUFFER_LENGTH; i++) {
    const angle = phase + step * i;
    let value = 0;
    SIMULATED_HARMONICS.forEach((weight, k) => { value += weight * Math.sin(angle * (k + 1)); });
    samples[i] = 0.3 * value;
  }
  return { block: { samples, sampleRate: SAMPLE_RATE }, phase: (phase + step * BUFFER_LENGTH) % (2 * Math.PI) };
};

/**
 * Micrófono simulado. `getTargets` devuelve las frecuencias de las cuerdas de la afinación
 * actual y `getLockedString` la cuerda fijada (o null). Sin cuerda fijada recorre las cuerdas
 * una a una; con una fijada, simula solo esa.
 */
export const createSimulatedMicrophone = (
  getTargets: () => number[],
  getLockedString: () => number | null
): Microphone => {
  let timer: ReturnType<typeof setInterval> | null = null;

  return {
    simulated: true,
    // No hay micrófono real: no se pide ningún permiso
    requestPermission: async () => 'Granted',
    start: async (onBlock) => {
      if (timer) return;
      const startedAt = Date.now();
      let phase = 0;
      let lastString: number | null = null;
      let noteStartedAt = startedAt;

      timer = setInterval(() => {
        const targets = getTargets().filter((t) => !Number.isNaN(t));
        if (targets.length === 0) return;

        const now = Date.now();
        const locked = getLockedString();
        const note = Math.floor((now - startedAt) / SIMULATED_NOTE_MS);
        const string = locked ?? note % targets.length;
        // Al cambiar de cuerda, la nueva vuelve a empezar desafinada
        if (string !== lastString) {
          lastString = string;
          noteStartedAt = now;
        }
        const target = targets[Math.min(string, targets.length - 1)];
        const cents = simulatedCents(string, now - noteStartedAt);
        const generated = simulatedBlock(target * Math.pow(2, cents / 1200), phase);
        phase = generated.phase;
        onBlock(generated.block);
      }, SIMULATED_BLOCK_MS);
    },
    stop: async () => {
      if (timer) clearInterval(timer);
      timer = null;
    },
    isRecording: () => timer !== null,
  };
};

/** true en Expo Go: el afinador usa el micrófono simulado. */
export const isMicrophoneSimulated = (): boolean => isRunningInExpoGo();
