import { isRunningInExpoGo } from 'expo';
import {
  AudioBlock,
  BUFFER_LENGTH,
  createNativeMicrophone,
  createSimulatedMicrophone,
  isMicrophoneSimulated,
  SAMPLE_RATE,
  SIMULATED_NOTE_MS,
  simulatedBlock,
  simulatedCents,
} from '../../utils/microphone.utils';
import { detectPitch } from '../../utils/pitch.utils';
import { centsBetween, noteToFrequency } from '../../utils/tuner.utils';

jest.mock('expo', () => ({ isRunningInExpoGo: jest.fn() }));

const mockRecorder = {
  onAudioReady: jest.fn((_options: any, _callback: any) => ({ status: 'success' })),
  clearOnAudioReady: jest.fn(),
  onError: jest.fn(),
  clearOnError: jest.fn(),
  start: jest.fn(async () => ({ status: 'success' })),
  stop: jest.fn(async () => ({ status: 'success' })),
  isRecording: jest.fn(() => false),
};
const mockRequestPermissions = jest.fn();
jest.mock('react-native-audio-api', () => ({
  AudioRecorder: jest.fn(() => mockRecorder),
  AudioManager: {
    requestRecordingPermissions: () => mockRequestPermissions(),
    setAudioSessionOptions: jest.fn(),
    setAudioSessionActivity: jest.fn(async () => {}),
  },
}));

const STANDARD = ['E2', 'A2', 'D3', 'G3', 'B3', 'E4'].map((note) => noteToFrequency(note)!);

describe('microphone.utils', () => {
  beforeEach(() => jest.clearAllMocks());

  describe('isMicrophoneSimulated', () => {
    it('es true solo en Expo Go', () => {
      jest.mocked(isRunningInExpoGo).mockReturnValue(true);
      expect(isMicrophoneSimulated()).toBe(true);
      jest.mocked(isRunningInExpoGo).mockReturnValue(false);
      expect(isMicrophoneSimulated()).toBe(false);
    });
  });

  describe('simulatedCents', () => {
    it('cada cuerda empieza desafinada y se acerca a 0 con el tiempo', () => {
      expect(Math.abs(simulatedCents(0, 0))).toBeGreaterThan(10);
      expect(Math.abs(simulatedCents(0, 1000))).toBeLessThan(Math.abs(simulatedCents(0, 0)));
      expect(Math.abs(simulatedCents(0, SIMULATED_NOTE_MS))).toBeLessThan(2);
    });

    it('hay cuerdas bajas y altas', () => {
      const starts = [0, 1, 2, 3, 4, 5].map((note) => simulatedCents(note, 0));
      expect(starts.some((c) => c < 0)).toBe(true);
      expect(starts.some((c) => c > 0)).toBe(true);
    });
  });

  describe('simulatedBlock', () => {
    it('genera un bloque que YIN reconoce en su frecuencia, aunque el 2.º armónico sea más fuerte', () => {
      const { block } = simulatedBlock(82.41, 0);
      expect(block.samples).toHaveLength(BUFFER_LENGTH);
      expect(block.sampleRate).toBe(SAMPLE_RATE);
      expect(Math.abs(centsBetween(detectPitch(block.samples, block.sampleRate)!, 82.41))).toBeLessThan(1);
    });

    it('la fase continúa entre bloques y se mantiene en [0, 2π)', () => {
      const first = simulatedBlock(110, 0);
      const second = simulatedBlock(110, first.phase);
      expect(second.phase).toBeGreaterThanOrEqual(0);
      expect(second.phase).toBeLessThan(2 * Math.PI);
      // Sin salto entre el último valor de un bloque y el primero del siguiente
      const last = first.block.samples[BUFFER_LENGTH - 1];
      expect(Math.abs(second.block.samples[0] - last)).toBeLessThan(0.05);
    });
  });

  describe('createSimulatedMicrophone', () => {
    beforeEach(() => jest.useFakeTimers());
    afterEach(() => jest.useRealTimers());

    const record = (targets: number[], locked: () => number | null = () => null) => {
      const microphone = createSimulatedMicrophone(() => targets, locked);
      const blocks: AudioBlock[] = [];
      return { microphone, blocks, onBlock: (block: AudioBlock) => blocks.push(block) };
    };
    const pitchOf = (block: AudioBlock) => detectPitch(block.samples, block.sampleRate)!;

    it('está marcado como simulado y concede el permiso sin preguntar', async () => {
      const { microphone } = record(STANDARD);
      expect(microphone.simulated).toBe(true);
      await expect(microphone.requestPermission()).resolves.toBe('Granted');
    });

    it('entrega unos 10 bloques por segundo mientras graba', async () => {
      const { microphone, blocks, onBlock } = record(STANDARD);
      await microphone.start(onBlock, jest.fn());
      expect(microphone.isRecording()).toBe(true);
      jest.advanceTimersByTime(1000);
      expect(blocks.length).toBeGreaterThanOrEqual(9);
      expect(blocks.length).toBeLessThanOrEqual(11);
    });

    it('recorre las cuerdas: cada una suena unos segundos', async () => {
      const { microphone, blocks, onBlock } = record(STANDARD);
      await microphone.start(onBlock, jest.fn());
      jest.advanceTimersByTime(SIMULATED_NOTE_MS - 200);
      expect(Math.abs(centsBetween(pitchOf(blocks.at(-1)!), STANDARD[0]))).toBeLessThan(10);

      jest.advanceTimersByTime(400);
      expect(Math.abs(centsBetween(pitchOf(blocks.at(-1)!), STANDARD[1]))).toBeLessThan(50);
    });

    it('con una cuerda fijada solo simula esa', async () => {
      const { microphone, blocks, onBlock } = record(STANDARD, () => 5);
      await microphone.start(onBlock, jest.fn());
      jest.advanceTimersByTime(SIMULATED_NOTE_MS * 2);
      expect(Math.abs(centsBetween(pitchOf(blocks.at(-1)!), STANDARD[5]))).toBeLessThan(10);
    });

    it('sin cuerdas no entrega nada', async () => {
      const { microphone, blocks, onBlock } = record([]);
      await microphone.start(onBlock, jest.fn());
      jest.advanceTimersByTime(1000);
      expect(blocks).toHaveLength(0);
    });

    it('arrancar dos veces no duplica el audio y stop lo para', async () => {
      const { microphone, blocks, onBlock } = record(STANDARD);
      await microphone.start(onBlock, jest.fn());
      await microphone.start(onBlock, jest.fn());
      jest.advanceTimersByTime(1000);
      expect(blocks.length).toBeLessThanOrEqual(11);

      await microphone.stop();
      const count = blocks.length;
      jest.advanceTimersByTime(1000);
      expect(blocks).toHaveLength(count);
      expect(microphone.isRecording()).toBe(false);
    });
  });

  describe('createNativeMicrophone', () => {
    it('pide el permiso a la librería de audio', async () => {
      mockRequestPermissions.mockResolvedValue('Denied');
      const microphone = createNativeMicrophone();
      expect(microphone.simulated).toBe(false);
      await expect(microphone.requestPermission()).resolves.toBe('Denied');
    });

    it('entrega cada bloque del grabador como muestras + frecuencia de muestreo', async () => {
      const microphone = createNativeMicrophone();
      const onBlock = jest.fn();
      await microphone.start(onBlock, jest.fn());

      const samples = new Float32Array(4);
      mockRecorder.onAudioReady.mock.calls[0][1]({ buffer: { sampleRate: 48000, getChannelData: () => samples } });
      expect(onBlock).toHaveBeenCalledWith({ samples, sampleRate: 48000 });
    });

    it('si el grabador no arranca lanza el error y lo libera', async () => {
      mockRecorder.start.mockResolvedValueOnce({ status: 'error', message: 'ocupado' } as any);
      const microphone = createNativeMicrophone();
      await expect(microphone.start(jest.fn(), jest.fn())).rejects.toThrow('ocupado');
      expect(mockRecorder.clearOnAudioReady).toHaveBeenCalled();
    });

    it('stop sin haber arrancado no hace nada', async () => {
      await expect(createNativeMicrophone().stop()).resolves.toBeUndefined();
      expect(createNativeMicrophone().isRecording()).toBe(false);
    });
  });
});
